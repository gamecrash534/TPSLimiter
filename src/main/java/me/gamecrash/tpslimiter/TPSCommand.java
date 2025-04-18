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

import static me.gamecrash.tpslimiter.MessageHelper.*;

public class TPSCommand {

    private static final String PERMISSION_BASE = "tpslimiter";
    private static final String PERMISSION_RELOAD = PERMISSION_BASE + ".reload";
    private static final String PERMISSION_FREEZE = PERMISSION_BASE + ".freeze";
    private static final String PERMISSION_UNFREEZE = PERMISSION_BASE + ".unfreeze";
    private static final String PERMISSION_SET = PERMISSION_BASE + ".set";
    private static final String PERMISSION_FORCE = PERMISSION_BASE + ".force";
    private static final String PERMISSION_STEP = PERMISSION_BASE + ".step";
    private static final String PERMISSION_INFO = PERMISSION_BASE + ".info";
    private static final String PERMISSION_RESET = PERMISSION_BASE + ".reset";

    public static LiteralCommandNode<CommandSourceStack> build() {
        TPSLimiter plugin = TPSLimiter.getPlugin();

        return Commands.literal("tps")
                .requires(sender -> hasPermission(sender, PERMISSION_BASE))
                .executes(ctx -> returnTPS(ctx.getSource()))
                .then(buildReloadCommand(plugin))
                .then(buildFreezeCommand())
                .then(buildUnfreezeCommand())
                .then(buildSetCommand(plugin))
                .then(buildResetCommand())
                .then(buildStepCommand(plugin))
                .then(buildInfoCommand())
                .build();
    }

    private static boolean hasPermission(CommandSourceStack sender, String permission) {
        return sender.getSender().hasPermission(permission);
    }

