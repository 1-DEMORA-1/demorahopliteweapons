package dev.demora.demorahopliteweapons.weapons.excalibur;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Excalibur {
    
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey excaliburKey;
    private final Map<UUID, Long> cooldowns;
    private final Map<UUID, Long> activeProtection;
    
    public Excalibur(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.excaliburKey = new NamespacedKey(plugin, "excalibur");
        this.cooldowns = new HashMap<>();
        this.activeProtection = new HashMap<>();
    }
    
    public ItemStack createExcalibur() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§lЭкскалибур");
            meta.setCustomModelData(3);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setUnbreakable(true);
            
            double damage = plugin.getConfig().getDouble("weapons.excalibur.attribute_damage", 11.0);
            AttributeModifier damageModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_damage",
                damage,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
            
            double attackSpeed = plugin.getConfig().getDouble("weapons.excalibur.attack_speed", -2.4);
            AttributeModifier attackSpeedModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_speed",
                attackSpeed,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, attackSpeedModifier);
            
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(excaliburKey, PersistentDataType.BYTE, (byte) 1);
            
            sword.setItemMeta(meta);
        }
        
        return sword;
    }
    
    public boolean isExcalibur(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Byte mark = pdc.get(excaliburKey, PersistentDataType.BYTE);
        return mark != null && mark == (byte) 1;
    }
    
    public boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        
        long cooldownTime = plugin.getConfig().getInt("weapons.excalibur.cooldown", 20) * 1000L;
        long lastUse = cooldowns.get(playerId);
        return System.currentTimeMillis() - lastUse < cooldownTime;
    }
    
    public long getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long cooldownTime = plugin.getConfig().getInt("weapons.excalibur.cooldown", 20) * 1000L;
        long lastUse = cooldowns.get(playerId);
        long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
        
        return Math.max(0, remaining / 1000L + 1);
    }
    
    public void activateProtection(Player player) {
        UUID playerId = player.getUniqueId();
        int duration = plugin.getConfig().getInt("weapons.excalibur.duration", 7);
        
        activeProtection.put(playerId, System.currentTimeMillis());
        cooldowns.put(playerId, System.currentTimeMillis());
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration * 20, 255, false, false, false));
        
        Location playerLoc = player.getLocation();
        
        for (Player nearbyPlayer : player.getWorld().getNearbyPlayers(playerLoc, 10.0)) {
            nearbyPlayer.playSound(playerLoc, Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
        }
        
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, 
            playerLoc.clone().add(0, 1, 0), 
            100, 
            0.5, 0.8, 0.5, 
            0.3);
    }
    
    public boolean hasActiveProtection(Player player) {
        UUID playerId = player.getUniqueId();
        if (!activeProtection.containsKey(playerId)) {
            return false;
        }
        
        long duration = plugin.getConfig().getInt("weapons.excalibur.duration", 7) * 1000L;
        long activatedTime = activeProtection.get(playerId);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - activatedTime > duration) {
            activeProtection.remove(playerId);
            return false;
        }
        
        return true;
    }
    
    public void removePlayerData(UUID playerId) {
        cooldowns.remove(playerId);
        activeProtection.remove(playerId);
    }
}
