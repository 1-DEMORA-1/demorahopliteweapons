package dev.demora.demorahopliteweapons.weapons.mjolnir;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Mjolnir {
    
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey mjolnirKey;
    private final Map<String, Long> cooldowns;
    private final Set<UUID> processingMjolnirDamage;
    
    public Mjolnir(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.mjolnirKey = new NamespacedKey(plugin, "mjolnir");
        this.cooldowns = new HashMap<>();
        this.processingMjolnirDamage = new HashSet<>();
    }
    
    public ItemStack createMjolnir() {
        ItemStack item = new ItemStack(Material.STONE_AXE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§lМьёльнир");
            meta.setCustomModelData(1);
            meta.addEnchant(Enchantment.SHARPNESS, 5, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.setUnbreakable(true);
            meta.getPersistentDataContainer().set(mjolnirKey, PersistentDataType.STRING, "mjolnir");
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public boolean isMjolnir(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        return meta.getPersistentDataContainer().has(mjolnirKey, PersistentDataType.STRING);
    }
    
    public boolean isOnThrowCooldown(Player player) {
        String playerId = player.getUniqueId().toString();
        if (!cooldowns.containsKey(playerId + "_throw")) {
            return false;
        }
        
        long lastUse = cooldowns.get(playerId + "_throw");
        int cooldownDuration = plugin.getConfig().getInt("weapons.mjolnir.cooldown.throw_duration", 10);
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - lastUse) < (cooldownDuration * 1000L);
    }
    
    public boolean isOnMeleeCooldown(Player player) {
        String playerId = player.getUniqueId().toString();
        if (!cooldowns.containsKey(playerId + "_melee")) {
            return false;
        }
        
        long lastUse = cooldowns.get(playerId + "_melee");
        int cooldownDuration = plugin.getConfig().getInt("weapons.mjolnir.cooldown.melee_duration", 5);
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - lastUse) < (cooldownDuration * 1000L);
    }
    
    public void setThrowCooldown(Player player) {
        cooldowns.put(player.getUniqueId().toString() + "_throw", System.currentTimeMillis());
    }
    
    public void setMeleeCooldown(Player player) {
        cooldowns.put(player.getUniqueId().toString() + "_melee", System.currentTimeMillis());
    }
    
    public long getRemainingThrowCooldown(Player player) {
        String playerId = player.getUniqueId().toString();
        if (!cooldowns.containsKey(playerId + "_throw")) {
            return 0;
        }
        
        long lastUse = cooldowns.get(playerId + "_throw");
        int cooldownDuration = plugin.getConfig().getInt("weapons.mjolnir.cooldown.throw_duration", 10);
        long currentTime = System.currentTimeMillis();
        long remaining = (cooldownDuration * 1000L) - (currentTime - lastUse);
        
        return Math.max(0, remaining / 1000L);
    }
    
    public long getRemainingMeleeCooldown(Player player) {
        String playerId = player.getUniqueId().toString();
        if (!cooldowns.containsKey(playerId + "_melee")) {
            return 0;
        }
        
        long lastUse = cooldowns.get(playerId + "_melee");
        int cooldownDuration = plugin.getConfig().getInt("weapons.mjolnir.cooldown.melee_duration", 5);
        long currentTime = System.currentTimeMillis();
        long remaining = (cooldownDuration * 1000L) - (currentTime - lastUse);
        
        return Math.max(0, remaining / 1000L);
    }
    
    public void throwMjolnir(Player player) {
        Location loc = player.getEyeLocation();
        ItemDisplay mjolnirDisplay = (ItemDisplay) player.getWorld().spawnEntity(loc, EntityType.ITEM_DISPLAY);
        
        ItemStack displayItem = new ItemStack(Material.STONE_AXE);
        ItemMeta displayMeta = displayItem.getItemMeta();
        displayMeta.setCustomModelData(1);
        displayItem.setItemMeta(displayMeta);
        mjolnirDisplay.setItemStack(displayItem);
        
        Transformation transformation = mjolnirDisplay.getTransformation();
        transformation.getLeftRotation().rotationX((float) Math.toRadians(90));
        mjolnirDisplay.setTransformation(transformation);
        mjolnirDisplay.setRotation(loc.getYaw(), loc.getPitch());
        
        player.getInventory().setItemInMainHand(null);
        
        Vector direction = loc.getDirection().normalize().multiply(1.5);
        
        player.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 1.0f);
        
        double flightSpeed = plugin.getConfig().getDouble("weapons.mjolnir.animation.flight_speed", 0.3);
        double returnSpeed = plugin.getConfig().getDouble("weapons.mjolnir.animation.return_speed", 0.4);
        float rotationSpeed = (float) plugin.getConfig().getDouble("weapons.mjolnir.animation.rotation_speed", 25.0);
        float returnRotationMultiplier = (float) plugin.getConfig().getDouble("weapons.mjolnir.animation.return_rotation_multiplier", 1.5);
        
        new BukkitRunnable() {
            private final Location startLoc = loc.clone();
            private Location currentLoc = startLoc.clone();
            private boolean isReturning = false;
            private int ticks = 0;
            private float rotationAngle = 0;
            private float targetRotationAngle = 0;
            private Vector currentVelocity = direction.clone().normalize().multiply(flightSpeed);
            
            @Override
            public void run() {
                if (!isReturning) {
                    currentLoc.add(currentVelocity);
                    
                    double wobble = Math.sin(ticks * 0.3) * 0.05;
                    Location displayLoc = currentLoc.clone().add(0, wobble, 0);
                    mjolnirDisplay.teleport(displayLoc);
                    
                    targetRotationAngle += rotationSpeed;
                    rotationAngle += (targetRotationAngle - rotationAngle) * 0.15f;
                    Transformation transformation = mjolnirDisplay.getTransformation();
                    transformation.getLeftRotation().rotationX((float) Math.toRadians(90));
                    transformation.getLeftRotation().rotateY((float) Math.toRadians(rotationAngle));
                    mjolnirDisplay.setTransformation(transformation);
                    
                    Block targetBlock = currentLoc.getBlock();
                    if (!targetBlock.isPassable() || ticks >= 80) {
                        currentLoc.getWorld().strikeLightning(currentLoc);
                        
                        double areaDamage = plugin.getConfig().getDouble("weapons.mjolnir.damage.area_damage", 12.0);
                        for (Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, 2, 2, 2)) {
                            if (entity instanceof LivingEntity && entity != player) {
                                ((LivingEntity) entity).damage(areaDamage, player);
                            }
                        }
                        isReturning = true;
                        return;
                    }
                    
                    for (Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, 1, 1, 1)) {
                        if (entity instanceof LivingEntity && entity != player) {
                            if (entity instanceof Player) {
                                Player targetPlayer = (Player) entity;
                                targetPlayer.getWorld().strikeLightning(targetPlayer.getLocation());
                                double throwDamage = plugin.getConfig().getDouble("weapons.mjolnir.damage.throw_damage", 15.0);
                                targetPlayer.damage(throwDamage, player);
                            } else {
                                currentLoc.getWorld().strikeLightning(currentLoc);
                                double areaDamage = plugin.getConfig().getDouble("weapons.mjolnir.damage.area_damage", 12.0);
                                for (Entity nearbyEntity : currentLoc.getWorld().getNearbyEntities(currentLoc, 2, 2, 2)) {
                                    if (nearbyEntity instanceof LivingEntity && nearbyEntity != player) {
                                        ((LivingEntity) nearbyEntity).damage(areaDamage, player);
                                    }
                                }
                            }
                            isReturning = true;
                            return;
                        }
                    }
                } else {
                    Location playerTarget = player.getLocation().add(0, 1, 0);
                    Vector returnVector = playerTarget.subtract(currentLoc).toVector().normalize().multiply(returnSpeed);
                    currentLoc.add(returnVector);
                    mjolnirDisplay.teleport(currentLoc);
                    
                    targetRotationAngle += rotationSpeed * returnRotationMultiplier;
                    rotationAngle += (targetRotationAngle - rotationAngle) * 0.2f;
                    Transformation transformation = mjolnirDisplay.getTransformation();
                    transformation.getLeftRotation().rotationX((float) Math.toRadians(90));
                    transformation.getLeftRotation().rotateY((float) Math.toRadians(rotationAngle));
                    mjolnirDisplay.setTransformation(transformation);
                    
                    if (currentLoc.distance(player.getLocation()) < 1.5) {
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 0.5f);
                        
                        ItemStack mjolnir = createMjolnir();
                        HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(mjolnir);
                        
                        if (!leftOver.isEmpty()) {
                            for (ItemStack item : leftOver.values()) {
                                player.getWorld().dropItem(player.getLocation(), item);
                            }
                        }
                        
                        mjolnirDisplay.remove();
                        this.cancel();
                        return;
                    }
                }
                
                currentLoc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, currentLoc, 5, 0.1, 0.1, 0.1, 0.05);
                
                if (ticks % 3 == 0) {
                    currentLoc.getWorld().spawnParticle(Particle.CLOUD, currentLoc, 2, 0.2, 0.2, 0.2, 0.01);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    public boolean isProcessingDamage(UUID playerId) {
        return processingMjolnirDamage.contains(playerId);
    }
    
    public void addProcessingDamage(UUID playerId) {
        processingMjolnirDamage.add(playerId);
    }
    
    public void removeProcessingDamage(UUID playerId) {
        processingMjolnirDamage.remove(playerId);
    }
    
    public void performMeleeAttack(Player target, Player attacker) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Location lightningLoc = target.getLocation().add(0, 1, 0);
                
                LightningStrike lightning = target.getWorld().strikeLightningEffect(lightningLoc);
                
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, lightningLoc, 15, 0.3, 0.3, 0.3, 0.05);
            }
        }.runTaskLater(plugin, 1L);
    }
}
