package me.gamecrash.tpslimiter;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class TPSCommand {

    public static LiteralCommandNode<CommandSourceStack> build(TPSLimiter plugin) {
        LiteralArgumentBuilder<CommandSourceStack> tpsBuilder = Commands.literal("tps")
                .requires(sender -> sender.getSender().hasPermission("tps"))
                .executes(ctx -> {
                    Spark spark = SparkProvider.get();
                    DoubleStatistic<StatisticWindow.TicksPerSecond> tps = spark.tps();
                    ctx.getSource().getSender().sendMessage(returnFormatted(MessageHelper.getMessage("messages.tps", plugin), tps));
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("reload")
                    .requires(sender -> sender.getSender().hasPermission("tps.reload"))
                    .executes(ctx -> {
                        plugin.reloadConf();
                        ctx.getSource().getSender().sendMessage(MessageHelper.getMessage("messages.reload", plugin));
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(Commands.literal("freeze")
                        .requires(sender -> sender.getSender().hasPermission("tps.freeze"))
                        .executes(ctx -> {
                            ServerTickManager serverTickManager = Bukkit.getServerTickManager();
                            if (serverTickManager.isFrozen()) {
                                ctx.getSource().getSender().sendMessage(MessageHelper.getMessage("messages.tpsAlreadyFrozen", plugin));
                                return Command.SINGLE_SUCCESS;
                            }
                            serverTickManager.setFrozen(true);
                            if (plugin.getConfig().getBoolean("broadcastChanges")) {
                                Bukkit.broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(
                                        returnFormatted(MessageHelper.getMessage("messages.tpsFreeze", plugin), ctx.getSource().getSender()))
                                );
                            } else {
                                ctx.getSource().getSender().sendMessage(
                                        returnFormatted(MessageHelper.getMessage("messages.tpsFreeze", plugin), plugin.getConfig().getString("yourselfString"))
                                );
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("unfreeze")
                        .requires(sender -> sender.getSender().hasPermission("tps.unfreeze"))
                        .executes(ctx -> {
                            ServerTickManager serverTickManager = Bukkit.getServerTickManager();
                            if (!serverTickManager.isFrozen()) {
                                ctx.getSource().getSender().sendMessage(MessageHelper.getMessage("messages.tpsAlreadyUnfrozen", plugin));
                                return Command.SINGLE_SUCCESS;
                            }
                            serverTickManager.setFrozen(false);
                            if (plugin.getConfig().getBoolean("broadcastChanges")) {
                                Bukkit.broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(
                                    returnFormatted(MessageHelper.getMessage("messages.tpsUnfreeze", plugin), ctx.getSource().getSender()))
                                );
                            } else {
                                ctx.getSource().getSender().sendMessage(
                                        returnFormatted(MessageHelper.getMessage("messages.tpsUnfreeze", plugin), plugin.getConfig().getString("yourselfString"))
                                );
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("set")
                        .requires(sender -> sender.getSender().hasPermission("tps.set"))
                        .then(Commands.argument("tps", LongArgumentType.longArg(0))
                                .executes(ctx -> {
                                    long newTps = LongArgumentType.getLong(ctx, "tps");
                                    long maxTps = getMaxTickPerm(ctx.getSource().getSender(), "tps.set.", plugin);
                                    if (newTps > maxTps) {
                                        ctx.getSource().getSender().sendMessage(MessageHelper.getMessage("messages.tpsAboveValid", plugin)
                                                .replace("%max%", String.valueOf(maxTps)));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    Bukkit.getServerTickManager().setTickRate(newTps);
                                    if (plugin.getConfig().getBoolean("broadcastChanges")) {
                                        Bukkit.broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(
                                                returnFormatted(MessageHelper.getMessage("messages.tpsSet", plugin), ctx.getSource().getSender(), newTps))
                                        );
                                    } else {
                                        ctx.getSource().getSender().sendMessage(
                                                returnFormatted(MessageHelper.getMessage("messages.tpsSet", plugin),
                                                        plugin.getConfig().getString("yourselfString")).replace("%max%", String.valueOf(maxTps))
                                        );
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                ))
                .then(Commands.literal("reset")
                        .requires(sender -> sender.getSender().hasPermission("tps.reset"))
                        .executes(ctx -> {
                            Bukkit.getServerTickManager().setFrozen(false);
                            Bukkit.getServerTickManager().setTickRate(20);
                            if (plugin.getConfig().getBoolean("broadcastChanges")) {
                                Bukkit.broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(
                                        returnFormatted(MessageHelper.getMessage("messages.tpsReset", plugin), ctx.getSource().getSender()))
                                );
                            } else {
                                ctx.getSource().getSender().sendMessage(
                                        returnFormatted(MessageHelper.getMessage("messages.tpsReset", plugin), plugin.getConfig().getString("yourselfString"))
                                );
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("step")
                        .requires(sender -> sender.getSender().hasPermission("tps.step"))
                        .then(Commands.argument("tick", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    int newTps = IntegerArgumentType.getInteger(ctx, "tick");
                                    long maxTps = getMaxTickPerm(ctx.getSource().getSender(), "tps.step.", plugin);
                                    if (newTps > maxTps) {
                                        ctx.getSource().getSender().sendMessage(MessageHelper.getMessage("messages.tpsAboveValid", plugin)
                                                .replace("%max%", String.valueOf(maxTps)));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    if (!Bukkit.getServerTickManager().isFrozen()) {
                                        ctx.getSource().getSender().sendMessage(MessageHelper.getMessage("messages.tpsNotFrozen", plugin));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    Bukkit.getServerTickManager().stepGameIfFrozen(newTps);
                                    if (plugin.getConfig().getBoolean("broadcastChanges")) {
                                        Bukkit.broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(
                                                returnFormatted(MessageHelper.getMessage("messages.tpsStep", plugin), ctx.getSource().getSender(), newTps))
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    ctx.getSource().getSender().sendMessage(
                                            returnFormatted(MessageHelper.getMessage("messages.tpsStep", plugin),
                                                    plugin.getConfig().getString("yourselfString")).replace("%max%", String.valueOf(maxTps))
                                    );
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                );
                return tpsBuilder.build();
    }

    private static String returnFormatted(String unformatted, DoubleStatistic<StatisticWindow.TicksPerSecond> tps) {
        return unformatted.replace("%5s%", String.format(Locale.US, "%.1f", tps.poll(StatisticWindow.TicksPerSecond.SECONDS_5)))
                .replace("%10s%", String.format(Locale.US, "%.1f", tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10)))
                .replace("%1m%", String.format(Locale.US, "%.1f", tps.poll(StatisticWindow.TicksPerSecond.MINUTES_1)))
                .replace("%5m%", String.format(Locale.US, "%.1f", tps.poll(StatisticWindow.TicksPerSecond.MINUTES_5)))
                .replace("%15m%", String.format(Locale.US, "%.1f", tps.poll(StatisticWindow.TicksPerSecond.MINUTES_15)));
    }
    private static String returnFormatted(String unformatted, CommandSender sender) {
        return unformatted.replace("%player%", sender.getName());
    }
    private static String returnFormatted(String unformatted, String sender) {
        return unformatted.replace("%player%", sender);
    }
    private static String returnFormatted(String unformatted, CommandSender sender, long newTps) {
        return unformatted.replace("%player%", sender.getName())
                .replace("%tps%", String.valueOf(newTps));
    }

    private static long getMaxTickPerm(CommandSender sender, String permPath, JavaPlugin plugin) {
        long max = plugin.getConfig().getLong("maxTps");
        for (String permission : sender.getEffectivePermissions().stream().map(p -> p.getPermission()).toList()) {
            if (permission.startsWith(permPath)) {
                String[] parts = permission.split("\\.");
                if (parts.length != 3) { return max; }
                if (parts[2].equals("*")) { return max; }
                try {
                    return Long.parseLong(parts[2]);
                } catch (NumberFormatException e) {
                    return max;
                }
            }
        }
        return max;
    }
}