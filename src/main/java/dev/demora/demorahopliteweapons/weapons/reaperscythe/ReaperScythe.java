package dev.demora.demorahopliteweapons.weapons.reaperscythe;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ReaperScythe {
    
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey reaperScytheKey;
    private final Map<UUID, Long> cooldowns;
    
    public ReaperScythe(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.reaperScytheKey = new NamespacedKey(plugin, "reaper_scythe");
        this.cooldowns = new HashMap<>();
    }
    
    public ItemStack createReaperScythe() {
        ItemStack scythe = new ItemStack(Material.STONE_HOE);
        ItemMeta meta = scythe.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§5§lКоса жнеца");
            meta.setCustomModelData(2);
            
            meta.getPersistentDataContainer().set(reaperScytheKey, PersistentDataType.BOOLEAN, true);
            
            scythe.setItemMeta(meta);
            
            scythe.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
            meta.addEnchant(Enchantment.SHARPNESS, 5, true);
            meta.setUnbreakable(true);
            scythe.setItemMeta(meta);
        }
        
        return scythe;
    }
    
    public boolean isReaperScythe(ItemStack item) {
        if (item == null || item.getType() != Material.STONE_HOE) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        return meta.getPersistentDataContainer().has(reaperScytheKey, PersistentDataType.BOOLEAN);
    }
    
    public boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        
        long cooldownTime = plugin.getConfig().getInt("weapons.reaper_scythe.cooldown", 15) * 1000L;
        long lastUse = cooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();
        return currentTime - lastUse < cooldownTime;
    }
    
    public long getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long cooldownTime = plugin.getConfig().getInt("weapons.reaper_scythe.cooldown", 15) * 1000L;
        long lastUse = cooldowns.get(playerId);
        long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
        
        return Math.max(0, remaining / 1000L);
    }
    
    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public void performReaperAttack(Player attacker, Player target) {
        Collection<PotionEffect> targetEffects = target.getActivePotionEffects();
        List<PotionEffect> positiveEffects = new ArrayList<>();
        
        for (PotionEffect effect : targetEffects) {
            PotionEffectType type = effect.getType();
            if (isPositiveEffect(type)) {
                positiveEffects.add(effect);
            }
        }
        
        if (positiveEffects.isEmpty()) {
            return;
        }
        
        for (PotionEffect effect : positiveEffects) {
            target.removePotionEffect(effect.getType());
        }
        
        for (PotionEffect effect : positiveEffects) {
            attacker.addPotionEffect(effect);
        }
        
        int healHearts = plugin.getConfig().getInt("weapons.reaper_scythe.heal_hearts", 2);
        double healAmount = healHearts * 2.0;
        double maxHealth = attacker.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double newHealth = Math.min(attacker.getHealth() + healAmount, maxHealth);
        attacker.setHealth(newHealth);
        
        setCooldown(attacker);
        
        World world = target.getWorld();
        Location loc = target.getLocation().add(0, 1.0, 0);
        world.spawnParticle(Particle.SOUL, loc, 60, 0.85, 0.9, 0.85, 0.15);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 40, 0.75, 1.0, 0.75, 0.08);
        world.playSound(loc, Sound.ENTITY_VEX_DEATH, 1.0f, 0.7f);
    }
    
    private boolean isPositiveEffect(PotionEffectType type) {
        return type == PotionEffectType.SPEED ||
               type == PotionEffectType.HASTE ||
               type == PotionEffectType.STRENGTH ||
               type == PotionEffectType.JUMP_BOOST ||
               type == PotionEffectType.REGENERATION ||
               type == PotionEffectType.RESISTANCE ||
               type == PotionEffectType.FIRE_RESISTANCE ||
               type == PotionEffectType.WATER_BREATHING ||
               type == PotionEffectType.INVISIBILITY ||
               type == PotionEffectType.NIGHT_VISION ||
               type == PotionEffectType.HEALTH_BOOST ||
               type == PotionEffectType.ABSORPTION ||
               type == PotionEffectType.SATURATION ||
               type == PotionEffectType.LUCK ||
               type == PotionEffectType.SLOW_FALLING ||
               type == PotionEffectType.CONDUIT_POWER ||
               type == PotionEffectType.DOLPHINS_GRACE ||
               type == PotionEffectType.HERO_OF_THE_VILLAGE;
    }
}
