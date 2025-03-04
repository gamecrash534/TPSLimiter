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
    private final Map<UUID, Map<Integer, Integer>> cache = new HashMap<>();

    private void cachePlayerPerms(CommandSender sender) {
        int maxStep = plugin.getConfig().getInt("maxStepCount");
        int maxTps = plugin.getConfig().getInt("maxTps");
        Map<Integer, Integer> permCache = new HashMap<>();
        for (String perm : sender.getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission).toList()) {
            if (perm.startsWith("tps.set")) {
                String value = perm.substring("tps.set.".length());
                if (value.equals("*")) value = String.valueOf(maxTps);
                permCache.put(0, Integer.parseInt(value));
            } else if (perm.startsWith("tps.step")) {
                String value = perm.substring("tps.step.".length());
                if (value.equals("*")) value = String.valueOf(maxStep);
                permCache.put(1, Integer.parseInt(value));
            }
        }
        cache.put(((Player)sender).getUniqueId(), permCache);
    }
    public int getMax(CommandSender sender, boolean isStep) {
        int maxStep = plugin.getConfig().getInt("maxStepCount");
        int maxTps = plugin.getConfig().getInt("maxTps");
        if (!cache.containsKey(((Player)sender).getUniqueId())) {
            cachePlayerPerms(sender);
        }
        Map<Integer, Integer> perms = cache.get(((Player)sender).getUniqueId());

        if (isStep) return perms.getOrDefault(1, maxStep);
        else return perms.getOrDefault(0, maxTps);
    }
    public void clearCache() {
        cache.clear();
    }
}