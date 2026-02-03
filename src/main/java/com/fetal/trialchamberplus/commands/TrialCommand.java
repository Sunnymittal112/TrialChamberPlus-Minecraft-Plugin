package com.fetal.trialchamberplus.commands;

import com.fetal.trialchamberplus.TrialChamberPlus;
import com.fetal.trialchamberplus.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TrialCommand implements CommandExecutor, TabCompleter {

    private final TrialChamberPlus plugin;
    private final MessageUtils msg;

    public TrialCommand(TrialChamberPlus plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageUtils();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> sendHelp(sender);
            case "stats" -> showStats(sender);
            case "leaderboard", "lb", "top" -> showLeaderboard(sender);
            case "reload" -> reloadConfig(sender);
            case "addpoints" -> addPoints(sender, args);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(msg.color("&6&l========== TrialChamberPlus =========="));
        sender.sendMessage(msg.color("&e/trial help &7- Show this help menu"));
        sender.sendMessage(msg.color("&e/trial stats &7- View your trial statistics"));
        sender.sendMessage(msg.color("&e/trial leaderboard &7- View top players"));
        if (sender.hasPermission("trialchamberplus.admin")) {
            sender.sendMessage(msg.color("&c/trial reload &7- Reload configuration"));
            sender.sendMessage(msg.color("&c/trial addpoints <player> <amount> &7- Add points"));
        }
        sender.sendMessage(msg.color("&6&l======================================"));
    }

    private void showStats(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(msg.color("&cThis command is for players only!"));
            return;
        }

        if (!player.hasPermission("trialchamberplus.stats")) {
            sender.sendMessage(msg.color(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        UUID uuid = player.getUniqueId();
        var lbManager = plugin.getLeaderboardManager();

        sender.sendMessage(msg.color(plugin.getConfig().getString("messages.stats-header")));
        sender.sendMessage(msg.color("&7Total Points: &e" + lbManager.getPoints(uuid)));
        sender.sendMessage(msg.color("&7Mobs Killed: &e" + lbManager.getMobKills(uuid)));
        sender.sendMessage(msg.color("&7Breeze Killed: &e" + lbManager.getBreezeKills(uuid)));
        sender.sendMessage(msg.color("&7Chambers Completed: &e" + lbManager.getChambersCompleted(uuid)));
        sender.sendMessage(msg.color("&7Vaults Opened: &e" + lbManager.getVaultsOpened(uuid)));
        sender.sendMessage(msg.color("&7Your Rank: &6#" + lbManager.getPlayerRank(uuid)));
    }

    private void showLeaderboard(CommandSender sender) {
        if (!sender.hasPermission("trialchamberplus.leaderboard")) {
            sender.sendMessage(msg.color(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        sender.sendMessage(msg.color(plugin.getConfig().getString("messages.leaderboard-header")));
        
        var topPlayers = plugin.getLeaderboardManager().getTopPlayers(
            plugin.getConfig().getInt("leaderboard.max-entries", 10)
        );

        int rank = 1;
        for (var entry : topPlayers) {
            String rankColor = switch (rank) {
                case 1 -> "&6&l";
                case 2 -> "&7&l";
                case 3 -> "&c&l";
                default -> "&e";
            };
            sender.sendMessage(msg.color(rankColor + "#" + rank + " &f" + entry.getKey() + " &7- &e" + entry.getValue() + " points"));
            rank++;
        }

        if (topPlayers.isEmpty()) {
            sender.sendMessage(msg.color("&7No data yet! Start exploring Trial Chambers!"));
        }
    }

    private void reloadConfig(CommandSender sender) {
        if (!sender.hasPermission("trialchamberplus.admin")) {
            sender.sendMessage(msg.color(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        plugin.reloadPlugin();
        sender.sendMessage(msg.color(plugin.getConfig().getString("messages.reload-success")));
    }

    private void addPoints(CommandSender sender, String[] args) {
        if (!sender.hasPermission("trialchamberplus.admin")) {
            sender.sendMessage(msg.color(plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(msg.color("&cUsage: /trial addpoints <player> <amount>"));
            return;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(msg.color("&cPlayer not found!"));
            return;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            plugin.getLeaderboardManager().addPoints(target.getUniqueId(), amount);
            sender.sendMessage(msg.color("&aAdded " + amount + " points to " + target.getName()));
        } catch (NumberFormatException e) {
            sender.sendMessage(msg.color("&cInvalid number!"));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(Arrays.asList("help", "stats", "leaderboard"));
            if (sender.hasPermission("trialchamberplus.admin")) {
                subcommands.add("reload");
                subcommands.add("addpoints");
            }
            for (String cmd : subcommands) {
                if (cmd.startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("addpoints")) {
            if (sender.hasPermission("trialchamberplus.admin")) {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        }

        return completions;
    }
}