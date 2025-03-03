package me.gamecrash.tpslimiter;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.plugin.java.JavaPlugin;

public final class TPSLimiter extends JavaPlugin {
    public PermCache permCache;
    public ComponentLogger logger = this.getComponentLogger();

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        permCache = new PermCache();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->
                commands.registrar().register(TPSCommand.build()));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reloadConf() {
        saveDefaultConfig();
        permCache.clearCache();
        reloadConfig();
    }
}
