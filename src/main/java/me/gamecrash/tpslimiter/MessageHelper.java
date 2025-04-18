package me.gamecrash.tpslimiter;

import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.ServerTickManager;
import org.bukkit.command.CommandSender;

import java.util.Locale;

public class MessageHelper {
    static TPSLimiter plugin = TPSLimiter.getPlugin();

    public static String getMessage(String messagePath) {
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

    public static String returnFormatted(String unformatted, DoubleStatistic<StatisticWindow.TicksPerSecond> tps) {
        double s5 = tps.poll(StatisticWindow.TicksPerSecond.SECONDS_5);
        double s10 = tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10);
        double m1 = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_1);
        double m5 = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_5);
        double m15 = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_15);

        if (!plugin.getConfig().getBoolean("tpsColor.enabled")) return unformatted
                .replace("%5s%", String.format(Locale.US, "%.1f", s5))
                .replace("%10s%", String.format(Locale.US, "%.1f", s10))
                .replace("%1m%", String.format(Locale.US, "%.1f", m1))
                .replace("%5m%", String.format(Locale.US, "%.1f", m5))
                .replace("%15m%", String.format(Locale.US, "%.1f", m15));
        return unformatted
                .replace("%5s%", getColorFormattedTps(s5))
                .replace("%10s%", getColorFormattedTps(s10))
                .replace("%1m%", getColorFormattedTps(m1))
                .replace("%5m%", getColorFormattedTps(m5))
                .replace("%15m%", getColorFormattedTps(m15));
    }
    public static String returnFormatted(String unformatted, long newTps) {
        return unformatted.replace("%tps%", String.valueOf(newTps));
    }
    public static String returnFormatted(String unformatted) {
        ServerTickManager tickManager = Bukkit.getServer().getServerTickManager();
        return unformatted.replace("%frozen%", String.valueOf(tickManager.isFrozen()))
                .replace("%tps%", String.format(Locale.US, "%.1f", tickManager.getTickRate()))
                .replace("%stepping%", String.valueOf(tickManager.isStepping()));
    }

    public static void broadcastMessage(String message, CommandSender sender) {
        if (plugin.getConfig().getBoolean("broadcastChanges")) {
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize(
                    message.replace("%player%", sender.getName())
            ));
        } else {
            sender.sendRichMessage(message.replace("%player%", plugin.getConfig().getString("yourselfString")));

        }
    }

    private static String getColorFormattedTps(double tps) {
        double mediumTps = plugin.getConfig().getDouble("tpsColor.mediumTps");
        double lowTps = plugin.getConfig().getDouble("tpsColor.lowTps");
        String highColor = plugin.getConfig().getString("tpsColor.highTpsColor");
        String mediumColor = plugin.getConfig().getString("tpsColor.mediumTpsColor");
        String lowColor = plugin.getConfig().getString("tpsColor.lowTpsColor");
        if (tps > mediumTps) {
            return highColor + String.format(Locale.US, "%.1f", tps) + highColor.replaceAll("<", "</");
        } else if (tps <= lowTps) {
            return lowColor + String.format(Locale.US, "%.1f", tps) + lowColor.replaceAll("<", "</");
        } else {
            return mediumColor + String.format(Locale.US, "%.1f", tps) + mediumColor.replaceAll("<", "</");
        }
    }

}
