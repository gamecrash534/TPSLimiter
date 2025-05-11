package me.gamecrash.tpslimiter;

import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.ServerTickManager;
import org.bukkit.command.CommandSender;

import java.util.Locale;

public class MessageHelper {
    private static final String PREFIX_PATH = "messages.prefix";
    private static final String SHOW_PREFIX_PATH = "showPrefix";
    private static final String BROADCAST_CHANGES_PATH = "broadcastChanges";
    private static final String YOURSELF_STRING_PATH = "yourselfString";
    private static final String TPS_COLOR_ENABLED_PATH = "tpsColor.enabled";
    private static final String HIGH_TPS_COLOR_PATH = "tpsColor.highTpsColor";
    private static final String MEDIUM_TPS_COLOR_PATH = "tpsColor.mediumTpsColor";
    private static final String LOW_TPS_COLOR_PATH = "tpsColor.lowTpsColor";
    private static final String MEDIUM_TPS_THRESHOLD_PATH = "tpsColor.mediumTps";
    private static final String LOW_TPS_THRESHOLD_PATH = "tpsColor.lowTps";

    private static final String PERMISSION_GET_NOTIFICATION = "tpslimiter.getNotifications";

    private static final TPSLimiter plugin = TPSLimiter.getPlugin();

    public static String getMessage(String messagePath) {
        String message = plugin.getConfig().getString(messagePath);
        if (plugin.getConfig().getBoolean(SHOW_PREFIX_PATH)) {
            String prefix = plugin.getConfig().getString(PREFIX_PATH);
            return formatMessage(prefix + message);
        }
        return formatMessage(message);
    }

    public static String formatMessage(String message) {
        return MiniMessage.builder()
            .build()
            .serialize(LegacyComponentSerializer.legacySection().deserialize(colorFormatted(message)))
            .replace("\\<", "<");
    }

    public static String colorFormatted(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String formatTpsMessage(String message, DoubleStatistic<StatisticWindow.TicksPerSecond> tps) {
        double s5 = tps.poll(StatisticWindow.TicksPerSecond.SECONDS_5);
        double s10 = tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10);
        double m1 = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_1);
        double m5 = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_5);
        double m15 = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_15);

        if (!plugin.getConfig().getBoolean(TPS_COLOR_ENABLED_PATH)) return message
            .replace("%5s%", String.format(Locale.US, "%.2f", s5))
            .replace("%10s%", String.format(Locale.US, "%.2f", s10))
            .replace("%1m%", String.format(Locale.US, "%.2f", m1))
            .replace("%5m%", String.format(Locale.US, "%.2f", m5))
            .replace("%15m%", String.format(Locale.US, "%.2f", m15));
        return message
            .replace("%5s%", getColorFormattedTps(s5))
            .replace("%10s%", getColorFormattedTps(s10))
            .replace("%1m%", getColorFormattedTps(m1))
            .replace("%5m%", getColorFormattedTps(m5))
            .replace("%15m%", getColorFormattedTps(m15));
    }

    public static String formatTpsMessage(String template, long tps) {
        return template.replace("%tps%", String.valueOf(tps));
    }

    public static String formatServerState(String message) {
        ServerTickManager tickManager = Bukkit.getServer().getServerTickManager();
        return message
                .replace("%frozen%", String.valueOf(tickManager.isFrozen()))
                .replace("%tps%", String.format(Locale.US, "%.2f", tickManager.getTickRate()))
                .replace("%stepping%", String.valueOf(tickManager.isStepping()));
    }

    public static void broadcastMessage(String message, CommandSender sender) {
        String formattedMessage = message.replace("%player%", sender.getName());
        if (plugin.getConfig().getBoolean(BROADCAST_CHANGES_PATH)) {
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize(formattedMessage), PERMISSION_GET_NOTIFICATION);
        } else {
            sender.sendRichMessage(formattedMessage.replace("%player%", plugin.getConfig().getString(YOURSELF_STRING_PATH)));
        }
    }

    private static String getColorFormattedTps(double tps) {
        double mediumTps = plugin.getConfig().getDouble(MEDIUM_TPS_THRESHOLD_PATH);
        double lowTps = plugin.getConfig().getDouble(LOW_TPS_THRESHOLD_PATH);
        String highColor = plugin.getConfig().getString(HIGH_TPS_COLOR_PATH);
        String mediumColor = plugin.getConfig().getString(MEDIUM_TPS_COLOR_PATH);
        String lowColor = plugin.getConfig().getString(LOW_TPS_COLOR_PATH);

        String color = tps > mediumTps ? highColor : (tps <= lowTps ? lowColor : mediumColor);
        return color + String.format(Locale.US, "%.1f", tps) + color.replace("<", "</");
    }
}