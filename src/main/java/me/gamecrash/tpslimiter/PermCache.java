package me.gamecrash.tpslimiter;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermCache {
    TPSLimiter plugin = TPSLimiter.getPlugin();
    private final Map<UUID, TPSProperties> cache = new HashMap<>();

    public void cachePlayerPerms(CommandSender sender) {
        int maxStep = plugin.getConfig().getInt("maxStepCount");
        int maxTps = plugin.getConfig().getInt("maxTps");
        TPSProperties permCache = new TPSProperties();
        for (String perm : sender.getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission).toList()) {
            if (perm.startsWith("tpslimiter.set")) {
                int value;
                if (perm.equals("tpslimiter.set") || perm.equals("tpslimiter.set.*")) {
                    value = maxTps;
                } else {
                    value = Integer.parseInt(perm.substring("tpslimiter.set.".length()));
                }
                permCache.maxTps = value;
            } else if (perm.startsWith("tpslimiter.step")) {
                int value;
                if (perm.equals("tpslimiter.step") || perm.equals("tpslimiter.step.*")) {
                    value = maxStep;
                } else {
                    value = Integer.parseInt(perm.substring("tpslimiter.step.".length()));
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