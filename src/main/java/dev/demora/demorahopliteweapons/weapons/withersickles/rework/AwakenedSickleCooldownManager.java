package dev.demora.demorahopliteweapons.weapons.withersickles.rework;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class AwakenedSickleCooldownManager {
    private final DemoraHopliteWeapons plugin;
    private final Map<UUID, Long> throwCooldowns = new HashMap<>();
    public AwakenedSickleCooldownManager(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
    }
    
    public boolean isOnCooldown(Player player) {
        Long end = throwCooldowns.get(player.getUniqueId());
        if (end == null) {
            return false;
        }
        if (System.currentTimeMillis() >= end) {
            throwCooldowns.remove(player.getUniqueId());
            return false;
        }
        return true;
    }
    
    public long getRemainingCooldown(Player player) {
        Long end = throwCooldowns.get(player.getUniqueId());
        if (end == null) {
            return 0;
        }
        long diff = end - System.currentTimeMillis();
        if (diff <= 0) {
            throwCooldowns.remove(player.getUniqueId());
            return 0;
        }
        return (long) Math.ceil(diff / 1000.0);
    }
    
    public void setCooldown(Player player) {
        long cooldownTime = plugin.getConfig().getInt("weapons.awakened_wither_sickles.throw_cooldown_seconds", 12) * 1000L;
        throwCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownTime);
    }
    
    public long getCooldownEndTime(Player player) {
        Long end = throwCooldowns.get(player.getUniqueId());
        if (end == null) {
            long cooldownTime = plugin.getConfig().getInt("weapons.awakened_wither_sickles.throw_cooldown_seconds", 12) * 1000L;
            end = System.currentTimeMillis() + cooldownTime;
            throwCooldowns.put(player.getUniqueId(), end);
        }
        return end;
    }
    public void removePlayerData(UUID playerId) {
        throwCooldowns.remove(playerId);
    }
}

