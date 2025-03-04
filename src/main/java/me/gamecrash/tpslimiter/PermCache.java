package me.gamecrash.tpslimiter;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermCache {
    TPSLimiter plugin = (TPSLimiter)Bukkit.getPluginManager().getPlugin("TPSLimiter");
    private final Map<UUID, TPSProperties> cache = new HashMap<>();

    public void cachePlayerPerms(CommandSender sender) {
        int maxStep = plugin.getConfig().getInt("maxStepCount");
        int maxTps = plugin.getConfig().getInt("maxTps");
        TPSProperties permCache = new TPSProperties();
        for (String perm : sender.getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission).toList()) {
            if (perm.startsWith("tps.set")) {
                int value;
                if (perm.equals("tps.set") || perm.equals("tps.set.*")) {
                    value = maxTps;
                } else {
                    value = Integer.parseInt(perm.substring("tps.set.".length()));
                }
                permCache.maxTps = value;
            } else if (perm.startsWith("tps.step")) {
                int value;
                if (perm.equals("tps.step") || perm.equals("tps.step.*")) {
                    value = maxStep;
                } else {
                    value = Integer.parseInt(perm.substring("tps.step.".length()));
                }
                permCache.maxStepCount = value;
            }
        }
        cache.put(((Player)sender).getUniqueId(), permCache);
    }
    public int getMax(CommandSender sender, boolean isStepCommand) {
        if (!cache.containsKey(((Player)sender).getUniqueId())) {
            cachePlayerPerms(sender);
        }
        TPSProperties perms = cache.get(((Player)sender).getUniqueId());
        return isStepCommand ? perms.maxStepCount : perms.maxTps;
    }
    public void clearCache() {
        cache.clear();
    }
    public void clearCachedUser(UUID uuid) {
        cache.remove(uuid);
    }
    public boolean isUserCached(UUID uuid) {
        return cache.containsKey(uuid);
    }
}