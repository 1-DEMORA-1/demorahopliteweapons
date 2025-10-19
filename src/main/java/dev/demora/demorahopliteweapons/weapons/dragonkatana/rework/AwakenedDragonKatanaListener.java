package dev.demora.demorahopliteweapons.weapons.dragonkatana.rework;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class AwakenedDragonKatanaListener implements Listener {
    
    private final DemoraHopliteWeapons plugin;
    private final AwakenedDragonKatana awakenedKatana;
    
    public AwakenedDragonKatanaListener(DemoraHopliteWeapons plugin, AwakenedDragonKatana awakenedKatana) {
        this.plugin = plugin;
        this.awakenedKatana = awakenedKatana;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!awakenedKatana.isAwakenedDragonKatana(item)) {
            return;
        }
        
        Action a = event.getAction();
        boolean canDash = (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK);
        
        if (canDash && player.isSneaking()) {
            event.setCancelled(true);
            
            if (awakenedKatana.isOnCooldown(player)) {
                long remaining = awakenedKatana.getRemainingCooldown(player);
                player.sendMessage("§cПробуждённая Драконья катана перезаряжается! Осталось: §e" + remaining + " §cсек");
                return;
            }
            
            player.getInventory().setItemInMainHand(null);
            awakenedKatana.swapAllPlayers(player);
            awakenedKatana.setCooldown(player);
        } else if (canDash && !player.isSneaking()) {
            event.setCancelled(true);
            
            if (awakenedKatana.isOnTeleportCooldown(player)) {
                long remaining = awakenedKatana.getTeleportRemainingCooldown(player);
                player.sendMessage("§cПробуждённая Драконья катана перезаряжается! Осталось: §e" + remaining + " §cсек");
                return;
            }
            
            Location fromLoc = player.getLocation().clone();
            plugin.getDragonKatana().performDash(player);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location toLoc = player.getLocation().clone();
                    awakenedKatana.handleTeleport(player, fromLoc, toLoc);
                }
            }.runTaskLater(plugin, 1L);
        }
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!awakenedKatana.isAwakenedDragonKatana(item)) {
            return;
        }
        
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL ||
            event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            
            awakenedKatana.handleTeleport(player, event.getFrom(), event.getTo());
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        awakenedKatana.removePlayerData(event.getPlayer().getUniqueId());
    }
}
