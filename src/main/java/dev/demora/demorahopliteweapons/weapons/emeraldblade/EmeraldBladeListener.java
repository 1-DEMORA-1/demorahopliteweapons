package dev.demora.demorahopliteweapons.weapons.emeraldblade;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
public class EmeraldBladeListener implements Listener {
    private final DemoraHopliteWeapons plugin;
    private final EmeraldBlade emeraldBlade;
    
    public EmeraldBladeListener(DemoraHopliteWeapons plugin, EmeraldBlade emeraldBlade) {
        this.plugin = plugin;
        this.emeraldBlade = emeraldBlade;
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!emeraldBlade.isEmeraldBlade(item)) {
            return;
        }
        event.setCancelled(true);
        openUpgradeMenu(player);
    }
    private void openUpgradeMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 18, Component.text("§a§lИзумрудный клинок"));
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        int storedEmeralds = emeraldBlade.getEmeraldCount(mainHand);
        int remaining = storedEmeralds;
        while (remaining > 0) {
            int stackSize = Math.min(64, remaining);
            menu.addItem(new ItemStack(Material.EMERALD, stackSize));
            remaining -= stackSize;
        }
        player.openInventory(menu);
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        if (!event.getView().title().equals(Component.text("§a§lИзумрудный клинок"))) {
            return;
        }
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }
        
        if (clickedInventory.equals(event.getView().getTopInventory())) {
            ItemStack cursor = event.getCursor();
            
            if (event.getAction().name().contains("PLACE") || event.getAction().name().contains("SWAP")) {
                if (cursor != null && cursor.getType() != Material.AIR && cursor.getType() != Material.EMERALD) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (event.getAction().name().contains("HOTBAR")) {
                ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                if (hotbarItem != null && hotbarItem.getType() != Material.AIR && hotbarItem.getType() != Material.EMERALD) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (event.getAction().name().contains("MOVE_TO_OTHER_INVENTORY")) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR && clicked.getType() != Material.EMERALD) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        if (!event.getView().title().equals(Component.text("§a§lИзумрудный клинок"))) {
            return;
        }
        
        ItemStack dragged = event.getOldCursor();
        if (dragged != null && dragged.getType() != Material.AIR && dragged.getType() != Material.EMERALD) {
            for (int slot : event.getRawSlots()) {
                if (slot < event.getView().getTopInventory().getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        if (!event.getView().title().equals(Component.text("§a§lИзумрудный клинок"))) {
            return;
        }
        Player player = (Player) event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        
        if (!emeraldBlade.isEmeraldBlade(mainHand)) {
            returnItemsToPlayer(player, event.getInventory());
            return;
        }
        int emeraldsInMenu = countEmeralds(event.getInventory());
        int previousEmeralds = emeraldBlade.getEmeraldCount(mainHand);
        ItemStack newBlade = emeraldBlade.createEmeraldBladeWithEmeralds(emeraldsInMenu);
        player.getInventory().setItemInMainHand(newBlade);
        event.getInventory().clear();
        if (emeraldsInMenu > previousEmeralds) {
            int emeraldsPerUpgrade = plugin.getConfig().getInt("weapons.emerald_blade.emeralds_per_upgrade", 64);
            int oldUpgrades = previousEmeralds / emeraldsPerUpgrade;
            int newUpgrades = emeraldsInMenu / emeraldsPerUpgrade;
            if (newUpgrades > oldUpgrades) {
                player.sendMessage("§aОружие улучшено! Уровень улучшений: §e" + newUpgrades);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            }
        } else if (emeraldsInMenu < previousEmeralds) {
            int emeraldsPerUpgrade = plugin.getConfig().getInt("weapons.emerald_blade.emeralds_per_upgrade", 64);
            int oldUpgrades = previousEmeralds / emeraldsPerUpgrade;
            int newUpgrades = emeraldsInMenu / emeraldsPerUpgrade;
            if (newUpgrades < oldUpgrades) {
                player.sendMessage("§cУровень улучшений снижен: §e" + newUpgrades);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 0.8f);
            }
        }
    }
    
    private int countEmeralds(Inventory inventory) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == Material.EMERALD) {
                count += item.getAmount();
            }
        }
        return count;
    }
    private void returnItemsToPlayer(Player player, Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                player.getInventory().addItem(item);
            }
        }
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }
        Player killer = event.getEntity().getKiller();
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (!emeraldBlade.isEmeraldBlade(weapon)) {
            return;
        }
        int emeraldsForPlayer = plugin.getConfig().getInt("weapons.emerald_blade.emeralds_per_player_kill", 20);
        int emeraldsForMob = plugin.getConfig().getInt("weapons.emerald_blade.emeralds_per_mob_kill", 1);
        int amount = event.getEntity() instanceof Player ? emeraldsForPlayer : emeraldsForMob;
        if (amount <= 0) {
            return;
        }
        event.getDrops().add(new ItemStack(Material.EMERALD, amount));
    }
}

