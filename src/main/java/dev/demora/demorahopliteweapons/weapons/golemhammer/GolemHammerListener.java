package dev.demora.demorahopliteweapons.weapons.golemhammer;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class GolemHammerListener implements Listener {
    private final DemoraHopliteWeapons plugin;
    private final GolemHammer golemHammer;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Boolean> inAir = new HashMap<>();
    public GolemHammerListener(DemoraHopliteWeapons plugin, GolemHammer golemHammer) {
        this.plugin = plugin;
        this.golemHammer = golemHammer;
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!golemHammer.isGolemHammer(item)) {
            return;
        }
        
        event.setCancelled(true);
        if (isOnCooldown(player)) {
            long remaining = getRemainingCooldown(player);
            player.sendMessage("§cМолот голема перезаряжается! Осталось: §e" + remaining + " §cсек");
            return;
        }
        
        double launchHeight = plugin.getConfig().getDouble("weapons.golem_hammer.launch_height", 5.0);
        Vector velocity = new Vector(0, launchHeight / 2.5, 0);
        player.setVelocity(velocity);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.5f, 0.8f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.5f);
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 5, 0.5, 0.5, 0.5, 0);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.5, 0.2, 0.5, 0.1);
        inAir.put(player.getUniqueId(), true);
        int cooldownSeconds = plugin.getConfig().getInt("weapons.golem_hammer.cooldown", 15);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownSeconds * 1000L));
        new BukkitRunnable() {
            private int ticks = 0;
            private final int maxTicks = 200;
            @Override
            public void run() {
                if (!player.isOnline() || ticks >= maxTicks) {
                    inAir.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                
                if (inAir.getOrDefault(player.getUniqueId(), false) && player.isOnGround()) {
                    createShockwave(player);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        inAir.remove(player.getUniqueId());
                    }, 20L);
                    cancel();
                    return;
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 5L, 1L);
    }
    
    private void createShockwave(Player player) {
        Location loc = player.getLocation();
        double radius = plugin.getConfig().getDouble("weapons.golem_hammer.shockwave_radius", 6.0);
        double damage = plugin.getConfig().getDouble("weapons.golem_hammer.shockwave_damage", 10.0);
        double knockback = plugin.getConfig().getDouble("weapons.golem_hammer.knockback_strength", 2.0);
        player.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.7f);
        player.getWorld().playSound(loc, Sound.ENTITY_IRON_GOLEM_DEATH, 1.5f, 0.8f);
        player.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 2.0f, 0.5f);
        player.getWorld().spawnParticle(Particle.CLOUD, loc, 30, 0.3, 0.1, 0.3, 0.1);
        player.getWorld().spawnParticle(Particle.CRIT, loc, 20, 0.3, 0.1, 0.3, 0.05);
        new BukkitRunnable() {
            private double currentRadius = 0.0;
            private final double maxRadius = radius;
            private final double speed = 1.5;
            private boolean damageDealt = false;
            @Override
            public void run() {
                if (currentRadius >= maxRadius) {
                    if (!damageDealt) {
                        for (LivingEntity entity : loc.getWorld().getNearbyLivingEntities(loc, maxRadius, e -> !e.equals(player))) {
                            entity.damage(damage, player);
                            Vector direction = entity.getLocation().toVector().subtract(loc.toVector()).normalize();
                            direction.setY(0.5);
                            direction.multiply(knockback);
                            entity.setVelocity(direction);                            
                            if (entity instanceof Player) {
                                Player targetPlayer = (Player) entity;
                                targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1, false, false));
                            }
                        }
                        damageDealt = true;
                    }
                    cancel();
                    return;
                }
                
                int particles = (int) (currentRadius * 15);
                if (particles < 8) particles = 8;
                for (int i = 0; i < particles; i++) {
                    double angle = (2 * Math.PI * i) / particles;
                    double x = Math.cos(angle) * currentRadius;
                    double z = Math.sin(angle) * currentRadius;
                    Location particleLoc = loc.clone().add(x, 0.1, z);
                    player.getWorld().spawnParticle(Particle.CLOUD, particleLoc, 2, 0.1, 0.1, 0.1, 0.02);
                    player.getWorld().spawnParticle(Particle.CRIT, particleLoc, 1, 0.05, 0.05, 0.05, 0);
                }
                currentRadius += speed;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private boolean isOnCooldown(Player player) {
        Long end = cooldowns.get(player.getUniqueId());
        if (end == null) {
            return false;
        }
        if (System.currentTimeMillis() >= end) {
            cooldowns.remove(player.getUniqueId());
            return false;
        }
        return true;
    }
    
    private long getRemainingCooldown(Player player) {
        Long end = cooldowns.get(player.getUniqueId());
        if (end == null) {
            return 0;
        }
        long diff = end - System.currentTimeMillis();
        if (diff <= 0) {
            cooldowns.remove(player.getUniqueId());
            return 0;
        }
        return (long) Math.ceil(diff / 1000.0);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && inAir.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        cooldowns.remove(playerId);
        inAir.remove(playerId);
    }
}

