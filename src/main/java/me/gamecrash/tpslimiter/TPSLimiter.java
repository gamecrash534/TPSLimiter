package me.gamecrash.tpslimiter;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class TPSLimiter extends JavaPlugin {
    FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        config = getConfig();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->
                commands.registrar().register(TPSCommand.build(this)));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reloadConf() {
        saveDefaultConfig();
        reloadConfig();
    }
}
