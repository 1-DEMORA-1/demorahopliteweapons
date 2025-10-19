package dev.demora.demorahopliteweapons.weapons.dragonkatana.rework;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public class AwakenedDragonKatana {
    
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey awakenedKatanaKey;
    private final Map<UUID, Long> cooldowns;
    private final Map<UUID, Long> teleportCooldowns;
    private final Map<UUID, Location> lastTeleportLocations;
    private FileConfiguration reworkConfig;
    
    public AwakenedDragonKatana(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.awakenedKatanaKey = new NamespacedKey(plugin, "awakened_dragon_katana");
        this.cooldowns = new HashMap<>();
        this.teleportCooldowns = new HashMap<>();
        this.lastTeleportLocations = new HashMap<>();
        loadReworkConfig();
    }
    
    private void loadReworkConfig() {
        File reworkFile = new File(plugin.getDataFolder(), "rework.yml");
        
        if (!reworkFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                InputStream defaultConfig = plugin.getResource("rework.yml");
                if (defaultConfig != null) {
                    Files.copy(defaultConfig, reworkFile.toPath());
                    defaultConfig.close();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать rework.yml: " + e.getMessage());
            }
        }
        
        reworkConfig = YamlConfiguration.loadConfiguration(reworkFile);
    }
    
    public void reloadConfig() {
        File reworkFile = new File(plugin.getDataFolder(), "rework.yml");
        reworkConfig = YamlConfiguration.loadConfiguration(reworkFile);
    }
    
    public ItemStack createAwakenedDragonKatana() {
        ItemStack katana = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = katana.getItemMeta();
        
        meta.setDisplayName("§5§lПробуждённая Драконья катана");
        
        meta.addEnchant(Enchantment.SHARPNESS, 5, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);
        
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
            new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", 
                reworkConfig.getDouble("awakened_dragon_katana.damage", 12.0) - 1,
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
        
        meta.setCustomModelData(4);
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(awakenedKatanaKey, PersistentDataType.BYTE, (byte) 1);
        
        katana.setItemMeta(meta);
        return katana;
    }
    
    public boolean isAwakenedDragonKatana(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(awakenedKatanaKey, PersistentDataType.BYTE);
    }
    
    public void handleTeleport(Player player, Location from, Location to) {
        lastTeleportLocations.put(player.getUniqueId(), from);
        
        createParticleSphere(from);
        createExplosionPath(from, to, player);
        
        int cooldownTime = reworkConfig.getInt("awakened_dragon_katana.teleport_cooldown", 5);
        teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownTime * 1000L));
    }
    
    public boolean isOnTeleportCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!teleportCooldowns.containsKey(playerId)) {
            return false;
        }
        
        long cooldownEnd = teleportCooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime >= cooldownEnd) {
            teleportCooldowns.remove(playerId);
            return false;
        }
        
        return true;
    }
    
    public long getTeleportRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!teleportCooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long cooldownEnd = teleportCooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();
        long remainingMs = cooldownEnd - currentTime;
        
        return Math.max(0, remainingMs / 1000);
    }
    
    private void createParticleSphere(Location location) {
        new BukkitRunnable() {
            private int ticks = 0;
            
            @Override
            public void run() {
                int particleDuration = reworkConfig.getInt("awakened_dragon_katana.particle_sphere_duration", 2) * 20;
                if (ticks >= particleDuration) {
                    explodeParticleSphere(location);
                    cancel();
                    return;
                }
                
                double radius = 1.5;
                for (int i = 0; i < 30; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double yAngle = Math.random() * Math.PI;
                    
                    double x = radius * Math.sin(yAngle) * Math.cos(angle);
                    double y = radius * Math.cos(yAngle);
                    double z = radius * Math.sin(yAngle) * Math.sin(angle);
                    
                    Location particleLoc = location.clone().add(x, y + 1, z);
                    location.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLoc, 2, 0.1, 0.1, 0.1, 0.01);
                    location.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 5, 0.2, 0.2, 0.2, 0.5);
                    
                    if (i % 3 == 0) {
                        location.getWorld().spawnParticle(Particle.WITCH, particleLoc, 1, 0, 0, 0, 0);
                    }
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private void explodeParticleSphere(Location location) {
        Location centerLoc = location.clone().add(0, 1, 0);
        
        location.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, centerLoc, 3, 0.5, 0.5, 0.5, 0);
        location.getWorld().spawnParticle(Particle.DRAGON_BREATH, centerLoc, 100, 2, 2, 2, 0.3);
        location.getWorld().spawnParticle(Particle.PORTAL, centerLoc, 150, 3, 3, 3, 1.5);
        location.getWorld().spawnParticle(Particle.WITCH, centerLoc, 50, 2.5, 2.5, 2.5, 0.2);
        location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, centerLoc, 80, 2, 2, 2, 0.5);
        location.getWorld().spawnParticle(Particle.END_ROD, centerLoc, 40, 2, 2, 2, 0.15);
        
        for (int i = 0; i < 20; i++) {
            double angle = (Math.PI * 2 * i) / 20;
            double x = Math.cos(angle) * 3;
            double z = Math.sin(angle) * 3;
            Location ringLoc = centerLoc.clone().add(x, 0, z);
            location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, ringLoc, 3, 0.1, 0.5, 0.1, 0.02);
        }
        
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
        
        double explosionRadius = reworkConfig.getDouble("awakened_dragon_katana.teleport_explosion_radius", 3.0);
        double damage = reworkConfig.getDouble("awakened_dragon_katana.teleport_explosion_damage", 8.0);
        
        location.getWorld().getNearbyEntities(location, explosionRadius, explosionRadius, explosionRadius)
            .forEach(entity -> {
                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    target.damage(damage);
                }
            });
    }
    
    private void createExplosionPath(Location from, Location to, Player owner) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        double distance = from.distance(to);
        int explosions = (int) (distance / reworkConfig.getDouble("awakened_dragon_katana.path_explosion_interval", 5.0));
        
        for (int i = 1; i <= explosions; i++) {
            Location explosionLoc = from.clone().add(direction.clone().multiply(i * 5));
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    createPathExplosion(explosionLoc, owner);
                }
            }.runTaskLater(plugin, i * 5L);
        }
    }
    
    private void createPathExplosion(Location location, Player owner) {
        location.getWorld().spawnParticle(Particle.EXPLOSION, location, 5, 0.5, 0.5, 0.5, 0);
        location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location, 50, 1.5, 1.5, 1.5, 0.15);
        location.getWorld().spawnParticle(Particle.PORTAL, location, 60, 1.5, 1.5, 1.5, 1.0);
        location.getWorld().spawnParticle(Particle.WITCH, location, 25, 1, 1, 1, 0.1);
        location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location, 40, 1.5, 1.5, 1.5, 0.3);
        location.getWorld().spawnParticle(Particle.END_ROD, location, 20, 1, 1, 1, 0.1);
        location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, 15, 1, 1, 1, 0.05);
        
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2 * i) / 8;
            double x = Math.cos(angle) * 2;
            double z = Math.sin(angle) * 2;
            Location ringLoc = location.clone().add(x, 0, z);
            location.getWorld().spawnParticle(Particle.FLAME, ringLoc, 2, 0.1, 0.3, 0.1, 0.02);
        }
        
        location.getWorld().playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 1.2f);
        location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.6f, 1.5f);
        
        double explosionRadius = reworkConfig.getDouble("awakened_dragon_katana.path_explosion_radius", 2.0);
        double damage = reworkConfig.getDouble("awakened_dragon_katana.path_explosion_damage", 6.0);
        
        location.getWorld().getNearbyEntities(location, explosionRadius, explosionRadius, explosionRadius)
            .forEach(entity -> {
                if (entity instanceof Player && !entity.equals(owner)) {
                    Player target = (Player) entity;
                    target.damage(damage);
                }
            });
    }
    
    public void swapAllPlayers(Player owner) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        if (onlinePlayers.size() < 2) {
            return;
        }
        
        for (Player player : onlinePlayers) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 255, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 200, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, false, false, false));
            
            player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0f, 0.5f);
            
            Location playerLoc = player.getLocation();
            playerLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, playerLoc.add(0, 1, 0), 50, 0.5, 1, 0.5, 0.1);
        }
        
        owner.getWorld().playSound(owner.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.5f);
        
        new BukkitRunnable() {
            private int countdown = 5;
            
            @Override
            public void run() {
                if (countdown > 0) {
                    for (Player player : onlinePlayers) {
                        if (player.isOnline()) {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f + (0.2f * (5 - countdown)));
                            
                            Location loc = player.getLocation();
                            loc.getWorld().spawnParticle(Particle.PORTAL, loc.add(0, 1, 0), 30, 0.5, 1, 0.5, 0.5);
                        }
                    }
                    countdown--;
                } else {
                    owner.getWorld().playSound(owner.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 0.5f);
                    
                    Map<Player, Location> originalLocations = new HashMap<>();
                    List<Player> validPlayers = new ArrayList<>();
                    
                    for (Player player : onlinePlayers) {
                        if (player.isOnline()) {
                            originalLocations.put(player, player.getLocation().clone());
                            validPlayers.add(player);
                        }
                    }
                    
                    Collections.shuffle(validPlayers);
                    List<Location> locations = new ArrayList<>(originalLocations.values());
                    
                    for (int i = 0; i < validPlayers.size(); i++) {
                        Player player = validPlayers.get(i);
                        Location newLocation = locations.get((i + 1) % locations.size());
                        player.teleport(newLocation);
                        
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
                        
                        Location particleLoc = player.getLocation().clone();
                        player.getWorld().spawnParticle(Particle.PORTAL, particleLoc.add(0, 1, 0), 100, 1, 1, 1, 1.0);
                        player.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLoc, 50, 1, 1, 1, 0.1);
                        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 60, 1, 1, 1, 0.5);
                        
                        if (!player.equals(owner)) {
                            player.damage(15.0);
                        }
                    }
                    
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    public boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        
        long cooldownEnd = cooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime >= cooldownEnd) {
            cooldowns.remove(playerId);
            return false;
        }
        
        return true;
    }
    
    public long getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long cooldownEnd = cooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();
        long remainingMs = cooldownEnd - currentTime;
        
        return Math.max(0, remainingMs / 1000);
    }
    
    public void setCooldown(Player player) {
        int cooldownTime = reworkConfig.getInt("awakened_dragon_katana.swap_cooldown", 60);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownTime * 1000L));
    }
    
    public void removePlayerData(UUID playerId) {
        cooldowns.remove(playerId);
        teleportCooldowns.remove(playerId);
        lastTeleportLocations.remove(playerId);
    }
}
