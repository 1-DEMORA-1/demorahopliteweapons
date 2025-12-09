package dev.demora.demorahopliteweapons.weapons.magmaclub;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MagmaClub {
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey magmaClubKey;
    private final Map<Player, Long> cooldowns;
    private final Set<Player> fireResistancePlayers;
    
    public MagmaClub(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.magmaClubKey = new NamespacedKey(plugin, "magma_club");
        this.cooldowns = new HashMap<>();
        this.fireResistancePlayers = new HashSet<>();
        startFireResistanceTask();
    }
    
    public ItemStack createMagmaClub() {
        ItemStack club = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = club.getItemMeta();
        
        meta.setDisplayName("§6§lМагмовая бита");
        meta.setCustomModelData(5);
        meta.setUnbreakable(true);
        
        double damage = plugin.getConfig().getDouble("weapons.magma_club.attribute_damage", 11.0);
        AttributeModifier damageModifier = new AttributeModifier(
            UUID.randomUUID(),
            "generic.attack_damage",
            damage,
            AttributeModifier.Operation.ADD_NUMBER,
            EquipmentSlot.HAND
        );
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
        
        double attackSpeed = plugin.getConfig().getDouble("weapons.magma_club.attack_speed", -2.4);
        AttributeModifier attackSpeedModifier = new AttributeModifier(
            UUID.randomUUID(),
            "generic.attack_speed",
            attackSpeed,
            AttributeModifier.Operation.ADD_NUMBER,
            EquipmentSlot.HAND
        );
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, attackSpeedModifier);
        
        meta.getPersistentDataContainer().set(magmaClubKey, PersistentDataType.BYTE, (byte) 1);
        
        club.setItemMeta(meta);
        return club;
    }
    
    public boolean isMagmaClub(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_SWORD) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(magmaClubKey, PersistentDataType.BYTE);
    }
    
    public boolean isOnCooldown(Player player) {
        return cooldowns.containsKey(player) && System.currentTimeMillis() < cooldowns.get(player);
    }
    
    public long getRemainingCooldown(Player player) {
        if (!isOnCooldown(player)) return 0;
        return (cooldowns.get(player) - System.currentTimeMillis()) / 1000;
    }
    
    public void fireBlast(Player player) {
        if (isOnCooldown(player)) {
            long remaining = getRemainingCooldown(player);
            player.sendMessage("§cМагмовая бита перезаряжается! Осталось: §e" + remaining + " §cсек");
            return;
        }
        
        Location playerLoc = player.getLocation();
        World world = player.getWorld();
        
        createFireParticles(playerLoc, world);
        
        double damage = plugin.getConfig().getDouble("weapons.magma_club.damage", 8.0);
        
        for (Entity entity : world.getNearbyEntities(playerLoc, 5, 5, 5)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                
                target.damage(damage);
                target.setFireTicks(100);
                
                Vector knockback = target.getLocation().toVector().subtract(playerLoc.toVector()).normalize();
                knockback.setY(0.5);
                target.setVelocity(knockback.multiply(1.5));
                
                if (target instanceof Player) {
                    Player targetPlayer = (Player) target;
                    targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1.0f, 1.0f);
                }
            }
        }
        
        world.playSound(playerLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
        world.playSound(playerLoc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.5f);
        
        int cooldownSeconds = plugin.getConfig().getInt("weapons.magma_club.cooldown", 15);
        cooldowns.put(player, System.currentTimeMillis() + (cooldownSeconds * 1000L));
    }
    
    private void createFireParticles(Location center, World world) {
        new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (ticks >= 20) {
                    cancel();
                    return;
                }
                
                for (int i = 0; i < 50; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double radius = Math.random() * 5;
                    double x = center.getX() + Math.cos(angle) * radius;
                    double z = center.getZ() + Math.sin(angle) * radius;
                    double y = center.getY() + Math.random() * 2;
                    
                    Location particleLoc = new Location(world, x, y, z);
                    world.spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0.1);
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
    
    private void startFireResistanceTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    
                    if (isMagmaClub(mainHand)) {
                        if (!fireResistancePlayers.contains(player)) {
                            fireResistancePlayers.add(player);
                        }
                        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, true, false));
                    } else {
                        fireResistancePlayers.remove(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }
    
    public void removePlayerData(Player player) {
        cooldowns.remove(player);
        fireResistancePlayers.remove(player);
    }
}
