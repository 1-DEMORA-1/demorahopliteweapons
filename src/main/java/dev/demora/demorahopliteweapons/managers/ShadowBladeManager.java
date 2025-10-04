package dev.demora.demorahopliteweapons.managers;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ShadowBladeManager implements Listener {
    
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey shadowBladeKey;
    
    private final Map<UUID, Integer> shadowTimers = new HashMap<>();
    private final Map<UUID, Integer> shadowCooldowns = new HashMap<>();
    private final List<Player> vanishedPlayers = new ArrayList<>();
    
    private int shadowDuration;
    private int cooldownTime;
    
    public ShadowBladeManager(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.shadowBladeKey = new NamespacedKey(plugin, "shadow_blade");
        loadConfig();
    }
    
    public void loadConfig() {
        shadowDuration = plugin.getConfig().getInt("weapons.shadow_blade.duration", 200);
        cooldownTime = plugin.getConfig().getInt("weapons.shadow_blade.cooldown", 20) * 20;
    }
    
    public void reloadConfig() {
        loadConfig();
    }
    
    public ItemStack createShadowBlade() {
        ItemStack shadowBlade = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = shadowBlade.getItemMeta();
        
        if (meta != null) {
            meta.displayName(Component.text("§8§lТеневой клинок"));
            meta.setCustomModelData(4);
            meta.getPersistentDataContainer().set(shadowBladeKey, PersistentDataType.BOOLEAN, true);
            
            meta.addEnchant(Enchantment.SHARPNESS, 5, true);
            meta.setUnbreakable(true);
            
            shadowBlade.setItemMeta(meta);
        }
        
        return shadowBlade;
    }
    
    public boolean isShadowBlade(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        return item.getItemMeta().getPersistentDataContainer().has(shadowBladeKey, PersistentDataType.BOOLEAN);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (item == null || !isShadowBlade(item)) {
            return;
        }
        
        event.setCancelled(true);
        
        UUID playerId = player.getUniqueId();
        
        if (shadowCooldowns.containsKey(playerId) && shadowCooldowns.get(playerId) > 0) {
            int remainingSeconds = (int) Math.ceil(shadowCooldowns.get(playerId) / 20.0);
            player.sendMessage("§cТеневой клинок перезаряжается! Осталось: §e" + remainingSeconds + " сек");
            return;
        }
        
        if (shadowTimers.containsKey(playerId) && shadowTimers.get(playerId) > 0) {
            return;
        }
        
        activateShadowBlade(player);
    }
    
    private void activateShadowBlade(Player player) {
        UUID playerId = player.getUniqueId();
        
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                onlinePlayer.hidePlayer(plugin, player);
            }
        }
        vanishedPlayers.add(player);
        
        shadowTimers.put(playerId, shadowDuration);
        
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        
        Location loc = player.getLocation();
        loc.add(0, 0.5, 0);
        World world = player.getWorld();
        world.spawnParticle(Particle.SMOKE, loc, 100, 0.5, 0.5, 0.5, 0.1);
    }
    
    public void updateTimers() {
        Iterator<Map.Entry<UUID, Integer>> timerIterator = shadowTimers.entrySet().iterator();
        while (timerIterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = timerIterator.next();
            UUID playerId = entry.getKey();
            int timer = entry.getValue();
            
            Player player = plugin.getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                timerIterator.remove();
                continue;
            }
            
            if (timer > 0) {
                if (timer >= 20) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 25, 2, true, false));
                }
                
                shadowTimers.put(playerId, timer - 1);
            } else {
                returnFromShadow(player);
                timerIterator.remove();
            }
        }
        
        Iterator<Map.Entry<UUID, Integer>> cooldownIterator = shadowCooldowns.entrySet().iterator();
        while (cooldownIterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = cooldownIterator.next();
            UUID playerId = entry.getKey();
            int cooldown = entry.getValue();
            
            if (cooldown > 0) {
                shadowCooldowns.put(playerId, cooldown - 1);
            } else {
                Player player = plugin.getServer().getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 1.5f);
                }
                cooldownIterator.remove();
            }
        }
    }
    
    private void returnFromShadow(Player player) {
        UUID playerId = player.getUniqueId();
        
        vanishedPlayers.remove(player);
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                onlinePlayer.showPlayer(plugin, player);
            }
        }
        
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, 1.0f, 1.2f);
        
        player.sendMessage("§c§lВы снова видимы.");
        
        shadowCooldowns.put(playerId, cooldownTime);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Player vanishedPlayer : vanishedPlayers) {
            event.getPlayer().hidePlayer(plugin, vanishedPlayer);
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            UUID victimId = victim.getUniqueId();
            
            if (shadowTimers.containsKey(victimId) && shadowTimers.get(victimId) > 0) {
                returnFromShadow(victim);
                shadowTimers.remove(victimId);
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        
        shadowTimers.remove(playerId);
        shadowCooldowns.remove(playerId);
        vanishedPlayers.remove(event.getPlayer());
    }
}
