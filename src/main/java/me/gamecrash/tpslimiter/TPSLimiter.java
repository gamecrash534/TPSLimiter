package me.gamecrash.tpslimiter;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TPSLimiter extends JavaPlugin {
    public PermCache permCache;

    @Override
    public void onEnable() {
        reloadConf();
        permCache = new PermCache();
        LuckPerms luckPerms = Bukkit.getServer().getServicesManager().load(LuckPerms.class);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->
                commands.registrar().register(TPSCommand.build()));
        new LPEventListener().register(luckPerms);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reloadConf() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        reloadConfig();
        if (permCache != null) {
            permCache.clearCache();
        }
    }

    public static TPSLimiter getPlugin() {
        return (TPSLimiter) Bukkit.getPluginManager().getPlugin("TPSLimiter");
    }
}
