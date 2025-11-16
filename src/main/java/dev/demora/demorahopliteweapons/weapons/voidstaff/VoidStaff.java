package dev.demora.demorahopliteweapons.weapons.voidstaff;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import java.time.Duration;
import java.util.*;

public class VoidStaff {
    
    public enum StaffMode {
        PORTAL("Портал", 4),
        SHULKER_CHARGE("Заряд шалкера", 5);
        
        private final String displayName;
        private final int modelData;
        
        StaffMode(String displayName, int modelData) {
            this.displayName = displayName;
            this.modelData = modelData;
        }
        
        public String getDisplayName() { return displayName; }
        public int getModelData() { return modelData; }
    }
    
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey voidStaffKey;
    private final NamespacedKey staffModeKey;
    private final Map<UUID, Long> portalCooldowns;
    private final Map<UUID, Long> shulkerCooldowns;
    private final Map<UUID, Long> portalSpawnCooldowns;
    private final Map<UUID, StaffMode> playerModes;
    private final PortalManager portalManager;
    
    public VoidStaff(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.voidStaffKey = new NamespacedKey(plugin, "void_staff");
        this.staffModeKey = new NamespacedKey(plugin, "staff_mode");
        this.portalCooldowns = new HashMap<>();
        this.shulkerCooldowns = new HashMap<>();
        this.portalSpawnCooldowns = new HashMap<>();
        this.playerModes = new HashMap<>();
        this.portalManager = new PortalManager(plugin);
    }
    
    public ItemStack createVoidStaff() {
        ItemStack staff = new ItemStack(Material.STONE_SWORD);
        ItemMeta meta = staff.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§5§lПосох пустоты");
            meta.setCustomModelData(4);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            meta.setUnbreakable(true);
            
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(voidStaffKey, PersistentDataType.BYTE, (byte) 1);
            pdc.set(staffModeKey, PersistentDataType.STRING, StaffMode.PORTAL.name());
            
            staff.setItemMeta(meta);
        }
        
