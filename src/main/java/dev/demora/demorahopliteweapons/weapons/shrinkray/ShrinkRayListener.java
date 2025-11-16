package dev.demora.demorahopliteweapons.weapons.shrinkray;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class ShrinkRayListener implements Listener {
    private final DemoraHopliteWeapons plugin;
    private final ShrinkRay shrinkRay;
    
    public ShrinkRayListener(DemoraHopliteWeapons plugin, ShrinkRay shrinkRay) {
        this.plugin = plugin;
        this.shrinkRay = shrinkRay;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !shrinkRay.isShrinkRay(item)) return;
        
        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            shrinkRay.shrinkPlayer(player);
        }
    }
    
    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();
        
        if (bow == null || !shrinkRay.isShrinkRay(bow)) return;
        
        event.setCancelled(true);
        shrinkRay.shootRay(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        shrinkRay.removePlayerData(event.getPlayer());
    }
}

