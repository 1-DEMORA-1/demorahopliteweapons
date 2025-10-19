package dev.demora.demorahopliteweapons.weapons.villagerstaff;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VillagerStaff {
    
    private final DemoraHopliteWeapons plugin;
    private final Map<UUID, Long> cooldowns;
    private final Map<UUID, Long> taggedForReward;
    
    public VillagerStaff(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
        this.taggedForReward = new ConcurrentHashMap<>();
    }
    
    public ItemStack createVillagerStaff() {
        ItemStack staff = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = staff.getItemMeta();
        
        if (meta != null) {
            String name = plugin.getConfig().getString("weapons.villager_staff.name", "§6§lПосох жителя");
            meta.displayName(Component.text(name));
            int customModelData = plugin.getConfig().getInt("weapons.villager_staff.custom_model_data", 3);
            meta.setCustomModelData(customModelData);
            meta.setUnbreakable(true);
            staff.setItemMeta(meta);
        }
        
        return staff;
    }
    
    public boolean isVillagerStaff(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_AXE) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) {
            return false;
        }
        int expectedCustomModelData = plugin.getConfig().getInt("weapons.villager_staff.custom_model_data", 3);
        return meta.getCustomModelData() == expectedCustomModelData;
    }
    
    public boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        
        long cooldownTime = plugin.getConfig().getLong("weapons.villager_staff.cooldown.time", 10) * 1000;
        long lastUse = cooldowns.get(playerId);
        return System.currentTimeMillis() - lastUse < cooldownTime;
    }
    
    public long getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long cooldownTime = plugin.getConfig().getLong("weapons.villager_staff.cooldown.time", 10) * 1000;
        long lastUse = cooldowns.get(playerId);
        long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
        
        return Math.max(0, remaining / 1000L);
    }
    
    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public void useStaff(Player player) {
        Location targetLocation = getTargetLocation(player);
        if (targetLocation == null) {
            return;
        }
        
        createExplosiveItemDisplay(targetLocation);
        setCooldown(player);
    }
    
    private Location getTargetLocation(Player player) {
        RayTraceResult result = player.getWorld().rayTraceBlocks(
            player.getEyeLocation(),
            player.getEyeLocation().getDirection(),
            50.0
        );
        
        if (result != null && result.getHitBlock() != null) {
            return result.getHitPosition().toLocation(player.getWorld());
        }
        
        Location playerLoc = player.getLocation();
        Vector direction = playerLoc.getDirection().normalize().multiply(10);
        return playerLoc.add(direction);
    }
    
    private void createExplosiveItemDisplay(Location location) {
        double heightOffset = 1.5;
        Location displayLocation = location.clone().add(0, heightOffset, 0);
        ItemDisplay itemDisplay = (ItemDisplay) displayLocation.getWorld().spawnEntity(displayLocation, EntityType.ITEM_DISPLAY);
        setupItemDisplay(itemDisplay);
        scheduleGrowExplodeShrink(itemDisplay);
    }
    
    private void setupItemDisplay(ItemDisplay itemDisplay) {
        ItemStack slimeBall = createSlimeBallHelmet();
        itemDisplay.setItemStack(slimeBall);
        
        float startX = 1.0f;
        float startY = 50.0f;
        float startZ = 1.0f;
        Vector3f translation = new Vector3f(0, 0, 0);
        AxisAngle4f leftRotation = new AxisAngle4f(0, 0, 0, 1);
        Vector3f scale = new Vector3f(startX, startY, startZ);
        AxisAngle4f rightRotation = new AxisAngle4f(0, 0, 0, 1);
        Transformation transformation = new Transformation(translation, new Quaternionf(leftRotation), scale, new Quaternionf(rightRotation));
        itemDisplay.setTransformation(transformation);
        itemDisplay.setGravity(false);
        itemDisplay.setInvulnerable(true);
        itemDisplay.setBrightness(new org.bukkit.entity.Display.Brightness(15, 15));
    }
    
    private ItemStack createSlimeBallHelmet() {
        ItemStack slimeBall = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = slimeBall.getItemMeta();
        if (meta != null) {
            int customModelData = plugin.getConfig().getInt("weapons.villager_staff.slime_ball_cmd", 12);
            meta.setCustomModelData(customModelData);
            slimeBall.setItemMeta(meta);
        }
        return slimeBall;
    }
    
    private void scheduleGrowExplodeShrink(ItemDisplay itemDisplay) {
        int growTicks = 12;
        int shrinkTicks = 12;
        float startX = 1.0f;
        float startY = 50.0f;
        float startZ = 1.0f;
        float endX = 4.0f;
        float endY = 50.0f;
        float endZ = 4.0f;
        
        final float rotationDegreesPerTick = 10.0f;
        
        new BukkitRunnable() {
            int tick = 0;
            boolean exploded = false;
            float yaw = 0f;
            
            @Override
            public void run() {
                if (itemDisplay.isDead() || !itemDisplay.isValid()) {
                    cancel();
                    return;
                }
                
                yaw += rotationDegreesPerTick;
                if (yaw >= 360f) yaw -= 360f;
                AxisAngle4f leftRot = new AxisAngle4f((float) Math.toRadians(yaw), 0, 1, 0);
                
                if (!exploded) {
                    float t = Math.min(1f, growTicks <= 0 ? 1f : (float) tick / (float) growTicks);
                    float sx = lerp(startX, endX, t);
                    float sy = lerp(startY, endY, t);
                    float sz = lerp(startZ, endZ, t);
                    applyTransform(itemDisplay, leftRot, sx, sy, sz);
                    tick++;
                    if (t >= 1f) {
                        explodeItemDisplay(itemDisplay);
                        exploded = true;
                        tick = 0;
                    }
                    return;
                }
                
                float t = Math.min(1f, shrinkTicks <= 0 ? 1f : (float) tick / (float) shrinkTicks);
                float sx = lerp(endX, startX, t);
                float sy = lerp(endY, startY, t);
                float sz = lerp(endZ, startZ, t);
                applyTransform(itemDisplay, leftRot, sx, sy, sz);
                tick++;
                if (t >= 1f) {
                    itemDisplay.remove();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void explodeItemDisplay(ItemDisplay itemDisplay) {
        Location location = itemDisplay.getLocation();
        float power = (float) plugin.getConfig().getDouble("weapons.villager_staff.explosion.power", 3.0);
        double rewardTagRadius = power;
        
        for (org.bukkit.entity.Entity e : location.getWorld().getNearbyEntities(location, rewardTagRadius, rewardTagRadius, rewardTagRadius)) {
            if (e instanceof LivingEntity living) {
                taggedForReward.put(living.getUniqueId(), System.currentTimeMillis());
            }
        }
        
        location.getWorld().createExplosion(location, power, false, true);
        int particleCount = 50;
        location.getWorld().spawnParticle(Particle.EXPLOSION, location, particleCount,
            power / 2, power / 2, power / 2, 0.1);
        location.getWorld().spawnParticle(Particle.FLAME, location, particleCount * 2,
            power / 2, power / 2, power / 2, 0.05);
        location.getWorld().spawnParticle(Particle.LARGE_SMOKE, location, particleCount,
            power / 2, power / 2, power / 2, 0.1);
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        location.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.2f);
    }
    
    public boolean isTaggedForReward(UUID entityId) {
        Long ts = taggedForReward.get(entityId);
        if (ts == null) return false;
        return System.currentTimeMillis() - ts <= 3000;
    }
    
    public void removeRewardTag(UUID entityId) {
        taggedForReward.remove(entityId);
    }
    
    private void applyTransform(ItemDisplay itemDisplay, AxisAngle4f leftRotation, float x, float y, float z) {
        Transformation current = itemDisplay.getTransformation();
        Transformation updated = new Transformation(
            current.getTranslation(),
            new Quaternionf(leftRotation),
            new Vector3f(x, y, z),
            current.getRightRotation()
        );
        itemDisplay.setTransformation(updated);
    }
    
    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