    private static int returnTPS(CommandSourceStack source) {
        Spark spark = SparkProvider.get();
        if (spark == null) {
            source.getSender().sendRichMessage(getMessage("messages.notAvailable").replace("%arg%", "Spark API"));
            return 1;
        }
        DoubleStatistic<StatisticWindow.TicksPerSecond> tps = spark.tps();
        if (tps == null) {
            source.getSender().sendRichMessage(getMessage("messages.notAvailable").replace("%arg%", "TPS-Data"));
            return 1;
        }
        source.getSender().sendRichMessage(returnFormatted(getMessage("messages.tps"), tps));
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildReloadCommand(TPSLimiter plugin) {
        return Commands.literal("reload")
            .requires(sender -> hasPermission(sender, PERMISSION_RELOAD))
            .executes(ctx -> {
                plugin.reloadConf();
                ctx.getSource().getSender().sendRichMessage(getMessage("messages.reload"));
                return 1;
            });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildFreezeCommand() {
        return Commands.literal("freeze")
            .requires(sender -> hasPermission(sender, PERMISSION_FREEZE))
            .executes(ctx -> {
                ServerTickManager serverTickManager = Bukkit.getServerTickManager();
                if (serverTickManager.isFrozen()) {
                    ctx.getSource().getSender().sendRichMessage(getMessage("messages.tpsAlreadyFrozen"));
                    return 1;
                }
                serverTickManager.setFrozen(true);
                broadcastMessage(getMessage("messages.tpsFreeze"), ctx.getSource().getSender());
                return 1;
            });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildUnfreezeCommand() {
        return Commands.literal("unfreeze")
            .requires(sender -> hasPermission(sender, PERMISSION_UNFREEZE))
            .executes(ctx -> {
                ServerTickManager serverTickManager = Bukkit.getServerTickManager();
                if (!serverTickManager.isFrozen()) {
                    ctx.getSource().getSender().sendRichMessage(getMessage("messages.tpsAlreadyUnfrozen"));
                    return 1;
                }
                serverTickManager.setFrozen(false);
                broadcastMessage(getMessage("messages.tpsUnfreeze"), ctx.getSource().getSender());
                return 1;
            });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildSetCommand(TPSLimiter plugin) {
        return Commands.literal("set")
            .requires(sender -> hasPermission(sender, PERMISSION_SET))
            .then(Commands.argument("tps", IntegerArgumentType.integer(1))
                .executes(ctx -> {
                    int newTps = IntegerArgumentType.getInteger(ctx, "tps");
                    int maxTps = plugin.permCache.getMax(ctx.getSource().getSender(), false);
                    if (newTps > maxTps) {
                        ctx.getSource().getSender().sendRichMessage(getMessage("messages.tpsAboveValid")
                                .replace("%max%", String.valueOf(maxTps)));
                        return 1;
                    }
                    Bukkit.getServerTickManager().setTickRate(newTps);
                    broadcastMessage(returnFormatted(getMessage("messages.tpsSet"), newTps), ctx.getSource().getSender());
                    return 1;
                })
                .then(Commands.literal("-f")
                    .requires(sender -> hasPermission(sender, PERMISSION_FORCE))
                    .executes(ctx -> {
                        int newTps = IntegerArgumentType.getInteger(ctx, "tps");
                        Bukkit.getServerTickManager().setTickRate(newTps);
                        broadcastMessage(returnFormatted(getMessage("messages.tpsSet"), newTps), ctx.getSource().getSender());
                        return 1;
                    })
                )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildResetCommand() {
        return Commands.literal("reset")
            .requires(sender -> hasPermission(sender, PERMISSION_RESET))
            .executes(ctx -> {
                Bukkit.getServerTickManager().setFrozen(false);
                Bukkit.getServerTickManager().setTickRate(20);
                broadcastMessage(getMessage("messages.tpsReset"), ctx.getSource().getSender());
                return 1;
            });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildStepCommand(TPSLimiter plugin) {
        return Commands.literal("step")
            .requires(sender -> hasPermission(sender, PERMISSION_STEP))
            .executes(ctx -> {
                if (!Bukkit.getServerTickManager().isFrozen()) {
                    ctx.getSource().getSender().sendRichMessage(getMessage("messages.tpsNotFrozen"));
                    return 1;
                }
                Bukkit.getServerTickManager().stepGameIfFrozen(1);
                broadcastMessage(returnFormatted(getMessage("messages.tpsStep"), 1), ctx.getSource().getSender());
                return 1;
            })
            .then(Commands.argument("tick", IntegerArgumentType.integer(1))
                .executes(ctx -> {
                    if (!Bukkit.getServerTickManager().isFrozen()) {
                        ctx.getSource().getSender().sendRichMessage(getMessage("messages.tpsNotFrozen"));
                        return 1;
                    }
                    int newTps = IntegerArgumentType.getInteger(ctx, "tick");
                    int maxTps = plugin.permCache.getMax(ctx.getSource().getSender(), true);
                    if (newTps > maxTps) {
                        ctx.getSource().getSender().sendRichMessage(getMessage("messages.tpsAboveValid")
                                .replace("%max%", String.valueOf(maxTps)));
                        return 1;
                    }
                    Bukkit.getServerTickManager().stepGameIfFrozen(newTps);
                    broadcastMessage(returnFormatted(getMessage("messages.tpsStep"), newTps), ctx.getSource().getSender());
                    return 1;
                })
                .then(Commands.literal("-f")
                    .requires(sender -> hasPermission(sender, PERMISSION_FORCE))
                    .executes(ctx -> {
                        if (!Bukkit.getServerTickManager().isFrozen()) {
                            ctx.getSource().getSender().sendRichMessage(getMessage("messages.tpsNotFrozen"));
                            return 1;
                        }
                        int newTps = IntegerArgumentType.getInteger(ctx, "tick");
                        Bukkit.getServerTickManager().stepGameIfFrozen(newTps);
                        broadcastMessage(returnFormatted(getMessage("messages.tpsStep"), newTps), ctx.getSource().getSender());
                        return 1;
                    })
                )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildInfoCommand() {
        return Commands.literal("info")
            .requires(sender -> hasPermission(sender, PERMISSION_INFO))
            .executes(ctx -> {
                ctx.getSource().getSender().sendRichMessage(returnFormatted(getMessage("messages.tpsInfo")));
                return 1;
            });
    }
}