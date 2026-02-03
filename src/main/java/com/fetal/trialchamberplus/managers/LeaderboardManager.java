package com.fetal.trialchamberplus.managers;

import com.fetal.trialchamberplus.TrialChamberPlus;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {

    private final TrialChamberPlus plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    private final Map<UUID, Integer> points = new HashMap<>();
    private final Map<UUID, Integer> mobKills = new HashMap<>();
    private final Map<UUID, Integer> breezeKills = new HashMap<>();
    private final Map<UUID, Integer> chambersCompleted = new HashMap<>();
    private final Map<UUID, Integer> vaultsOpened = new HashMap<>();
    private final Map<UUID, String> playerNames = new HashMap<>();

    public LeaderboardManager(TrialChamberPlus plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
    }

    public void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml!");
                e.printStackTrace();
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Clear existing data
        points.clear();
        mobKills.clear();
        breezeKills.clear();
        chambersCompleted.clear();
        vaultsOpened.clear();
        playerNames.clear();

        // Load player data
        if (dataConfig.contains("players")) {
            for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    String path = "players." + uuidString + ".";
                    
                    points.put(uuid, dataConfig.getInt(path + "points", 0));
                    mobKills.put(uuid, dataConfig.getInt(path + "mob-kills", 0));
                    breezeKills.put(uuid, dataConfig.getInt(path + "breeze-kills", 0));
                    chambersCompleted.put(uuid, dataConfig.getInt(path + "chambers-completed", 0));
                    vaultsOpened.put(uuid, dataConfig.getInt(path + "vaults-opened", 0));
                    playerNames.put(uuid, dataConfig.getString(path + "name", "Unknown"));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in playerdata: " + uuidString);
                }
            }
        }

        plugin.getLogger().info("Loaded data for " + points.size() + " players.");
    }

    public void saveData() {
        for (UUID uuid : points.keySet()) {
            String path = "players." + uuid.toString() + ".";
            
            dataConfig.set(path + "points", points.getOrDefault(uuid, 0));
            dataConfig.set(path + "mob-kills", mobKills.getOrDefault(uuid, 0));
            dataConfig.set(path + "breeze-kills", breezeKills.getOrDefault(uuid, 0));
            dataConfig.set(path + "chambers-completed", chambersCompleted.getOrDefault(uuid, 0));
            dataConfig.set(path + "vaults-opened", vaultsOpened.getOrDefault(uuid, 0));
            dataConfig.set(path + "name", playerNames.getOrDefault(uuid, "Unknown"));
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml!");
            e.printStackTrace();
        }
    }

    // Points
    public void addPoints(UUID uuid, int amount) {
        points.put(uuid, getPoints(uuid) + amount);
        updatePlayerName(uuid);
        saveData();
    }

    public int getPoints(UUID uuid) {
        return points.getOrDefault(uuid, 0);
    }

    // Mob Kills
    public void addMobKill(UUID uuid) {
        mobKills.put(uuid, getMobKills(uuid) + 1);
        updatePlayerName(uuid);
    }

    public int getMobKills(UUID uuid) {
        return mobKills.getOrDefault(uuid, 0);
    }

    // Breeze Kills
    public void addBreezeKill(UUID uuid) {
        breezeKills.put(uuid, getBreezeKills(uuid) + 1);
        updatePlayerName(uuid);
    }

    public int getBreezeKills(UUID uuid) {
        return breezeKills.getOrDefault(uuid, 0);
    }

    // Chambers Completed
    public void addChamberCompleted(UUID uuid) {
        chambersCompleted.put(uuid, getChambersCompleted(uuid) + 1);
        updatePlayerName(uuid);
    }

    public int getChambersCompleted(UUID uuid) {
        return chambersCompleted.getOrDefault(uuid, 0);
    }

    // Vaults Opened
    public void addVaultOpened(UUID uuid) {
        vaultsOpened.put(uuid, getVaultsOpened(uuid) + 1);
        updatePlayerName(uuid);
    }

    public int getVaultsOpened(UUID uuid) {
        return vaultsOpened.getOrDefault(uuid, 0);
    }

    // Player Name
    private void updatePlayerName(UUID uuid) {
        var player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            playerNames.put(uuid, player.getName());
        }
    }

    // Leaderboard
    public List<Map.Entry<String, Integer>> getTopPlayers(int limit) {
        return points.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> Map.entry(
                playerNames.getOrDefault(entry.getKey(), "Unknown"),
                entry.getValue()
            ))
            .collect(Collectors.toList());
    }

    public int getPlayerRank(UUID uuid) {
        List<UUID> sorted = points.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        int rank = sorted.indexOf(uuid);
        return rank == -1 ? sorted.size() + 1 : rank + 1;
    }
}