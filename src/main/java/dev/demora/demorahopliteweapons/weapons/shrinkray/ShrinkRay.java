package dev.demora.demorahopliteweapons.weapons.shrinkray;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class ShrinkRay {
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey shrinkRayKey;
    private final Map<UUID, Long> shrinkEffects;
    private final Map<UUID, Long> enlargeEffects;
    private final Map<UUID, Long> cooldowns;
    
    public ShrinkRay(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.shrinkRayKey = new NamespacedKey(plugin, "shrink_ray");
        this.shrinkEffects = new HashMap<>();
        this.enlargeEffects = new HashMap<>();
        this.cooldowns = new HashMap<>();
    }
    public ItemStack createShrinkRay() {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();
        
        meta.setDisplayName("§9§lУменьшающий луч");
        meta.setCustomModelData(12);
        meta.setUnbreakable(true);
        
        meta.getPersistentDataContainer().set(shrinkRayKey, PersistentDataType.BYTE, (byte) 1);
        
        bow.setItemMeta(meta);
        return bow;
    }
    
    public boolean isShrinkRay(ItemStack item) {
        if (item == null || item.getType() != Material.BOW) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(shrinkRayKey, PersistentDataType.BYTE);
    }
    
    public boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        return cooldowns.containsKey(playerId) && System.currentTimeMillis() < cooldowns.get(playerId);
    }
    
    public long getRemainingCooldown(Player player) {
        if (!isOnCooldown(player)) return 0;
        UUID playerId = player.getUniqueId();
        return (cooldowns.get(playerId) - System.currentTimeMillis()) / 1000;
    }
    
    public void shrinkPlayer(Player player) {
        if (isOnCooldown(player)) {
            long remaining = getRemainingCooldown(player);
            player.sendMessage("§cУменьшающий луч перезаряжается! Осталось: §e" + remaining + " §cсек");
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        if (shrinkEffects.containsKey(playerId)) {
            long remaining = (shrinkEffects.get(playerId) - System.currentTimeMillis()) / 1000;
            player.sendMessage("§cВы уже уменьшены! Осталось: §e" + remaining + " §cсек");
            return;
        }
        
        if (enlargeEffects.containsKey(playerId)) {
            player.sendMessage("§cВы не можете уменьшиться пока увеличены!");
            return;
        }
        
        AttributeInstance scaleAttr = player.getAttribute(Attribute.GENERIC_SCALE);
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(0.5);
        }
        
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 2.0f);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0);
        
        int duration = plugin.getConfig().getInt("weapons.shrink_ray.shrink_duration", 12);
        long endTime = System.currentTimeMillis() + (duration * 1000L);
        shrinkEffects.put(playerId, endTime);
        
        int cooldown = plugin.getConfig().getInt("weapons.shrink_ray.cooldown", 20);
        setCooldown(player, cooldown);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() >= endTime) {
                    resetPlayerSize(player);
                    shrinkEffects.remove(playerId);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }
    
    public void shootRay(Player shooter) {
        if (isOnCooldown(shooter)) {
            long remaining = getRemainingCooldown(shooter);
            shooter.sendMessage("§cУменьшающий луч перезаряжается! Осталось: §e" + remaining + " §cсек");
            return;
        }
        
        Location eyeLoc = shooter.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        World world = shooter.getWorld();
        
        double raySpeed = plugin.getConfig().getDouble("weapons.shrink_ray.ray_speed", 0.5);
        int cooldown = plugin.getConfig().getInt("weapons.shrink_ray.cooldown", 20);
        
        world.playSound(eyeLoc, Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 1.0f, 1.5f);
        
        final boolean[] hitTarget = {false};
        
        new BukkitRunnable() {
            Location currentLoc = eyeLoc.clone();
            double distance = 0;
            final double maxDistance = 50;
            final double step = raySpeed;
            
            @Override
            public void run() {
                if (distance >= maxDistance) {
                    if (!hitTarget[0]) {
                        setCooldown(shooter, 5);
                    }
                    cancel();
                    return;
                }
                
                currentLoc.add(direction.clone().multiply(step));
                distance += step;
                
                world.spawnParticle(Particle.HAPPY_VILLAGER, currentLoc, 2, 0, 0, 0, 0);
                world.spawnParticle(Particle.COMPOSTER, currentLoc, 1, 0, 0, 0, 0);
                
                RayTraceResult result = world.rayTraceEntities(currentLoc, direction, step, 
                    entity -> entity instanceof LivingEntity && entity != shooter);
                
                if (result != null && result.getHitEntity() != null) {
                    Entity hitEntity = result.getHitEntity();
                    if (hitEntity instanceof LivingEntity) {
                        hitTarget[0] = true;
                        enlargeEntity((LivingEntity) hitEntity);
                        setCooldown(shooter, cooldown);
                        cancel();
                        return;
                    }
                }
                if (currentLoc.getBlock().getType().isSolid()) {
                    world.spawnParticle(Particle.HAPPY_VILLAGER, currentLoc, 30, 0.2, 0.2, 0.2, 0);
                    world.playSound(currentLoc, Sound.BLOCK_GLASS_BREAK, 0.5f, 1.5f);
                    if (!hitTarget[0]) {
                        setCooldown(shooter, 5);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
    
    private void setCooldown(Player player, int seconds) {
        UUID playerId = player.getUniqueId();
        cooldowns.put(playerId, System.currentTimeMillis() + (seconds * 1000L));
    }
    
    private void enlargeEntity(LivingEntity entity) {
        UUID entityId = entity.getUniqueId();
        
        if (enlargeEffects.containsKey(entityId)) {
            return;
        }
        
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (shrinkEffects.containsKey(entityId)) {
                player.sendMessage("§cВы не можете быть увеличены пока уменьшены!");
                return;
            }
        }
        AttributeInstance scaleAttr = entity.getAttribute(Attribute.GENERIC_SCALE);
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(2.0);
        }
        
        entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.5f);
        entity.getWorld().spawnParticle(Particle.COMPOSTER, entity.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0);
        
        int duration = plugin.getConfig().getInt("weapons.shrink_ray.enlarge_duration", 12);
        long endTime = System.currentTimeMillis() + (duration * 1000L);
        enlargeEffects.put(entityId, endTime);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() >= endTime) {
                    resetEntitySize(entity);
                    enlargeEffects.remove(entityId);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }
    
    private void resetPlayerSize(Player player) {
        AttributeInstance scaleAttr = player.getAttribute(Attribute.GENERIC_SCALE);
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(1.0);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
    }
    
    private void resetEntitySize(LivingEntity entity) {
        AttributeInstance scaleAttr = entity.getAttribute(Attribute.GENERIC_SCALE);
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(1.0);
        }
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
    }
    
    public void removePlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        shrinkEffects.remove(playerId);
        enlargeEffects.remove(playerId);
        cooldowns.remove(playerId);
        resetPlayerSize(player);
    }
}

