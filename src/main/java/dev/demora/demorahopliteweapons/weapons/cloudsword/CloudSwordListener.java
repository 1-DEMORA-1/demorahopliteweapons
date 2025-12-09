package dev.demora.demorahopliteweapons.weapons.cloudsword;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class CloudSwordListener implements Listener {
    private final DemoraHopliteWeapons plugin;
    private final CloudSword cloudSword;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, CloudAbility> activeAbilities = new HashMap<>();
    private final Map<UUID, Long> lastClickTime = new HashMap<>();
    
    public CloudSwordListener(DemoraHopliteWeapons plugin, CloudSword cloudSword) {
        this.plugin = plugin;
        this.cloudSword = cloudSword;
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!cloudSword.isCloudSword(item)) {
            return;
        }
        event.setCancelled(true);
        UUID playerId = player.getUniqueId();
        Long lastClick = lastClickTime.get(playerId);
        if (lastClick != null && System.currentTimeMillis() - lastClick < 300) {
            return;
        }
        lastClickTime.put(playerId, System.currentTimeMillis());
        CloudAbility ability = activeAbilities.get(playerId);
        if (ability != null) {
            if (ability.isPaused()) {
                if (!ability.canResume()) {
                    return;
                }
                ability.resume();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
            } else {
                if (!ability.canPause()) {
                    return;
                }
                ability.pause();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.8f);
            }
            return;
        }
        Long cooldownEnd = cooldowns.get(player.getUniqueId());
        if (cooldownEnd != null && System.currentTimeMillis() < cooldownEnd) {
            long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
            player.sendMessage("§cОткат на способность. Осталось §e" + remaining + "§c сек.");
            return;
        }
        
        startCloudAbility(player);
    }
    
    private void startCloudAbility(Player player) {
        int duration = plugin.getConfig().getInt("weapons.cloud_sword.ability_duration", 10);
        int cloudLifetimeTicks = plugin.getConfig().getInt("weapons.cloud_sword.cloud_lifetime_ticks", 40);
        CloudAbility ability = new CloudAbility(player, duration, cloudLifetimeTicks);
        activeAbilities.put(player.getUniqueId(), ability);
        ability.start();
        
        player.playSound(player.getLocation(), Sound.BLOCK_SNOW_PLACE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1.0f, 1.5f);
    }
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (cloudSword.isCloudSword(item)) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        CloudAbility ability = activeAbilities.remove(id);
        if (ability != null) {
            ability.cancel();
        }
        cooldowns.remove(id);
        lastClickTime.remove(id);
    }
    
    private class CloudAbility extends BukkitRunnable {
        private final Player player;
        private final int maxDuration;
        private int ticksElapsed = 0;
        private boolean paused = false;
        private final Map<Location, Material> originalBlocks = new HashMap<>();
        private final int cloudLifetimeTicks;
        private long startTime;
        private long lastPauseToggleTime = 0;
        
        public CloudAbility(Player player, int durationSeconds, int cloudLifetimeTicks) {
            this.player = player;
            this.maxDuration = durationSeconds * 20;
            this.cloudLifetimeTicks = cloudLifetimeTicks;
        }
        public void start() {
            this.startTime = System.currentTimeMillis();
            runTaskTimer(plugin, 0L, 1L);
        }
        public boolean canPause() {
            long currentTime = System.currentTimeMillis();
            return currentTime - startTime >= 250 && currentTime - lastPauseToggleTime >= 300;
        }
        public boolean canResume() {
            long currentTime = System.currentTimeMillis();
            return currentTime - lastPauseToggleTime >= 300;
        }
        public void pause() {
            paused = true;
            lastPauseToggleTime = System.currentTimeMillis();
        }
        public void resume() {
            paused = false;
            lastPauseToggleTime = System.currentTimeMillis();
        }
        public boolean isPaused() {
            return paused;
        }
        @Override
        public void run() {
            if (!player.isOnline() || !cloudSword.isCloudSword(player.getInventory().getItemInMainHand())) {
                finish();
                return;
            }
            if (!paused) {
                ticksElapsed++;
                if (ticksElapsed % 2 == 0) {
                    spawnClouds();
                }
                if (ticksElapsed >= maxDuration) {
                    finish();
                    return;
                }
            }
            updateActionBar();
        }
        private void spawnClouds() {
            Location playerLoc = player.getLocation();
            int y = playerLoc.getBlockY() - 1;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Location blockLoc = new Location(player.getWorld(), 
                        playerLoc.getBlockX() + x, y, playerLoc.getBlockZ() + z);
                    Block block = blockLoc.getBlock();
                    if (block.getType() == Material.AIR || block.getType() == Material.SNOW_BLOCK) {
                        if (!originalBlocks.containsKey(blockLoc)) {
                            originalBlocks.put(blockLoc.clone(), block.getType());
                        }
                        block.setType(Material.SNOW_BLOCK);
                        Location particleLoc = blockLoc.clone().add(0.5, 0.8, 0.5);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (block.getType() == Material.SNOW_BLOCK) {
                                Material original = originalBlocks.getOrDefault(blockLoc, Material.AIR);
                                block.setType(original);
                                player.getWorld().spawnParticle(Particle.SNOWFLAKE, particleLoc, 12, 0.2, 0.2, 0.2, 0.0);
                            }
                        }, cloudLifetimeTicks);
                    }
                }
            }
        }
        
        private void updateActionBar() {
            int totalSegments = 6;
            double progress = (double) ticksElapsed / maxDuration;
            int filled = (int) Math.round(totalSegments * Math.max(0, Math.min(1.0, progress)));
            int brightCount = totalSegments - filled;
            StringBuilder bar = new StringBuilder("");
            for (int i = 0; i < totalSegments; i++) {
                if (i < brightCount) {
                    bar.append("§f⍣");
                } else {
                    bar.append("§7⍣");
                }
                if (i < totalSegments - 1) {
                    bar.append(" ");
                }
            }
            bar.append("");
            if (paused) {
                bar.append(" §fПАУЗА");
            }
            
            player.sendActionBar(Component.text(bar.toString()));
        }
        private void finish() {
            cancel();
            activeAbilities.remove(player.getUniqueId());
            
            int cooldownSeconds = plugin.getConfig().getInt("weapons.cloud_sword.cooldown", 25);
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldownSeconds * 1000L));
            
            player.sendActionBar(Component.text(""));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
        }
    }
}

