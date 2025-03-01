package me.gamecrash.tpslimiter;

import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class MessageHelper {
    //private static JavaPlugin plugin = (JavaPlugin)Bukkit.getPluginManager().getPlugin("TPSLimiter");

    public static String getMessage(String messagePath, JavaPlugin plugin) {
        boolean prefix = plugin.getConfig().getBoolean("showPrefix");
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

    public static String returnFormatted(String unformatted, DoubleStatistic<StatisticWindow.TicksPerSecond> tps, JavaPlugin plugin) {
        double s5 = tps.poll(StatisticWindow.TicksPerSecond.SECONDS_5);
        double s10 = tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10);
        double m1 = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_1);
        double m5 = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_5);
        double m15 = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_15);
        String highColor = plugin.getConfig().getString("tpsColor.highTpsColor");
        String mediumColor = plugin.getConfig().getString("tpsColor.mediumTpsColor");
        String lowColor = plugin.getConfig().getString("tpsColor.lowTpsColor");

        if (!plugin.getConfig().getBoolean("tpsColor.enabled")) return unformatted
                .replace("%5s%", String.format(Locale.US, "%.1f", s5))
                .replace("%10s%", String.format(Locale.US, "%.1f", s10))
                .replace("%1m%", String.format(Locale.US, "%.1f", m1))
                .replace("%5m%", String.format(Locale.US, "%.1f", m5))
                .replace("%15m%", String.format(Locale.US, "%.1f", m15));
        return unformatted
                .replace("%5s%", getColorFormattedTps(s5, plugin, highColor, mediumColor, lowColor))
                .replace("%10s%", getColorFormattedTps(s10, plugin, highColor, mediumColor, lowColor))
                .replace("%1m%", getColorFormattedTps(m1, plugin, highColor, mediumColor, lowColor))
                .replace("%5m%", getColorFormattedTps(m5, plugin, highColor, mediumColor, lowColor))
                .replace("%15m%", getColorFormattedTps(m15, plugin, highColor, mediumColor, lowColor));
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

    private static String getColorFormattedTps(double tps, JavaPlugin plugin, String highColor, String mediumColor, String lowColor) {
        double mediumTps = plugin.getConfig().getDouble("tpsColor.mediumTps");
        double lowTps = plugin.getConfig().getDouble("tpsColor.lowTps");
        if (tps > mediumTps) {
            return highColor + String.format(Locale.US, "%.1f", tps);
        } else if (tps <= lowTps) {
            return lowColor + String.format(Locale.US, "%.1f", tps);
        } else {
            return mediumColor + String.format(Locale.US, "%.1f", tps);
        }
    }
}
