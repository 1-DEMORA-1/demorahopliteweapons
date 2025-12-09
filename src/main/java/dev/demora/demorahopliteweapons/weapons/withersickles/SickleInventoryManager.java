package dev.demora.demorahopliteweapons.weapons.withersickles;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class SickleInventoryManager {
    private final DemoraHopliteWeapons plugin;
    private final WitherSickles witherSickles;
    private final Map<UUID, Boolean> hasOffhandSickle = new HashMap<>();
    public SickleInventoryManager(DemoraHopliteWeapons plugin, WitherSickles witherSickles, SickleCooldownManager cooldownManager) {
        this.plugin = plugin;
        this.witherSickles = witherSickles;
    }
    public void equipOffhandSickle(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!witherSickles.isWitherSickle(mainHand)) {
            return;
        }
        
        PlayerInventory inv = player.getInventory();
        ItemStack offHand = inv.getItemInOffHand();
        if (witherSickles.isAdditionalSickle(offHand)) {
            hasOffhandSickle.put(player.getUniqueId(), true);
            return;
        }
        
        ItemStack secondSickle = witherSickles.createAdditionalSickle();
        
        if (offHand != null && offHand.getType() != Material.AIR) {
            int emptySlot = inv.firstEmpty();
            if (emptySlot != -1) {
                inv.setItem(emptySlot, offHand);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), offHand);
            }
        }
        
        inv.setItemInOffHand(secondSickle);
        hasOffhandSickle.put(player.getUniqueId(), true);
    }
    
    public void removeOffhandSickle(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack offHand = inv.getItemInOffHand();
        
        if (witherSickles.isAdditionalSickle(offHand)) {
            inv.setItemInOffHand(new ItemStack(Material.AIR));
        }
        hasOffhandSickle.remove(player.getUniqueId());
    }
    
    public boolean hasOffhandSickle(Player player) {
        return hasOffhandSickle.containsKey(player.getUniqueId()) && hasOffhandSickle.get(player.getUniqueId());
    }
    
    public void setOffhandSickle(Player player, boolean value) {
        hasOffhandSickle.put(player.getUniqueId(), value);
    }
    
    public void handleInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        boolean isPlayerInventory = event.getView().getTopInventory() == null || 
                                   event.getView().getTopInventory().getType() == org.bukkit.event.inventory.InventoryType.PLAYER;
        if (witherSickles.isWitherSickle(offHand)) {
            event.setCancelled(true);
            return;
        }
        
        if (witherSickles.isAdditionalSickle(offHand)) {
            boolean isOffhandSlot = (event.getSlotType() == InventoryType.SlotType.ARMOR && event.getSlot() == 40);
            if (isOffhandSlot) {
                event.setCancelled(true);
                return;
            }
            
            boolean isOffhandSickleInCursor = witherSickles.isAdditionalSickle(cursor) && cursor != null && cursor.isSimilar(offHand);
            boolean isOffhandSickleInClicked = witherSickles.isAdditionalSickle(clicked) && clicked != null && clicked.isSimilar(offHand);
            if (!isPlayerInventory) {
                if (isOffhandSickleInCursor || isOffhandSickleInClicked) {
                    event.setCancelled(true);
                    return;
                }
            }
            
            InventoryAction action = event.getAction();
            if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (isOffhandSickleInCursor || isOffhandSickleInClicked) {
                    event.setCancelled(true);
                    return;
                }
            }
            
            if (action == InventoryAction.HOTBAR_MOVE_AND_READD || action == InventoryAction.HOTBAR_SWAP) {
                if (isOffhandSickleInCursor || isOffhandSickleInClicked) {
                    event.setCancelled(true);
                    return;
                }
            }
            
            if (isOffhandSickleInCursor) {
                event.setCancelled(true);
                return;
            }
            
            if (isOffhandSickleInClicked) {
                event.setCancelled(true);
                return;
            }
            if (event.getClick().isShiftClick() && (isOffhandSickleInCursor || isOffhandSickleInClicked)) {
                event.setCancelled(true);
                return;
            }
            
            if (event.getHotbarButton() >= 0 && event.getHotbarButton() <= 8) {
                if (witherSickles.isWitherSickle(clicked) || witherSickles.isWitherSickle(cursor)) {
                    if (!isPlayerInventory) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (isOffhandSickleInCursor || isOffhandSickleInClicked) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        
        if (event.getSlot() == player.getInventory().getHeldItemSlot()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (!witherSickles.isWitherSickle(mainHand)) {
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
        ItemStack dragged = event.getOldCursor();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        if (witherSickles.isWitherSickle(offHand) || witherSickles.isWitherSickle(dragged)) {
            event.setCancelled(true);
            return;
        }
        
        if (witherSickles.isAdditionalSickle(offHand) && witherSickles.isAdditionalSickle(dragged)) {
            if (dragged.isSimilar(offHand)) {
                event.setCancelled(true);
                return;
            }
        }
        
        if (event.getInventorySlots().contains(40)) {
            if (witherSickles.isAdditionalSickle(offHand) && witherSickles.isAdditionalSickle(dragged) && dragged.isSimilar(offHand)) {
                event.setCancelled(true);
                return;
            }
        }
        
        for (int slot : event.getRawSlots()) {
            if (slot == 40) {
                if (witherSickles.isAdditionalSickle(offHand) && witherSickles.isAdditionalSickle(dragged) && dragged.isSimilar(offHand)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
    
    public void handleDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack dropped = event.getItemDrop().getItemStack();
        if (witherSickles.isWitherSickle(dropped) || witherSickles.isAdditionalSickle(dropped)) {
            removeOffhandSickle(player);
        }
    }
    
    public void handleSwapHands(PlayerSwapHandItemsEvent event) {
        ItemStack mainHand = event.getMainHandItem();
        ItemStack offHand = event.getOffHandItem();
        if (witherSickles.isWitherSickle(offHand) || witherSickles.isWitherSickle(mainHand)) {
            event.setCancelled(true);
        }
        
        if (witherSickles.isAdditionalSickle(offHand) && !witherSickles.isWitherSickle(mainHand)) {
            event.setCancelled(true);
        }
    }
    
    public void removePlayerData(UUID playerId) {
        hasOffhandSickle.remove(playerId);
    }
}


