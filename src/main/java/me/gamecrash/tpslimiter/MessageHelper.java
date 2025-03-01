package me.gamecrash.tpslimiter;

import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

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
        return builder.serialize(component).replaceAll("\\\\<", "<");
    }

    public static String returnFormatted(String unformatted, DoubleStatistic<StatisticWindow.TicksPerSecond> tps) {
        return unformatted.replace("%5s%", String.format(Locale.US, "%.1f", tps.poll(StatisticWindow.TicksPerSecond.SECONDS_5)))
                .replace("%10s%", String.format(Locale.US, "%.1f", tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10)))
                .replace("%1m%", String.format(Locale.US, "%.1f", tps.poll(StatisticWindow.TicksPerSecond.MINUTES_1)))
                .replace("%5m%", String.format(Locale.US, "%.1f", tps.poll(StatisticWindow.TicksPerSecond.MINUTES_5)))
                .replace("%15m%", String.format(Locale.US, "%.1f", tps.poll(StatisticWindow.TicksPerSecond.MINUTES_15)));
    }
    public static String returnFormatted(String unformatted, CommandSender sender) {
        return unformatted.replace("%player%", sender.getName());
    }
    public static String returnFormatted(String unformatted, String sender) {
        return unformatted.replace("%player%", sender);
    }
    public static String returnFormatted(String unformatted, CommandSender sender, long newTps) {
        return unformatted.replace("%player%", sender.getName())
                .replace("%tps%", String.valueOf(newTps));
    }
}
