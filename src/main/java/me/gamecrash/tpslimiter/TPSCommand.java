package me.gamecrash.tpslimiter;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import org.bukkit.Bukkit;
import org.bukkit.ServerTickManager;

import static me.gamecrash.tpslimiter.MessageHelper.returnFormatted;
import static me.gamecrash.tpslimiter.MessageHelper.broadcastMessage;


public class TPSCommand {
    public static LiteralCommandNode<CommandSourceStack> build() {
        TPSLimiter plugin = TPSLimiter.getPlugin();
        LiteralArgumentBuilder<CommandSourceStack> tpsBuilder = Commands.literal("tps")
                .requires(sender -> sender.getSender().hasPermission("tpslimiter"))
                .executes(ctx -> {
                    Spark spark = SparkProvider.get();
                    DoubleStatistic<StatisticWindow.TicksPerSecond> tps = spark.tps();
                    assert tps != null;
                    ctx.getSource().getSender().sendRichMessage(returnFormatted(MessageHelper.getMessage("messages.tps"), tps));
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("reload")
                    .requires(sender -> sender.getSender().hasPermission("tpslimiter.reload"))
                    .executes(ctx -> {
                        plugin.reloadConf();
                        ctx.getSource().getSender().sendRichMessage(MessageHelper.getMessage("messages.reload"));
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(Commands.literal("freeze")
                    .requires(sender -> sender.getSender().hasPermission("tpslimiter.freeze"))
                    .executes(ctx -> {
                        ServerTickManager serverTickManager = Bukkit.getServerTickManager();
                        if (serverTickManager.isFrozen()) {
                            ctx.getSource().getSender().sendRichMessage(MessageHelper.getMessage("messages.tpsAlreadyFrozen"));
                            return Command.SINGLE_SUCCESS;
                        }
                        serverTickManager.setFrozen(true);
                        broadcastMessage(returnFormatted(MessageHelper.getMessage("messages.tpsFreeze")), ctx.getSource().getSender());

                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(Commands.literal("unfreeze")
                    .requires(sender -> sender.getSender().hasPermission("tpslimiter.unfreeze"))
                    .executes(ctx -> {
                        ServerTickManager serverTickManager = Bukkit.getServerTickManager();
                        if (!serverTickManager.isFrozen()) {
                            ctx.getSource().getSender().sendRichMessage(MessageHelper.getMessage("messages.tpsAlreadyUnfrozen"));
                            return Command.SINGLE_SUCCESS;
                        }
                        serverTickManager.setFrozen(false);
                        broadcastMessage(returnFormatted(MessageHelper.getMessage("messages.tpsUnfreeze")), ctx.getSource().getSender());

                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(Commands.literal("set")
                    .requires(sender -> sender.getSender().hasPermission("tpslimiter.set"))
                    .then(Commands.argument("tps", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int newTps = IntegerArgumentType.getInteger(ctx, "tps");
                            int maxTps = plugin.permCache.getMax(ctx.getSource().getSender(), false);
                            if (newTps > maxTps) {
                                ctx.getSource().getSender().sendRichMessage(MessageHelper.getMessage("messages.tpsAboveValid")
                                        .replace("%max%", String.valueOf(maxTps)));
                                return Command.SINGLE_SUCCESS;
                            }
                            Bukkit.getServerTickManager().setTickRate(newTps);
                            broadcastMessage(returnFormatted(MessageHelper.getMessage("messages.tpsSet"), newTps), ctx.getSource().getSender());

                            return Command.SINGLE_SUCCESS;
                        })
                ))
                .then(Commands.literal("reset")
                    .requires(sender -> sender.getSender().hasPermission("tpslimiter.reset"))
                    .executes(ctx -> {
                        Bukkit.getServerTickManager().setFrozen(false);
                        Bukkit.getServerTickManager().setTickRate(20);

                        broadcastMessage(returnFormatted(MessageHelper.getMessage("messages.tpsReset")), ctx.getSource().getSender());
                        return Command.SINGLE_SUCCESS;
                    })
                )
                .then(Commands.literal("step")
                    .requires(sender -> sender.getSender().hasPermission("tpslimiter.step"))
                    .executes(ctx -> {
                        if (!Bukkit.getServerTickManager().isFrozen()) {
                            ctx.getSource().getSender().sendRichMessage(MessageHelper.getMessage("messages.tpsNotFrozen"));
                            return Command.SINGLE_SUCCESS;
                        }
                        Bukkit.getServerTickManager().stepGameIfFrozen(1);
                        broadcastMessage(returnFormatted(MessageHelper.getMessage("messages.tpsStep"), 1), ctx.getSource().getSender());
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(Commands.argument("tick", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int newTps = IntegerArgumentType.getInteger(ctx, "tick");
                            int maxTps = plugin.permCache.getMax(ctx.getSource().getSender(), true);
                            if (!Bukkit.getServerTickManager().isFrozen()) {
                                ctx.getSource().getSender().sendRichMessage(MessageHelper.getMessage("messages.tpsNotFrozen"));
                                return Command.SINGLE_SUCCESS;
                            }
                            if (newTps > maxTps) {
                                ctx.getSource().getSender().sendRichMessage(MessageHelper.getMessage("messages.tpsAboveValid")
                                        .replace("%max%", String.valueOf(maxTps)));
                                return Command.SINGLE_SUCCESS;
                            }
                            Bukkit.getServerTickManager().stepGameIfFrozen(newTps);
                            broadcastMessage(returnFormatted(MessageHelper.getMessage("messages.tpsStep"), newTps), ctx.getSource().getSender());

                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
                .then(Commands.literal("info")
                    .requires(sender -> sender.getSender().hasPermission("tpslimiter.info"))
                    .executes(ctx -> {
                        ctx.getSource().getSender().sendRichMessage(returnFormatted(MessageHelper.getMessage("messages.tpsInfo")));
                        return Command.SINGLE_SUCCESS;
                    })
                );
        return tpsBuilder.build();
    }
}