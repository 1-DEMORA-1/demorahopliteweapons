package dev.demora.demorahopliteweapons.weapons.withersickles.rework;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.entity.Player;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.LivingEntity;
public class AwakenedWitherSicklesListener implements Listener {
    private final DemoraHopliteWeapons plugin;
    private final AwakenedWitherSickles awakenedSickles;
    private final AwakenedSickleInventoryManager inventoryManager;
    private final AwakenedSickleCooldownManager cooldownManager;
    private final AwakenedSickleThrowManager throwManager;
    private final AwakenedSickleActionBarManager actionBarManager;
    private final AwakenedSickleShieldManager shieldManager;
    public AwakenedWitherSicklesListener(DemoraHopliteWeapons plugin, AwakenedWitherSickles awakenedSickles) {
        this.plugin = plugin;
        this.awakenedSickles = awakenedSickles;
        this.cooldownManager = new AwakenedSickleCooldownManager(plugin);
        this.inventoryManager = new AwakenedSickleInventoryManager(plugin, awakenedSickles);
        this.throwManager = new AwakenedSickleThrowManager(plugin, awakenedSickles, cooldownManager, inventoryManager);
        this.actionBarManager = new AwakenedSickleActionBarManager(plugin, awakenedSickles, cooldownManager, inventoryManager);
        this.shieldManager = new AwakenedSickleShieldManager(plugin);
        this.actionBarManager.startActionBarTask();
    }
    
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());
        if (awakenedSickles.isAwakenedWitherSickle(oldItem)) {
            inventoryManager.removeOffhandSickle(player);
        }
        if (awakenedSickles.isAwakenedWitherSickle(newItem) && !cooldownManager.isOnCooldown(player)) {
            inventoryManager.equipOffhandSickle(player);
        }
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (!awakenedSickles.isAwakenedWitherSickle(mainHand)) {
            return;
        }
        if (!awakenedSickles.isAdditionalAwakenedSickle(offHand)) {
            return;
        }
        event.setCancelled(true);
        if (player.isSneaking()) {
            shieldManager.activateShield(player);
            return;
        }
        
        if (cooldownManager.isOnCooldown(player)) {
            long remaining = cooldownManager.getRemainingCooldown(player);
            player.sendMessage("§cСерпы перезаряжаются! Осталось: §e" + remaining + " §cсек");
            return;
        }
        
        inventoryManager.removeOffhandSickle(player);
        throwManager.throwSickle(player);
        cooldownManager.setCooldown(player);
    }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        Player player = (Player) event.getDamager();
        LivingEntity target = (LivingEntity) event.getEntity();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!awakenedSickles.isAwakenedWitherSickle(mainHand)) {
            return;
        }
        int fireDuration = plugin.getConfig().getInt("weapons.awakened_wither_sickles.fire_duration_ticks", 100);
        target.setFireTicks(fireDuration);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        inventoryManager.handleInventoryClick(event);
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        inventoryManager.handleInventoryDrag(event);
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (awakenedSickles.isAwakenedWitherSickle(mainHand) && !cooldownManager.isOnCooldown(player)) {
            inventoryManager.equipOffhandSickle(player);
        }
    }
    
    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        inventoryManager.handleSwapHands(event);
    }
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        inventoryManager.handleDropItem(event);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        if (shieldManager.isShieldActive(player)) {
            double reduction = shieldManager.getDamageReduction();
            double newDamage = event.getDamage() * (1.0 - reduction);
            event.setDamage(newDamage);
            player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.05);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(item -> awakenedSickles.isAdditionalAwakenedSickle(item));
        shieldManager.removePlayerData(event.getEntity().getUniqueId());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        inventoryManager.removeOffhandSickle(player);
        cooldownManager.removePlayerData(player.getUniqueId());
        shieldManager.removePlayerData(player.getUniqueId());
    }
    public void reloadConfig() {
        shieldManager.reloadConfig();
    }
}

