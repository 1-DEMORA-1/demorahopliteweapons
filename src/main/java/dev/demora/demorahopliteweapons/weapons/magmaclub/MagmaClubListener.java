package dev.demora.demorahopliteweapons.weapons.magmaclub;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class MagmaClubListener implements Listener {
    private final DemoraHopliteWeapons plugin;
    private final MagmaClub magmaClub;
    
    public MagmaClubListener(DemoraHopliteWeapons plugin, MagmaClub magmaClub) {
        this.plugin = plugin;
        this.magmaClub = magmaClub;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !magmaClub.isMagmaClub(item)) return;
        
        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            magmaClub.fireBlast(player);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        magmaClub.removePlayerData(event.getPlayer());
    }
}
