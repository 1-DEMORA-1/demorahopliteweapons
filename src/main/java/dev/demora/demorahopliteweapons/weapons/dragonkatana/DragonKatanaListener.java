package dev.demora.demorahopliteweapons.weapons.dragonkatana;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class DragonKatanaListener implements Listener {
    
    private final DragonKatana dragonKatana;
    
    public DragonKatanaListener(DragonKatana dragonKatana) {
        this.dragonKatana = dragonKatana;
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Action a = e.getAction();
        boolean canDash = (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK);
        if (!canDash) return;
        
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (!dragonKatana.isKatana(item)) return;
        
        e.setCancelled(true);
        
        if (dragonKatana.isOnCooldown(p)) {
            long remaining = dragonKatana.getRemainingCooldown(p);
            p.sendMessage("§cДраконья катана перезаряжается! Осталось: §e" + remaining + " сек");
            return;
        }
        
        dragonKatana.performDash(p);
    }
    
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        ItemStack newItem = p.getInventory().getItem(e.getNewSlot());
        boolean hasKatanaNow = dragonKatana.isKatana(newItem);
        dragonKatana.setHasKatana(p.getUniqueId(), hasKatanaNow);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        dragonKatana.removePlayerData(e.getPlayer().getUniqueId());
    }
}
