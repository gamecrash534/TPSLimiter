package me.gamecrash.tpslimiter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class MessageHelper {
    public static String getMessage(String messagePath, JavaPlugin plugin) {
        Boolean prefix = plugin.getConfig().getBoolean("showPrefix");
        if (prefix) return returnMiniMessage(plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString(messagePath));
        return returnMiniMessage(plugin.getConfig().getString(messagePath));
    }
    public static String colorFormatted(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    public static String returnMiniMessage(String message) {
        MiniMessage builder = MiniMessage.builder().build();
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
        Component component = legacy.deserialize(colorFormatted(message));
        return builder.serialize(component).replaceAll("\\\\", "");
    }
}
