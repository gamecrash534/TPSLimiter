package me.gamecrash.tpslimiter;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class LPEventListener {
    private final TPSLimiter plugin = (TPSLimiter)Bukkit.getPluginManager().getPlugin("TPSLimiter");
    private  LuckPerms luckPerms;

    public void register(LuckPerms lp) {
        luckPerms = lp;
        luckPerms.getEventBus().subscribe(plugin, UserDataRecalculateEvent.class, this::listener);
    }

    private void listener(UserDataRecalculateEvent e) {
        UUID uuid = e.getUser().getUniqueId();
        CommandSender sender = Bukkit.getPlayer(uuid);
        if (!plugin.permCache.isUserCached(uuid)) return;
        plugin.permCache.clearCachedUser(uuid);
        plugin.permCache.cachePlayerPerms(sender);
    }
}
