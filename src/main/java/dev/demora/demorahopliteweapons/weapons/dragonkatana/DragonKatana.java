package dev.demora.demorahopliteweapons.weapons.dragonkatana;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DragonKatana {
    
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey katanaKey;
    private final Map<UUID, Long> lastDash;
    private final Map<UUID, Boolean> hasKatana;
    
    private static final String KATANA_NAME = "§5§lДраконья катана";
    private static final int SPEED_EFFECT_DURATION = 80;
    private static final int SPEED_EFFECT_AMPLIFIER = 1;
    private static final double COLLISION_STEP = 0.3;
    private static final int PARTICLE_COUNT = 50;
    private static final double PARTICLE_OFFSET = 0.5;
    private static final double PARTICLE_SPEED = 0.1;
    
    public DragonKatana(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.katanaKey = new NamespacedKey(plugin, "dragon_katana");
        this.lastDash = new ConcurrentHashMap<>();
        this.hasKatana = new ConcurrentHashMap<>();
    }
    
    public ItemStack createDragonKatana() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.setDisplayName(KATANA_NAME);
        meta.setCustomModelData(1);
        meta.addEnchant(Enchantment.SHARPNESS, 5, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);
        
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(katanaKey, PersistentDataType.BYTE, (byte) 1);
        
        if (meta instanceof Damageable damageable) {
            damageable.setUnbreakable(true);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    public boolean isKatana(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Byte mark = pdc.get(katanaKey, PersistentDataType.BYTE);
        return mark != null && mark == (byte) 1;
    }
    
    public void updateSpeedEffects() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (hasKatana.getOrDefault(p.getUniqueId(), false)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, SPEED_EFFECT_DURATION, SPEED_EFFECT_AMPLIFIER, true, false, false));
            }
        }
    }
    
    public boolean isOnCooldown(Player player) {
        long now = System.currentTimeMillis();
        long last = lastDash.getOrDefault(player.getUniqueId(), 0L);
        int cd = Math.max(0, plugin.getConfig().getInt("weapons.dragon_katana.cooldown-seconds", 5));
        
        return now - last < cd * 1000L;
    }
    
    public long getRemainingCooldown(Player player) {
        long now = System.currentTimeMillis();
        long last = lastDash.getOrDefault(player.getUniqueId(), 0L);
        int cd = Math.max(0, plugin.getConfig().getInt("weapons.dragon_katana.cooldown-seconds", 5));
        
        return (cd * 1000L - (now - last)) / 1000L + 1;
    }
    
    public void performDash(Player player) {
        lastDash.put(player.getUniqueId(), System.currentTimeMillis());
        
        double dist = Math.max(0D, plugin.getConfig().getDouble("weapons.dragon_katana.dash-distance-blocks", 8.0));
        Vector dir = player.getLocation().getDirection().normalize();
        Location start = player.getLocation();
        Location lastSafe = start.clone();
        
        for (double s = COLLISION_STEP; s <= dist; s += COLLISION_STEP) {
            Location candidate = start.clone().add(dir.clone().multiply(s));
            if (isSpaceFree(candidate)) {
                lastSafe = candidate.clone();
            } else {
                break;
            }
        }
        
        if (!lastSafe.equals(start)) {
            lastSafe.setYaw(start.getYaw());
            lastSafe.setPitch(start.getPitch());
            player.teleport(lastSafe);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            player.getWorld().spawnParticle(Particle.PORTAL, start, PARTICLE_COUNT, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_SPEED);
            player.getWorld().spawnParticle(Particle.PORTAL, lastSafe, PARTICLE_COUNT, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_SPEED);
        }
    }
    
    private boolean isSpaceFree(Location loc) {
        double[][] offsets = {
            {0.0, 0.0},
            {0.3, 0.0}, {-0.3, 0.0}, {0.0, 0.3}, {0.0, -0.3}
        };
        
        for (double[] off : offsets) {
            Location feet = loc.clone().add(off[0], 0.0, off[1]);
            Location head = loc.clone().add(off[0], 1.0, off[1]);
            Block feetBlock = feet.getWorld().getBlockAt(feet);
            Block headBlock = head.getWorld().getBlockAt(head);
            if (!feetBlock.isPassable() || !headBlock.isPassable()) {
                return false;
            }
        }
        return true;
    }
    
    public void setHasKatana(UUID playerId, boolean hasKatana) {
        this.hasKatana.put(playerId, hasKatana);
    }
    
    public void removePlayerData(UUID playerId) {
        lastDash.remove(playerId);
        hasKatana.remove(playerId);
    }

    public boolean isDragonKatana(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(katanaKey, PersistentDataType.BYTE);
    }
}
