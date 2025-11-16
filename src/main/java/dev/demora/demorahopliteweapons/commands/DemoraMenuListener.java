package dev.demora.demorahopliteweapons.commands;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class DemoraMenuListener implements Listener {
    
    private final DemoraHopliteWeapons plugin;
    
    public DemoraMenuListener(DemoraHopliteWeapons plugin, DemoraMenuCommand menuCommand) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (event.getView().getTitle().equals("§c§lЛегендарные оружия")) {
            event.setCancelled(true);
            
            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }
            
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            

            if (clickedItem == null || !clickedItem.hasItemMeta()) {
                return;
            }
            ItemStack weaponToGive = clickedItem.clone();
            player.getInventory().addItem(weaponToGive);
            String weaponName = clickedItem.getItemMeta().getDisplayName();
            player.sendMessage(plugin.getMessage("prefix") + "§aВы получили " + weaponName + "§a!");
            player.closeInventory();
        }
    }
}

