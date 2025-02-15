package me.gamecrash.tpslimiter;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class MessageHelper {
    public static String getMessage(String messagePath, JavaPlugin plugin) {
        Boolean prefix = plugin.getConfig().getBoolean("showPrefix");
        if (prefix) return colorFormatted(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString(messagePath));
        return colorFormatted(plugin.getConfig().getString(messagePath));
    }
    public static String colorFormatted(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
