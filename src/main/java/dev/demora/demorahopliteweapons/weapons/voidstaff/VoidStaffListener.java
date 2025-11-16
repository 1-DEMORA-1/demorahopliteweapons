package dev.demora.demorahopliteweapons.weapons.voidstaff;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class VoidStaffListener implements Listener {
    
    private final VoidStaff voidStaff;
    
    public VoidStaffListener(VoidStaff voidStaff) {
        this.voidStaff = voidStaff;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!voidStaff.isVoidStaff(item)) {
            return;
        }
        
        Action action = event.getAction();
        VoidStaff.StaffMode mode = voidStaff.getStaffMode(item);
        
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            voidStaff.switchMode(player, item);
            return;
        }
        
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            
            if (mode == VoidStaff.StaffMode.PORTAL) {
                if (voidStaff.isOnPortalCooldown(player)) {
                    long remaining = voidStaff.getRemainingPortalCooldown(player);
                    player.sendMessage("§cПосох пустоты перезаряжается! Осталось: §e" + remaining + " §cсек");
                    return;
                }
                
                voidStaff.usePortalMode(player);
            } else if (mode == VoidStaff.StaffMode.SHULKER_CHARGE) {
                if (voidStaff.isOnShulkerCooldown(player)) {
                    long remaining = voidStaff.getRemainingShulkerCooldown(player);
                    player.sendMessage("§cЗаряд шалкера перезаряжается! Осталось: §e" + remaining + " §cсек");
                    return;
                }
                
                voidStaff.useShulkerMode(player);
            }
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        
        if (voidStaff.isVoidStaff(mainHand)) {
            VoidStaff.StaffMode mode = voidStaff.getStaffMode(mainHand);
            voidStaff.updateActionBar(player, mode);
        }
        
        if (event.getTo() == null || event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }
        
        if (event.getTo().getBlock().getType() == Material.END_GATEWAY) {
            if (voidStaff.getPortalManager().isPortalBlock(event.getTo().getBlock().getLocation())) {
                voidStaff.getPortalManager().handlePortalTouch(player, event.getTo().getBlock().getLocation());
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        voidStaff.removePlayerData(event.getPlayer().getUniqueId());
    }
}
