package com.fetal.trialchamberplus;

import com.fetal.trialchamberplus.commands.TrialCommand;
import com.fetal.trialchamberplus.listeners.TrialChamberListener;
import com.fetal.trialchamberplus.managers.DifficultyManager;
import com.fetal.trialchamberplus.managers.LeaderboardManager;
import com.fetal.trialchamberplus.managers.RewardManager;
import com.fetal.trialchamberplus.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class TrialChamberPlus extends JavaPlugin {

    private static TrialChamberPlus instance;
    private LeaderboardManager leaderboardManager;
    private RewardManager rewardManager;
    private DifficultyManager difficultyManager;
    private MessageUtils messageUtils;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        this.messageUtils = new MessageUtils(this);
        this.leaderboardManager = new LeaderboardManager(this);
        this.rewardManager = new RewardManager(this);
        this.difficultyManager = new DifficultyManager(this);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new TrialChamberListener(this), this);
        
        // Register commands
        getCommand("trial").setExecutor(new TrialCommand(this));
        getCommand("trial").setTabCompleter(new TrialCommand(this));
        
        // Load data
        leaderboardManager.loadData();
        
        getLogger().info("========================================");
        getLogger().info("  TrialChamberPlus v" + getDescription().getVersion());
        getLogger().info("  Author: FeTaL");
        getLogger().info("  Status: Enabled Successfully!");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        // Save data
        if (leaderboardManager != null) {
            leaderboardManager.saveData();
        }
        
        getLogger().info("TrialChamberPlus disabled!");
    }

    public void reloadPlugin() {
        reloadConfig();
        leaderboardManager.loadData();
        rewardManager.reloadRewards();
        difficultyManager.reloadSettings();
    }

    public static TrialChamberPlus getInstance() {
        return instance;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public DifficultyManager getDifficultyManager() {
        return difficultyManager;
    }

    public MessageUtils getMessageUtils() {
        return messageUtils;
    }
}