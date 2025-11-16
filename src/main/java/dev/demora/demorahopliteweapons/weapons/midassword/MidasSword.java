package dev.demora.demorahopliteweapons.weapons.midassword;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MidasSword {
    
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey midasSwordKey;
    private final NamespacedKey damageLevelKey;
    
    public MidasSword(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.midasSwordKey = new NamespacedKey(plugin, "midas_sword");
        this.damageLevelKey = new NamespacedKey(plugin, "damage_level");
    }
    
    public ItemStack createMidasSword() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lМеч Мидаса"));
            
            int initialDamage = plugin.getConfig().getInt("weapons.midas_sword.initial-damage", 5);
            
            List<String> lore = Arrays.asList(
                ChatColor.YELLOW + "Урон: " + initialDamage
            );
            meta.setLore(lore);
            
            meta.setCustomModelData(1);
            meta.setUnbreakable(true);
            meta.getPersistentDataContainer().set(midasSwordKey, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(damageLevelKey, PersistentDataType.INTEGER, initialDamage);
            
            if (initialDamage > 0) {
                AttributeModifier damageModifier = new AttributeModifier(
                    UUID.randomUUID(),
                    "generic.attack_damage",
                    initialDamage,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.HAND
                );
                meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
            }
            
            double attackSpeed = plugin.getConfig().getDouble("weapons.midas_sword.attack-speed", 1.6);
            AttributeModifier speedModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_speed",
                attackSpeed,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, speedModifier);
            sword.setItemMeta(meta);
        }
        
        return sword;
    }
    
    public boolean isMidasSword(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_SWORD) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        return meta.getPersistentDataContainer().has(midasSwordKey, PersistentDataType.BOOLEAN);
    }
    
    public int getDamageLevel(ItemStack sword) {
        if (!isMidasSword(sword)) {
            return 0;
        }
        
        ItemMeta meta = sword.getItemMeta();
        if (meta == null) {
            return 0;
        }
        
        return meta.getPersistentDataContainer().getOrDefault(damageLevelKey, PersistentDataType.INTEGER, 0);
    }
    
    public void increaseDamage(ItemStack sword) {
        if (!isMidasSword(sword)) {
            return;
        }
        
        ItemMeta meta = sword.getItemMeta();
        if (meta == null) {
            return;
        }
        
        int currentDamage = getDamageLevel(sword);
        int maxDamage = plugin.getConfig().getInt("weapons.midas_sword.max-damage", 25);
        
        if (currentDamage >= maxDamage) {
            return;
        }
        
        int damagePerKill = plugin.getConfig().getInt("weapons.midas_sword.damage-per-kill", 2);
        int popa = currentDamage + damagePerKill;
        int newDamage = popa;
        if (popa > maxDamage) {
            newDamage = maxDamage;
        }
        
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
        
        if (newDamage > 0) {
            AttributeModifier damageModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_damage",
                newDamage,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
        }
        
        meta.getPersistentDataContainer().set(damageLevelKey, PersistentDataType.INTEGER, newDamage);
        
        List<String> lore = meta.getLore();
        if (lore != null && lore.size() > 0) {
            lore.set(0, ChatColor.YELLOW + "Урон: " + newDamage);
            meta.setLore(lore);
        }
        
        sword.setItemMeta(meta);
    }
    
    public void createKillEffects(Player victim, Player killer) {
        Location location = victim.getLocation();
        World world = victim.getWorld();
        
        world.createExplosion(location, 0.0f, false, false);
        
        for (int i = 0; i < 50; i++) {
            double x = location.getX() + (Math.random() - 0.5) * 4.0;
            double y = location.getY() + Math.random() * 3.0;
            double z = location.getZ() + (Math.random() - 0.5) * 4.0;
            
            world.spawnParticle(Particle.HAPPY_VILLAGER, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        
        for (int i = 0; i < 30; i++) {
            double x = location.getX() + (Math.random() - 0.5) * 6;
            double y = location.getY() + Math.random() * 4;
            double z = location.getZ() + (Math.random() - 0.5) * 6;
            
            world.spawnParticle(Particle.ENCHANT, x, y, z, 1, 0, 0, 0, 0);
        }
        
        world.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        world.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
        world.playSound(location, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.5f);
        
        world.strikeLightningEffect(location);
    }
}
