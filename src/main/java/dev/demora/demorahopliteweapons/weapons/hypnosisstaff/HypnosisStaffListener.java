package dev.demora.demorahopliteweapons.weapons.hypnosisstaff;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import java.util.*;
public class HypnosisStaffListener implements Listener {
    private final DemoraHopliteWeapons plugin;
    private final HypnosisStaff hypnosisStaff;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Set<UUID>> controlledMobs = new HashMap<>();
    private final Map<UUID, UUID> mobOwners = new HashMap<>();
    public HypnosisStaffListener(DemoraHopliteWeapons plugin, HypnosisStaff hypnosisStaff) {
        this.plugin = plugin;
        this.hypnosisStaff = hypnosisStaff;
        startTeleportTask();
    }
    
    @EventHandler
    public void onOwnerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player owner = (Player) event.getDamager();
        Set<UUID> mobIds = controlledMobs.get(owner.getUniqueId());
        if (mobIds == null || mobIds.isEmpty()) {
            return;
        }
        Player target = (Player) event.getEntity();
        for (UUID mobId : new HashSet<>(mobIds)) {
            Entity entity = Bukkit.getEntity(mobId);
            if (!(entity instanceof Mob mob)) {
                continue;
            }
            if (mob.isDead() || !mob.isValid()) {
                continue;
            }
            mob.setTarget(target);
        }
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!hypnosisStaff.isHypnosisStaff(item)) {
            return;
        }
        
        event.setCancelled(true);
        Long cooldownEnd = cooldowns.get(player.getUniqueId());
        if (cooldownEnd != null && System.currentTimeMillis() < cooldownEnd) {
            long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
            player.sendMessage("§cОткат на способность. Осталось §e" + remaining + "§c сек.");
            return;
        }
        fireHypnosisRay(player);
    }
    private void fireHypnosisRay(Player player) {
        int cooldownSeconds = plugin.getConfig().getInt("weapons.hypnosis_staff.cooldown", 5);
        double beamSpeed = plugin.getConfig().getDouble("weapons.hypnosis_staff.beam_speed", 0.5);
        double beamStep = Math.max(0.1, beamSpeed);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownSeconds * 1000L));
        Location start = player.getEyeLocation();
        Vector direction = start.getDirection();
        player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.5f);
        new BukkitRunnable() {
            private double distance = 0;
            private final double maxDistance = 50;
            private final double step = beamStep;
            
            @Override
            public void run() {
                if (distance >= maxDistance) {
                    cancel();
                    return;
                }
                Location current = start.clone().add(direction.clone().multiply(distance));
                current.getWorld().spawnParticle(Particle.DUST, current, 3, 0.1, 0.1, 0.1, 0,
                    new Particle.DustOptions(org.bukkit.Color.YELLOW, 1.0f));
                RayTraceResult result = current.getWorld().rayTraceEntities(current, direction, step, 
                    entity -> entity instanceof LivingEntity && entity != player);
                if (result != null && result.getHitEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) result.getHitEntity();
                    if (!(target instanceof Player)) {
                        hypnotizeMob(player, target);
                    }
                    cancel();
                    return;
                }
                
                if (current.getBlock().getType().isSolid()) {
                    cancel();
                    return;
                }
                
                distance += step;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    private void hypnotizeMob(Player player, LivingEntity mob) {
        Set<UUID> playerMobs = controlledMobs.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        int maxMobs = plugin.getConfig().getInt("weapons.hypnosis_staff.max_controlled_mobs", 10);
        if (playerMobs.size() >= maxMobs) {
            player.sendMessage("§cВы уже контролируете максимальное количество мобов (" + maxMobs + ")!");
            return;
        }
        
        UUID previousOwner = mobOwners.get(mob.getUniqueId());
        if (previousOwner != null) {
            if (previousOwner.equals(player.getUniqueId())) {
                player.sendMessage("§cЭтот моб уже под вашим контролем.");
            } else {
                player.sendMessage("§cЭтот моб уже контролируется другим игроком.");
            }
            return;
        }
        
        playerMobs.add(mob.getUniqueId());
        mobOwners.put(mob.getUniqueId(), player.getUniqueId());
        addToTeam(player, mob);
        applyOwnerName(mob, player);
        if (mob.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            double maxHealth = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            mob.setHealth(Math.min(mob.getHealth() + 40, maxHealth));
        }
        mob.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
        mob.getWorld().spawnParticle(Particle.HEART, mob.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
    }
    private void addToTeam(Player player, LivingEntity mob) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "hypnosis_" + player.getUniqueId().toString().substring(0, 8);
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.setAllowFriendlyFire(false);
        }
        team.addEntry(mob.getUniqueId().toString());
        team.addEntry(player.getName());
    }
    private void removeFromTeam(LivingEntity mob, UUID ownerId) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "hypnosis_" + ownerId.toString().substring(0, 8);
        Team team = scoreboard.getTeam(teamName);
        if (team != null) {
            team.removeEntry(mob.getUniqueId().toString());
        }
    }
    private void removeGlowingEffect(LivingEntity mob) {
        mob.removePotionEffect(PotionEffectType.GLOWING);
    }
    private void applyOwnerName(LivingEntity mob, Player owner) {
        Component name = Component.text("Хозяин: " + owner.getName())
            .color(NamedTextColor.YELLOW)
            .decorate(TextDecoration.BOLD);
        mob.customName(name);
        mob.setCustomNameVisible(true);
    }
    
    private void removeOwnerName(LivingEntity mob) {
        mob.customName(null);
        mob.setCustomNameVisible(false);
    }
    
    private void startTeleportTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                double maxDistance = plugin.getConfig().getDouble("weapons.hypnosis_staff.max_distance", 30.0);
                double followSpeed = plugin.getConfig().getDouble("weapons.hypnosis_staff.follow_speed", 1.0);
                for (Map.Entry<UUID, Set<UUID>> entry : controlledMobs.entrySet()) {
                    UUID ownerId = entry.getKey();
                    Player owner = Bukkit.getPlayer(ownerId);
                    if (owner == null || !owner.isOnline()) {
                        continue;
                    }
                    GameMode ownerGameMode = owner.getGameMode();
                    Set<UUID> mobIds = new HashSet<>(entry.getValue());
                    if (ownerGameMode != GameMode.CREATIVE && ownerGameMode != GameMode.SURVIVAL) {
                        for (UUID mobId : mobIds) {
                            Entity entity = Bukkit.getEntity(mobId);
                            if (entity instanceof Mob mobEntity) {
                                mobEntity.getPathfinder().stopPathfinding();
                                mobEntity.setTarget(null);
                            }
                        }
                        continue;
                    }             
                    for (UUID mobId : mobIds) {
                        Entity entity = Bukkit.getEntity(mobId);
                        if (!(entity instanceof LivingEntity)) {
                            entry.getValue().remove(mobId);
                            mobOwners.remove(mobId);
                            continue;
                        }
                        
                        LivingEntity mob = (LivingEntity) entity;
                        if (mob.isDead() || !mob.isValid()) {
                            entry.getValue().remove(mobId);
                            mobOwners.remove(mobId);
                            removeFromTeam(mob, ownerId);
                            removeOwnerName(mob);
                            removeGlowingEffect(mob);
                            continue;
                        }
                        
                        double distance = mob.getLocation().distance(owner.getLocation());
                        if (distance > maxDistance) {
                            Location teleportLoc = owner.getLocation().clone();
                            teleportLoc.add(owner.getLocation().getDirection().multiply(-2));
                            teleportLoc.setY(owner.getLocation().getY());
                            if (teleportLoc.getBlock().getType().isSolid()) {
                                teleportLoc.add(0, 1, 0);
                            }
                            
                            mob.teleport(teleportLoc);
                            mob.getWorld().spawnParticle(Particle.PORTAL, mob.getLocation(), 20, 0.5, 0.5, 0.5, 0);
                            continue;
                        }
                        
                        Player enemy = findEnemyPlayer(mob, owner);
                        if (enemy != null && mob instanceof Mob mobEntity) {
                            mobEntity.setTarget(enemy);
                            continue;
                        }
                        if (mob instanceof Mob mobEntity && mobEntity.getTarget() instanceof Player targetPlayer && !targetPlayer.getUniqueId().equals(ownerId)) {
                            if (targetPlayer.getLocation().distanceSquared(mob.getLocation()) > 25) {
                                mobEntity.setTarget(null);
                            } else {
                                continue;
                            }
                        }
                        
                        if (distance > 1.5) {
                            if (mob instanceof Mob mobEntity) {
                                mobEntity.getPathfinder().moveTo(owner, followSpeed);
                            }
                        } else if (mob instanceof Mob mobEntity) {
                            mobEntity.getPathfinder().stopPathfinding();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
    
    private Player findEnemyPlayer(LivingEntity mob, Player owner) {
        for (Player candidate : mob.getWorld().getPlayers()) {
            if (!candidate.isOnline() || candidate.getUniqueId().equals(owner.getUniqueId())) {
                continue;
            }
            if (candidate.getLocation().distanceSquared(mob.getLocation()) <= 25) {
                return candidate;
            }
        }
        return null;
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        cooldowns.remove(playerId);
    }
}
