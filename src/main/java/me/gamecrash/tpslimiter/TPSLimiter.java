package me.gamecrash.tpslimiter;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class TPSLimiter extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->
                commands.registrar().register(TPSCommand.build()));
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
