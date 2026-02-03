package com.fetal.trialchamberplus.listeners;

import com.fetal.trialchamberplus.TrialChamberPlus;
import org.bukkit.Material;
import org.bukkit.entity.Breeze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TrialChamberListener implements Listener {

    private final TrialChamberPlus plugin;
    private final Set<UUID> playersInChamber = new HashSet<>();
    private final Set<UUID> announcedPlayers = new HashSet<>();

    private static final Set<EntityType> TRIAL_MOBS = Set.of(
        EntityType.BREEZE,
        EntityType.BOGGED,
        EntityType.ZOMBIE,
        EntityType.SKELETON,
        EntityType.SPIDER,
        EntityType.CAVE_SPIDER,
        EntityType.HUSK,
        EntityType.STRAY,
        EntityType.SLIME,
        EntityType.SILVERFISH
    );

    public TrialChamberListener(TrialChamberPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        Player killer = event.getEntity().getKiller();

        if (killer == null) return;
        if (!TRIAL_MOBS.contains(entity.getType())) return;
        
        if (!isLikelyInTrialChamber(entity)) return;

        var lbManager = plugin.getLeaderboardManager();
        var config = plugin.getConfig();

        if (entity instanceof Breeze) {
            int points = config.getInt("leaderboard.points.breeze-kill", 25);
            lbManager.addPoints(killer.getUniqueId(), points);
            lbManager.addBreezeKill(killer.getUniqueId());
            
            if (config.getBoolean("settings.enable-custom-rewards")) {
                plugin.getRewardManager().giveCustomRewards(killer, 1.5);
            }
            
            plugin.getMessageUtils().sendActionBar(killer, 
                "&a+&e" + points + " &apoints &7(Breeze Kill)");
        } else {
            int points = config.getInt("leaderboard.points.mob-kill", 10);
            lbManager.addPoints(killer.getUniqueId(), points);
            lbManager.addMobKill(killer.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!plugin.getConfig().getBoolean("settings.enable-difficulty-scaling")) return;
        if (!TRIAL_MOBS.contains(event.getEntity().getType())) return;
        if (!isLikelyInTrialChamber(event.getEntity())) return;

        plugin.getDifficultyManager().applyDifficultyScaling(event.getEntity());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Material blockType = event.getClickedBlock().getType();
        Player player = event.getPlayer();

        if (blockType == Material.VAULT) {
            var lbManager = plugin.getLeaderboardManager();
            int points = plugin.getConfig().getInt("leaderboard.points.vault-open", 50);
            
            lbManager.addPoints(player.getUniqueId(), points);
            lbManager.addVaultOpened(player.getUniqueId());
            
            plugin.getMessageUtils().sendActionBar(player, 
                "&a+&e" + points + " &apoints &7(Vault Opened)");

            if (plugin.getConfig().getBoolean("settings.enable-custom-rewards")) {
                plugin.getRewardManager().giveCustomRewards(player, 2.0);
            }
        }

        if (blockType == Material.TRIAL_SPAWNER) {
            if (plugin.getConfig().getBoolean("settings.enable-custom-rewards")) {
                int bonusXp = plugin.getConfig().getInt("rewards.bonus-xp", 50);
                player.giveExp(bonusXp);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("announcements.on-enter")) return;
        
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        boolean inChamber = isLikelyInTrialChamber(player);

        if (inChamber && !playersInChamber.contains(uuid)) {
            playersInChamber.add(uuid);
            
            if (!announcedPlayers.contains(uuid) && 
                plugin.getConfig().getBoolean("settings.enable-announcements")) {
                
                String message = plugin.getConfig().getString("announcements.enter-message", 
                    "&e%player% &7has entered a &6Trial Chamber&7!");
                message = message.replace("%player%", player.getName());
                
                plugin.getServer().broadcastMessage(plugin.getMessageUtils().color(
                    plugin.getConfig().getString("settings.prefix") + message));
                
                announcedPlayers.add(uuid);
            }
        } else if (!inChamber && playersInChamber.contains(uuid)) {
            playersInChamber.remove(uuid);
        }
    }

    private boolean isLikelyInTrialChamber(Entity entity) {
        int y = entity.getLocation().getBlockY();
        
        if (y < -60 || y > 10) return false;

        var loc = entity.getLocation();
        var world = entity.getWorld();

        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                Material type = world.getBlockAt(
                    loc.getBlockX() + x, 
                    loc.getBlockY(), 
                    loc.getBlockZ() + z
                ).getType();
                
                if (type == Material.TRIAL_SPAWNER || 
                    type == Material.VAULT || 
                    type == Material.TUFF_BRICKS ||
                    type == Material.CHISELED_TUFF_BRICKS ||
                    type == Material.POLISHED_TUFF) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public void markChamberCompleted(Player player) {
        var lbManager = plugin.getLeaderboardManager();
        int points = plugin.getConfig().getInt("leaderboard.points.chamber-complete", 100);
        
        lbManager.addPoints(player.getUniqueId(), points);
        lbManager.addChamberCompleted(player.getUniqueId());

        if (plugin.getConfig().getBoolean("announcements.on-complete")) {
            String message = plugin.getConfig().getString("announcements.complete-message",
                "&a%player% &7has conquered a &6Trial Chamber&7!");
            message = message.replace("%player%", player.getName())
                           .replace("%points%", String.valueOf(points));
            
            plugin.getServer().broadcastMessage(plugin.getMessageUtils().color(
                plugin.getConfig().getString("settings.prefix") + message));
        }

        announcedPlayers.remove(player.getUniqueId());
    }
}