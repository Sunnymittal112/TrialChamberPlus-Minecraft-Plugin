package com.fetal.trialchamberplus.managers;

import com.fetal.trialchamberplus.TrialChamberPlus;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RewardManager {

    private final TrialChamberPlus plugin;
    private final Random random = new Random();
    private final List<CustomDrop> customDrops = new ArrayList<>();

    public RewardManager(TrialChamberPlus plugin) {
        this.plugin = plugin;
        reloadRewards();
    }

    public void reloadRewards() {
        customDrops.clear();

        if (!plugin.getConfig().getBoolean("rewards.custom-drops.enabled")) return;

        var dropList = plugin.getConfig().getMapList("rewards.custom-drops.items");
        
        for (var dropMap : dropList) {
            try {
                String materialName = (String) dropMap.get("material");
                Material material = Material.valueOf(materialName.toUpperCase());
                double chance = ((Number) dropMap.get("chance")).doubleValue();
                int minAmount = ((Number) dropMap.get("min-amount")).intValue();
                int maxAmount = ((Number) dropMap.get("max-amount")).intValue();

                customDrops.add(new CustomDrop(material, chance, minAmount, maxAmount));
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid custom drop configuration: " + dropMap);
            }
        }

        plugin.getLogger().info("Loaded " + customDrops.size() + " custom drops.");
    }

    public void giveCustomRewards(Player player, double multiplier) {
        if (!plugin.getConfig().getBoolean("rewards.custom-drops.enabled")) return;

        // Bonus permission check
        if (player.hasPermission("trialchamberplus.rewards.bonus")) {
            multiplier *= 1.5;
        }

        for (CustomDrop drop : customDrops) {
            double adjustedChance = drop.chance * multiplier;
            
            if (random.nextDouble() < adjustedChance) {
                int amount = ThreadLocalRandom.current().nextInt(
                    drop.minAmount, 
                    drop.maxAmount + 1
                );
                
                ItemStack item = new ItemStack(drop.material, amount);
                
                // Give item or drop if inventory full
                var leftover = player.getInventory().addItem(item);
                if (!leftover.isEmpty()) {
                    for (ItemStack remaining : leftover.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), remaining);
                    }
                }

                plugin.getMessageUtils().sendMessage(player, 
                    "&a+ &e" + amount + "x " + formatMaterial(drop.material) + " &7(Bonus Reward!)");
            }
        }
    }

    private String formatMaterial(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : name.toCharArray()) {
            if (c == ' ') {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }

    private record CustomDrop(Material material, double chance, int minAmount, int maxAmount) {}
}