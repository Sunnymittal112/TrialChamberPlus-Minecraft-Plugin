package com.fetal.trialchamberplus.utils;

import com.fetal.trialchamberplus.TrialChamberPlus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtils {

    private final TrialChamberPlus plugin;
    private final LegacyComponentSerializer serializer;

    public MessageUtils(TrialChamberPlus plugin) {
        this.plugin = plugin;
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
    }

    public String color(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public Component colorComponent(String message) {
        if (message == null) return Component.empty();
        return serializer.deserialize(message);
    }

    public void sendMessage(Player player, String message) {
        String prefix = plugin.getConfig().getString("settings.prefix", "&6&l[TrialChamber+] &r");
        player.sendMessage(color(prefix + message));
    }

    public void sendActionBar(Player player, String message) {
        player.sendActionBar(colorComponent(message));
    }

    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(color(title), color(subtitle), fadeIn, stay, fadeOut);
    }

    public void broadcast(String message) {
        String prefix = plugin.getConfig().getString("settings.prefix", "&6&l[TrialChamber+] &r");
        plugin.getServer().broadcastMessage(color(prefix + message));
    }
}