        return staff;
    }
    
    public boolean isVoidStaff(ItemStack item) {
        if (item == null || item.getType() != Material.STONE_SWORD || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Byte mark = pdc.get(voidStaffKey, PersistentDataType.BYTE);
        return mark != null && mark == (byte) 1;
    }
    
    public StaffMode getStaffMode(ItemStack staff) {
        if (!isVoidStaff(staff)) {
            return StaffMode.PORTAL;
        }
        
        ItemMeta meta = staff.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String modeStr = pdc.get(staffModeKey, PersistentDataType.STRING);
        
        try {
            return StaffMode.valueOf(modeStr);
        } catch (Exception e) {
            return StaffMode.PORTAL;
        }
    }
    
    public void switchMode(Player player, ItemStack staff) {
        if (!isVoidStaff(staff)) {
            return;
        }
        
        StaffMode currentMode = getStaffMode(staff);
        StaffMode newMode = (currentMode == StaffMode.PORTAL) ? StaffMode.SHULKER_CHARGE : StaffMode.PORTAL;
        
        ItemMeta meta = staff.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(newMode.getModelData());
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(staffModeKey, PersistentDataType.STRING, newMode.name());
            staff.setItemMeta(meta);
        }
        
        playerModes.put(player.getUniqueId(), newMode);
        updateActionBar(player, newMode);
        
        player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 1.5f);
    }
    
    public void updateActionBar(Player player, StaffMode mode) {
        player.sendActionBar(Component.text("§5Режим: §f" + mode.getDisplayName()));
    }
    
    public boolean isOnPortalCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!portalCooldowns.containsKey(playerId)) {
            return false;
        }
        
        long cooldownTime = plugin.getConfig().getInt("weapons.void_staff.portal_cooldown", 40) * 1000L;
        long lastUse = portalCooldowns.get(playerId);
        return System.currentTimeMillis() - lastUse < cooldownTime;
    }
    
    public boolean isOnShulkerCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!shulkerCooldowns.containsKey(playerId)) {
            return false;
        }
        
        long cooldownTime = plugin.getConfig().getInt("weapons.void_staff.shulker_cooldown", 30) * 1000L;
        long lastUse = shulkerCooldowns.get(playerId);
        return System.currentTimeMillis() - lastUse < cooldownTime;
    }
    
    public long getRemainingPortalCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!portalCooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long cooldownTime = plugin.getConfig().getInt("weapons.void_staff.portal_cooldown", 40) * 1000L;
        long lastUse = portalCooldowns.get(playerId);
        long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
        
        return Math.max(0, remaining / 1000L + 1);
    }
    
    public long getRemainingShulkerCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!shulkerCooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long cooldownTime = plugin.getConfig().getInt("weapons.void_staff.shulker_cooldown", 30) * 1000L;
        long lastUse = shulkerCooldowns.get(playerId);
        long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
        
        return Math.max(0, remaining / 1000L + 1);
    }
    
    public boolean isOnPortalSpawnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!portalSpawnCooldowns.containsKey(playerId)) {
            return false;
        }
        
        long lastUse = portalSpawnCooldowns.get(playerId);
        return System.currentTimeMillis() - lastUse < 1000L;
    }
    
    public void usePortalMode(Player player) {
        if (isOnPortalSpawnCooldown(player)) {
            return;
        }
        
        Location targetLocation = getTargetLocation(player);
        if (targetLocation == null) {
            return;
        }
        
        boolean portalCreated = portalManager.createPortal(player, targetLocation);
        
        if (portalCreated || portalManager.hasFirstPortal(player)) {
            if (portalCreated) {
                portalCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            }
            
            portalSpawnCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.0f);
        }
    }
    
    public void useShulkerMode(Player player) {
        shulkerCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        
        Location eyeLoc = player.getEyeLocation();
        ShulkerBullet bullet = player.getWorld().spawn(eyeLoc, ShulkerBullet.class);
        bullet.setShooter(player);
        bullet.setTarget(null);
        
        org.bukkit.util.Vector direction = eyeLoc.getDirection().normalize().multiply(0.7);
        
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                if (bullet.isDead() || !bullet.isValid() || ticks > 200) {
                    Location explodeLoc = bullet.getLocation();
                    bullet.remove();
                    
                    float explosionPower = (float) plugin.getConfig().getDouble("weapons.void_staff.shulker_explosion_power", 4.0);
                    explodeLoc.getWorld().createExplosion(explodeLoc, explosionPower, false, true);
                    
                    Player nearestPlayer = getNearestPlayer(explodeLoc, player);
                    
                    for (int i = 0; i < 3; i++) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                ShulkerBullet extraBullet = explodeLoc.getWorld().spawn(explodeLoc, ShulkerBullet.class);
                                extraBullet.setShooter(player);
                                
                                if (nearestPlayer != null && nearestPlayer.isOnline()) {
                                    extraBullet.setTarget(nearestPlayer);
                                } else {
                                    extraBullet.setTarget(null);
                                }
                            }
                        }.runTaskLater(plugin, i * 5L);
                    }
                    
                    cancel();
                    return;
                }
                
                bullet.setVelocity(direction);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        
        player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 1.0f, 1.0f);
    }
    
    private Player getNearestPlayer(Location location, Player owner) {
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Player p : location.getWorld().getPlayers()) {
            if (p.equals(owner) || !p.isOnline()) {
                continue;
            }
            
            double distance = p.getLocation().distance(location);
            if (distance < minDistance && distance < 50) {
                minDistance = distance;
                nearest = p;
            }
        }
        
        return nearest;
    }
    
    private Location getTargetLocation(Player player) {
        RayTraceResult result = player.getWorld().rayTraceBlocks(
            player.getEyeLocation(),
            player.getEyeLocation().getDirection(),
            50.0
        );
        
        if (result != null && result.getHitBlock() != null) {
            Block hitBlock = result.getHitBlock();
            return hitBlock.getLocation().add(0, 1, 0);
        }
        
        Location playerLoc = player.getLocation();
        org.bukkit.util.Vector direction = playerLoc.getDirection().normalize().multiply(10);
        return playerLoc.add(direction);
    }
    
    public PortalManager getPortalManager() {
        return portalManager;
    }
    
    public void removePlayerData(UUID playerId) {
        portalCooldowns.remove(playerId);
        shulkerCooldowns.remove(playerId);
        portalSpawnCooldowns.remove(playerId);
        playerModes.remove(playerId);
    }
}
