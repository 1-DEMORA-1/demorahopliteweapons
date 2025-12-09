package dev.demora.demorahopliteweapons.weapons.withersickles.rework;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.util.*;
public class AwakenedSickleShieldManager {
    private final DemoraHopliteWeapons plugin;
    private final Map<UUID, Long> shieldCooldowns = new HashMap<>();
    private final Map<UUID, Boolean> activeShields = new HashMap<>();
    private final Map<UUID, BukkitRunnable> shieldTasks = new HashMap<>();
    private FileConfiguration reworkConfig;
    public AwakenedSickleShieldManager(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        loadReworkConfig();
    }
    
    private void loadReworkConfig() {
        File reworkFile = new File(plugin.getDataFolder(), "rework.yml");
        reworkConfig = YamlConfiguration.loadConfiguration(reworkFile);
    }
    
    public void reloadConfig() {
        loadReworkConfig();
    }
    public void activateShield(Player player) {
        if (isOnCooldown(player)) {
            long remaining = getRemainingCooldown(player);
            player.sendMessage("§cЩит серпов перезаряжается! Осталось: §e" + remaining + " §cсек");
            return;
        }
        
        if (isShieldActive(player)) {
            return;
        }
        
        int duration = reworkConfig.getInt("awakened_wither_sickles.shield_duration_seconds", 10);
        int flameCount = reworkConfig.getInt("awakened_wither_sickles.shield_flame_count", 6);
        double radius = reworkConfig.getDouble("awakened_wither_sickles.shield_radius", 2.5);
        activeShields.put(player.getUniqueId(), true);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 1.0f, 1.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 0.8f);
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        BukkitRunnable task = new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = duration * 20;
            @Override
            public void run() {
                if (!player.isOnline() || ticks >= maxTicks || !activeShields.getOrDefault(player.getUniqueId(), false)) {
                    deactivateShield(player);
                    cancel();
                    return;
                }
                Location center = player.getLocation().add(0, 1, 0);
                double angle = (ticks * 0.15);
                for (int i = 0; i < flameCount; i++) {
                    double currentAngle = angle + (2 * Math.PI * i / flameCount);
                    double x = Math.cos(currentAngle) * radius;
                    double z = Math.sin(currentAngle) * radius;
                    Location flameLoc = center.clone().add(x, 0, z);
                    player.getWorld().spawnParticle(Particle.FLAME, flameLoc, 3, 0.1, 0.1, 0.1, 0.02);
                    player.getWorld().spawnParticle(Particle.LAVA, flameLoc, 1, 0.05, 0.05, 0.05, 0);
                }
                
                ticks++;
            }
        };
        
        task.runTaskTimer(plugin, 0L, 1L);
        shieldTasks.put(player.getUniqueId(), task);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int cooldownSeconds = reworkConfig.getInt("awakened_wither_sickles.shield_cooldown_seconds", 30);
            shieldCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownSeconds * 1000L));
        }, duration * 20L);
    }
    
    public void deactivateShield(Player player) {
        activeShields.remove(player.getUniqueId());
        BukkitRunnable task = shieldTasks.remove(player.getUniqueId());
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        
        if (player.isOnline()) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 1.0f);
        }
    }
    
    public boolean isShieldActive(Player player) {
        return activeShields.getOrDefault(player.getUniqueId(), false);
    }
    public double getDamageReduction() {
        return reworkConfig.getDouble("awakened_wither_sickles.shield_damage_reduction", 0.2);
    }
    
    private boolean isOnCooldown(Player player) {
        Long end = shieldCooldowns.get(player.getUniqueId());
        if (end == null) {
            return false;
        }
        if (System.currentTimeMillis() >= end) {
            shieldCooldowns.remove(player.getUniqueId());
            return false;
        }
        return true;
    }
    
    private long getRemainingCooldown(Player player) {
        Long end = shieldCooldowns.get(player.getUniqueId());
        if (end == null) {
            return 0;
        }
        long diff = end - System.currentTimeMillis();
        if (diff <= 0) {
            shieldCooldowns.remove(player.getUniqueId());
            return 0;
        }
        return (long) Math.ceil(diff / 1000.0);
    }
    
    public void removePlayerData(UUID playerId) {
        activeShields.remove(playerId);
        shieldCooldowns.remove(playerId);
        BukkitRunnable task = shieldTasks.remove(playerId);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
}

