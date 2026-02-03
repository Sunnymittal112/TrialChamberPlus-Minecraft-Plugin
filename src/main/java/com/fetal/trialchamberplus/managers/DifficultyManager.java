package com.fetal.trialchamberplus.managers;

import com.fetal.trialchamberplus.TrialChamberPlus;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Breeze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class DifficultyManager {

    private final TrialChamberPlus plugin;
    
    private boolean scaleWithPlayers;
    private double healthMultiplierPerPlayer;
    private double maxMultiplier;
    private double breezeExtraHealth;
    private double breezeDamageMultiplier;

    public DifficultyManager(TrialChamberPlus plugin) {
        this.plugin = plugin;
        reloadSettings();
    }

    public void reloadSettings() {
        var config = plugin.getConfig();
        
        this.scaleWithPlayers = config.getBoolean("difficulty.scale-with-players", true);
        this.healthMultiplierPerPlayer = config.getDouble("difficulty.health-multiplier-per-player", 0.25);
        this.maxMultiplier = config.getDouble("difficulty.max-multiplier", 3.0);
        this.breezeExtraHealth = config.getDouble("difficulty.breeze.extra-health", 10.0);
        this.breezeDamageMultiplier = config.getDouble("difficulty.breeze.damage-multiplier", 1.5);
    }

    public void applyDifficultyScaling(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return;

        // Count nearby players
        int nearbyPlayers = countNearbyPlayers(entity, 50);
        
        if (nearbyPlayers <= 1 && !scaleWithPlayers) return;

        // Calculate multiplier
        double multiplier = 1.0;
        
        if (scaleWithPlayers && nearbyPlayers > 1) {
            multiplier += (nearbyPlayers - 1) * healthMultiplierPerPlayer;
            multiplier = Math.min(multiplier, maxMultiplier);
        }

        // Apply health scaling
        var healthAttribute = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            double baseHealth = healthAttribute.getBaseValue();
            double newHealth = baseHealth * multiplier;
            
            // Extra health for Breeze
            if (entity instanceof Breeze) {
                newHealth += breezeExtraHealth;
            }
            
            healthAttribute.setBaseValue(newHealth);
            livingEntity.setHealth(newHealth);
        }

        // Apply damage scaling for Breeze
        if (entity instanceof Breeze) {
            var damageAttribute = livingEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            if (damageAttribute != null) {
                double baseDamage = damageAttribute.getBaseValue();
                damageAttribute.setBaseValue(baseDamage * breezeDamageMultiplier);
            }
        }

        // Visual indicator for scaled mobs
        if (multiplier > 1.0) {
            livingEntity.setGlowing(true);
            livingEntity.setCustomName(plugin.getMessageUtils().color(
                "&c&l⚔ &r" + formatEntityName(entity.getType().name()) + " &c&l⚔"
            ));
            livingEntity.setCustomNameVisible(true);
        }
    }

    private int countNearbyPlayers(Entity entity, double radius) {
        Collection<Entity> nearbyEntities = entity.getNearbyEntities(radius, radius, radius);
        return (int) nearbyEntities.stream()
            .filter(e -> e instanceof Player)
            .count() + 1; // +1 for the player who triggered spawn
    }

    private String formatEntityName(String name) {
        return name.charAt(0) + name.substring(1).toLowerCase().replace("_", " ");
    }
}