package dev.demora.demorahopliteweapons.weapons.withersickles;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import java.util.Random;
public class SickleThrowManager {
    private final DemoraHopliteWeapons plugin;
    private final WitherSickles witherSickles;
    private final SickleCooldownManager cooldownManager;
    private final SickleInventoryManager inventoryManager;
    private final Random random = new Random();
    public SickleThrowManager(DemoraHopliteWeapons plugin, WitherSickles witherSickles, 
                              SickleCooldownManager cooldownManager, SickleInventoryManager inventoryManager) {
        this.plugin = plugin;
        this.witherSickles = witherSickles;
        this.cooldownManager = cooldownManager;
        this.inventoryManager = inventoryManager;
    }
    
    public void throwSickle(Player player) {
        Location startLoc = player.getEyeLocation();
        Vector direction = startLoc.getDirection().normalize();
        ItemDisplay display = (ItemDisplay) player.getWorld().spawnEntity(startLoc, EntityType.ITEM_DISPLAY);
        display.setItemStack(witherSickles.createAdditionalSickle());
        display.setBillboard(Display.Billboard.FIXED);
        double rotationX = 90.0;
        double rotationY = 0.0;
        double rotationZ = 0.0;
        Transformation transformation = display.getTransformation();
        transformation.getScale().set(1.0f, 1.0f, 1.0f);
        float angleX = (float) Math.toRadians(rotationX);
        float angleY = (float) Math.toRadians(rotationY);
        float angleZ = (float) Math.toRadians(rotationZ);
        org.joml.Quaternionf rotation = new org.joml.Quaternionf();
        rotation.rotateXYZ(angleX, angleY, angleZ);
        transformation.getLeftRotation().set(rotation);
        display.setTransformation(transformation);
        player.getWorld().playSound(startLoc, Sound.ENTITY_ITEM_PICKUP, 0.8f, 0.5f);
        player.getWorld().playSound(startLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 100;
            private final double speed = 1.2;
            private Location currentLoc = startLoc.clone();
            @Override
            public void run() {
                if (ticks >= maxTicks || !display.isValid()) {
                    currentLoc.getWorld().playSound(currentLoc, Sound.ENTITY_ITEM_BREAK, 0.6f, 1.0f);
                    currentLoc.getWorld().playSound(currentLoc, Sound.BLOCK_LAVA_EXTINGUISH, 0.4f, 1.5f);
                    display.remove();
                    returnSickleToPlayer(player);
                    cancel();
                    return;
                }
                currentLoc.add(direction.clone().multiply(speed));
                display.teleport(currentLoc);
                for (LivingEntity entity : currentLoc.getWorld().getNearbyLivingEntities(currentLoc, 0.8, e -> !e.equals(player))) {
                    double throwDamage = plugin.getConfig().getDouble("weapons.wither_sickles.throw_damage", 8.0);
                    entity.damage(throwDamage, player);
                    currentLoc.getWorld().playSound(currentLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);
                    currentLoc.getWorld().playSound(currentLoc, Sound.ENTITY_SKELETON_HORSE_DEATH, 0.7f, 1.5f);
                    currentLoc.getWorld().playSound(currentLoc, Sound.ENTITY_WITHER_SHOOT, 0.5f, 1.2f);
                    double throwChance = plugin.getConfig().getDouble("weapons.wither_sickles.wither_chance_throw", 0.2);
                    if (random.nextDouble() < throwChance) {
                        int duration = plugin.getConfig().getInt("weapons.wither_sickles.wither_duration_ticks", 60);
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration, 0, true, true));
                        currentLoc.getWorld().playSound(currentLoc, Sound.ENTITY_WITHER_AMBIENT, 0.8f, 0.9f);
                    }
                    
                    display.remove();
                    returnSickleToPlayer(player);
                    cancel();
                    return;
                }
                
                if (currentLoc.getBlock().getType().isSolid()) {
                    currentLoc.getWorld().playSound(currentLoc, Sound.BLOCK_STONE_HIT, 0.8f, 0.7f);
                    currentLoc.getWorld().playSound(currentLoc, Sound.ENTITY_ITEM_BREAK, 0.6f, 1.0f);
                    display.remove();
                    returnSickleToPlayer(player);
                    cancel();
                    return;
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void returnSickleToPlayer(Player player) {
        if (!player.isOnline()) {
            return;
        }
        long cooldownEnd = cooldownManager.getCooldownEndTime(player);
        long remaining = cooldownEnd - System.currentTimeMillis();
        if (remaining > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && !cooldownManager.isOnCooldown(player)) {
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    if (witherSickles.isWitherSickle(mainHand)) {
                        player.getInventory().setItemInOffHand(witherSickles.createAdditionalSickle());
                        inventoryManager.setOffhandSickle(player, true);
                    }
                }
            }, remaining / 50L);
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> {
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (witherSickles.isWitherSickle(mainHand)) {
                    player.getInventory().setItemInOffHand(witherSickles.createAdditionalSickle());
                    inventoryManager.setOffhandSickle(player, true);
                }
            });
        }
    }
}

