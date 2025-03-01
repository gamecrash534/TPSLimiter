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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachmentInfo;

import static me.gamecrash.tpslimiter.MessageHelper.returnFormatted;


public class TPSCommand {
    public static LiteralCommandNode<CommandSourceStack> build() {
        TPSLimiter plugin = (TPSLimiter)Bukkit.getPluginManager().getPlugin("TPSLimiter");
        LiteralArgumentBuilder<CommandSourceStack> tpsBuilder = Commands.literal("tps")
                .requires(sender -> sender.getSender().hasPermission("tps"))
                .executes(ctx -> {
                    Spark spark = SparkProvider.get();
                    DoubleStatistic<StatisticWindow.TicksPerSecond> tps = spark.tps();
                    assert tps != null;
                    ctx.getSource().getSender().sendRichMessage(returnFormatted(MessageHelper.getMessage("messages.tps"), tps));
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("reload")
                    .requires(sender -> sender.getSender().hasPermission("tps.reload"))
                    .executes(ctx -> {
                        plugin.reloadConf();
                        ctx.getSource().getSender().sendRichMessage(MessageHelper.getMessage("messages.reload"));
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(Commands.literal("freeze")
                        .requires(sender -> sender.getSender().hasPermission("tps.freeze"))
                        .executes(ctx -> {
                            ServerTickManager serverTickManager = Bukkit.getServerTickManager();
                            if (serverTickManager.isFrozen()) {
                                ctx.getSource().getSender().sendRichMessage(MessageHelper.getMessage("messages.tpsAlreadyFrozen"));
                                return Command.SINGLE_SUCCESS;
                            }
                            serverTickManager.setFrozen(true);
                            if (plugin.getConfig().getBoolean("broadcastChanges")) {
                                Bukkit.broadcast(MiniMessage.miniMessage().deserialize(
                                        returnFormatted(MessageHelper.getMessage("messages.tpsFreeze"), ctx.getSource().getSender()))
                                );
                            } else {
                                ctx.getSource().getSender().sendRichMessage(
                                        returnFormatted(MessageHelper.getMessage("messages.tpsFreeze"), plugin.getConfig().getString("yourselfString"))
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
                                ctx.getSource().getSender().sendRichMessage(MessageHelper.getMessage("messages.tpsAlreadyUnfrozen"));
                                return Command.SINGLE_SUCCESS;
                            }
                            serverTickManager.setFrozen(false);
                            if (plugin.getConfig().getBoolean("broadcastChanges")) {
                                Bukkit.broadcast(MiniMessage.miniMessage().deserialize(
                                    returnFormatted(MessageHelper.getMessage("messages.tpsUnfreeze"), ctx.getSource().getSender()))
                                );
                            } else {
                                ctx.getSource().getSender().sendRichMessage(
                                        returnFormatted(MessageHelper.getMessage("messages.tpsUnfreeze"), plugin.getConfig().getString("yourselfString"))
                                );
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("set")
                        .requires(sender -> sender.getSender().hasPermission("tps.set"))
                        .then(Commands.argument("tps", LongArgumentType.longArg(1))
                                .executes(ctx -> {
                                    long newTps = LongArgumentType.getLong(ctx, "tps");
                                    long maxTps = getMaxTickPerm(ctx.getSource().getSender(), "tps.set.", plugin.getConfig().getLong("maxTps"));
                                    if (newTps > maxTps) {
                                        ctx.getSource().getSender().sendRichMessage(MessageHelper.getMessage("messages.tpsAboveValid")
                                                .replace("%max%", String.valueOf(maxTps)));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    Bukkit.getServerTickManager().setTickRate(newTps);
                                    if (plugin.getConfig().getBoolean("broadcastChanges")) {
                                        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(
                                                returnFormatted(MessageHelper.getMessage("messages.tpsSet"), ctx.getSource().getSender(), newTps))
                                        );
                                    } else {
                                        ctx.getSource().getSender().sendRichMessage(
                                                returnFormatted(MessageHelper.getMessage("messages.tpsSet"),
                                                        plugin.getConfig().getString("yourselfString")).replace("%tps%", String.valueOf(newTps))
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
                                Bukkit.broadcast(MiniMessage.miniMessage().deserialize(
                                        returnFormatted(MessageHelper.getMessage("messages.tpsReset"), ctx.getSource().getSender()))
                                );
                            } else {
                                ctx.getSource().getSender().sendRichMessage(
                                        returnFormatted(MessageHelper.getMessage("messages.tpsReset"), plugin.getConfig().getString("yourselfString"))
                                );
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("step")
                        .requires(sender -> sender.getSender().hasPermission("tps.step"))
                        .then(Commands.argument("tick", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    int newTps = IntegerArgumentType.getInteger(ctx, "tick");
                                    long maxTps = getMaxTickPerm(ctx.getSource().getSender(), "tps.step.", plugin.getConfig().getLong("maxStepCount"));
                                    if (newTps > maxTps) {
                                        ctx.getSource().getSender().sendRichMessage(MessageHelper.getMessage("messages.tpsAboveValid")
                                                .replace("%max%", String.valueOf(maxTps)));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    if (!Bukkit.getServerTickManager().isFrozen()) {
                                        ctx.getSource().getSender().sendRichMessage(MessageHelper.getMessage("messages.tpsNotFrozen"));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    Bukkit.getServerTickManager().stepGameIfFrozen(newTps);
                                    if (plugin.getConfig().getBoolean("broadcastChanges")) {
                                        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(
                                                returnFormatted(MessageHelper.getMessage("messages.tpsStep"), ctx.getSource().getSender(), newTps))
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    ctx.getSource().getSender().sendRichMessage(
                                            returnFormatted(MessageHelper.getMessage("messages.tpsStep"),
                                                    plugin.getConfig().getString("yourselfString")).replace("%max%", String.valueOf(maxTps))
                                    );
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                );
                return tpsBuilder.build();
    }
    private static long getMaxTickPerm(CommandSender sender, String permPath, long max) {
        for (String permission : sender.getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission).toList()) {
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