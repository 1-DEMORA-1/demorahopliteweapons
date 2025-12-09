package dev.demora.demorahopliteweapons.weapons.withersickles.rework;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class AwakenedSickleInventoryManager {
    private final DemoraHopliteWeapons plugin;
    private final AwakenedWitherSickles awakenedSickles;
    private final Map<UUID, Boolean> offhandSickles = new HashMap<>();
    public AwakenedSickleInventoryManager(DemoraHopliteWeapons plugin, AwakenedWitherSickles awakenedSickles) {
        this.plugin = plugin;
        this.awakenedSickles = awakenedSickles;
    }
    public void equipOffhandSickle(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand == null || offHand.getType() == Material.AIR) {
            player.getInventory().setItemInOffHand(awakenedSickles.createAdditionalAwakenedSickle());
            offhandSickles.put(player.getUniqueId(), true);
        }
    }
    
    public void removeOffhandSickle(Player player) {
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (awakenedSickles.isAdditionalAwakenedSickle(offHand)) {
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            offhandSickles.remove(player.getUniqueId());
        }
    }
    
    public void setOffhandSickle(Player player, boolean has) {
        if (has) {
            offhandSickles.put(player.getUniqueId(), true);
        } else {
            offhandSickles.remove(player.getUniqueId());
        }
    }
    
    public boolean hasOffhandSickle(Player player) {
        return offhandSickles.getOrDefault(player.getUniqueId(), false);
    }
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (awakenedSickles.isAwakenedWitherSickle(offHand)) {
            event.setCancelled(true);
            return;
        }
        
        if (awakenedSickles.isAdditionalAwakenedSickle(offHand)) {
            boolean isOffhandSlot = (event.getSlotType() == InventoryType.SlotType.ARMOR && event.getSlot() == 40);
            if (isOffhandSlot) {
                event.setCancelled(true);
                return;
            }
            
            boolean isOffhandSickleInCursor = awakenedSickles.isAdditionalAwakenedSickle(cursor) && cursor != null && cursor.isSimilar(offHand);
            boolean isOffhandSickleInClicked = awakenedSickles.isAdditionalAwakenedSickle(clicked) && clicked != null && clicked.isSimilar(offHand);
            if (!isPlayerInventory(event.getView().getTopInventory())) {
                if (isOffhandSickleInCursor || isOffhandSickleInClicked) {
                    event.setCancelled(true);
                    return;
                }
            }
            
            InventoryAction action = event.getAction();
            if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                action == InventoryAction.HOTBAR_MOVE_AND_READD ||
                action == InventoryAction.HOTBAR_SWAP) {
                if (isOffhandSickleInCursor || isOffhandSickleInClicked) {
                    event.setCancelled(true);
                    return;
                }
            }
            
            if (isOffhandSickleInCursor || isOffhandSickleInClicked) {
                event.setCancelled(true);
                return;
            }
        }
        
        if (event.getSlot() == player.getInventory().getHeldItemSlot()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (!awakenedSickles.isAwakenedWitherSickle(mainHand)) {
                    removeOffhandSickle(player);
                }
            }, 1L);
        }
    }    
    public void handleInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (awakenedSickles.isAdditionalAwakenedSickle(offHand)) {
            for (int slot : event.getRawSlots()) {
                if (slot == 45) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
    
    public void handleSwapHands(PlayerSwapHandItemsEvent event) {
        ItemStack mainHand = event.getMainHandItem();
        ItemStack offHand = event.getOffHandItem();
        if (awakenedSickles.isAwakenedWitherSickle(mainHand) || awakenedSickles.isAdditionalAwakenedSickle(mainHand) ||
            awakenedSickles.isAwakenedWitherSickle(offHand) || awakenedSickles.isAdditionalAwakenedSickle(offHand)) {
            event.setCancelled(true);
        }
    }
    
    public void handleDropItem(PlayerDropItemEvent event) {
        ItemStack dropped = event.getItemDrop().getItemStack();
        Player player = event.getPlayer();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (awakenedSickles.isAwakenedWitherSickle(dropped) || awakenedSickles.isAdditionalAwakenedSickle(dropped)) {
            if (awakenedSickles.isAdditionalAwakenedSickle(offHand)) {
                removeOffhandSickle(player);
            }
        }
    }
    
    private boolean isPlayerInventory(Inventory inventory) {
        return inventory.getType() == InventoryType.PLAYER || 
               inventory.getType() == InventoryType.CRAFTING;
    }
}

