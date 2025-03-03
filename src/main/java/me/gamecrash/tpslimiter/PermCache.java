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
    private final Map<UUID, Map<Long, Long>> cache = new HashMap<>();

    private void cachePlayerPerms(CommandSender sender) {
        long maxStep = plugin.getConfig().getLong("maxStepCount");
        long maxTps = plugin.getConfig().getLong("maxTps");
        Map<Long, Long> permCache = new HashMap<>();
        for (String perm : sender.getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission).toList()) {
            if (perm.startsWith("tps.set")) {
                String value = perm.substring("tps.set.".length());
                if (value.equals("*")) value = String.valueOf(maxTps);
                permCache.put(0L, Long.parseLong(value));
            } else if (perm.startsWith("tps.step")) {
                String value = perm.substring("tps.step.".length());
                if (value.equals("*")) value = String.valueOf(maxStep);
                permCache.put(1L, Long.parseLong(value));
            }
        }
        cache.put(((Player)sender).getUniqueId(), permCache);

    }
    public long getMax(CommandSender sender, boolean isStep) {
        long maxStep = plugin.getConfig().getLong("maxStepCount");
        long maxTps = plugin.getConfig().getLong("maxTps");
        if (!cache.containsKey(((Player)sender).getUniqueId())) {
            cachePlayerPerms(sender);
        }
        Map<Long, Long> perms = cache.get(((Player)sender).getUniqueId());

        if (isStep) return perms.getOrDefault(1L, maxStep);
        else return perms.getOrDefault(0L, maxTps);
    }
    public void clearCache() {
        cache.clear();
    }
}