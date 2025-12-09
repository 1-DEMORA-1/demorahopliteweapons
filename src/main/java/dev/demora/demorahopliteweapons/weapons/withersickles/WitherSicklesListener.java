package dev.demora.demorahopliteweapons.weapons.withersickles;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Random;
import java.util.UUID;
public class WitherSicklesListener implements Listener {
    private final DemoraHopliteWeapons plugin;
    private final WitherSickles witherSickles;
    private final SickleCooldownManager cooldownManager;
    private final SickleInventoryManager inventoryManager;
    private final SickleThrowManager throwManager;
    private final SickleActionBarManager actionBarManager;
    private final Random random = new Random();
    public WitherSicklesListener(DemoraHopliteWeapons plugin, WitherSickles witherSickles) {
        this.plugin = plugin;
        this.witherSickles = witherSickles;
        this.cooldownManager = new SickleCooldownManager(plugin);
        this.inventoryManager = new SickleInventoryManager(plugin, witherSickles, cooldownManager);
        this.throwManager = new SickleThrowManager(plugin, witherSickles, cooldownManager, inventoryManager);
        this.actionBarManager = new SickleActionBarManager(plugin, witherSickles, cooldownManager, inventoryManager);
        this.actionBarManager.startActionBarTask();
    }
    
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());
        if (witherSickles.isWitherSickle(oldItem) && !witherSickles.isWitherSickle(newItem)) {
            inventoryManager.removeOffhandSickle(player);
        }
        
        if (witherSickles.isWitherSickle(newItem) && !cooldownManager.isOnCooldown(player)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> inventoryManager.equipOffhandSickle(player), 1L);
        }
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
    public void onDropItem(PlayerDropItemEvent event) {
        inventoryManager.handleDropItem(event);
    }
    
    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        inventoryManager.handleSwapHands(event);
    }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getDamager();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        if (witherSickles.isWitherSickle(offHand)) {
            return;
        }
        
        if (witherSickles.isAdditionalSickle(mainHand)) {
            return;
        }
        
        if (!witherSickles.isWitherSickle(mainHand)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity target = (LivingEntity) event.getEntity();
        double hitChance = plugin.getConfig().getDouble("weapons.wither_sickles.wither_chance_hit", 0.1);
        if (random.nextDouble() < hitChance) {
            int duration = plugin.getConfig().getInt("weapons.wither_sickles.wither_duration_ticks", 60);
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration, 0, true, true));
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
        if (!witherSickles.isWitherSickle(mainHand) || !witherSickles.isAdditionalSickle(offHand)) {
            return;
        }
        event.setCancelled(true);
        if (cooldownManager.isOnCooldown(player)) {
            long remaining = cooldownManager.getRemainingCooldown(player);
            player.sendMessage("§cОткат на бросок. Осталось §e" + remaining + "§c сек.");
            return;
        }
        
        player.getInventory().setItemInOffHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
        inventoryManager.setOffhandSickle(player, false);
        cooldownManager.setCooldown(player);
        throwManager.throwSickle(player);
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        
        if (witherSickles.isWitherSickle(mainHand) && !cooldownManager.isOnCooldown(player)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> inventoryManager.equipOffhandSickle(player), 1L);
        }
    }
    
    @EventHandler
    public void onDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player player = event.getEntity();
        ItemStack off = player.getInventory().getItemInOffHand();
        if (witherSickles.isAdditionalSickle(off)) {
            player.getInventory().setItemInOffHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
        }
        event.getDrops().removeIf(witherSickles::isAdditionalSickle);
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        cooldownManager.removePlayerData(id);
        inventoryManager.removePlayerData(id);
    }
}
