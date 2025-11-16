package dev.demora.demorahopliteweapons.weapons.shadowblade;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ShadowBladeListener implements Listener {
    
    private final ShadowBlade shadowBlade;
    
    public ShadowBladeListener(ShadowBlade shadowBlade) {
        this.shadowBlade = shadowBlade;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        if (item == null || !shadowBlade.isShadowBlade(item)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (shadowBlade.isOnCooldown(player)) {
            int remainingSeconds = shadowBlade.getRemainingCooldown(player);
            player.sendMessage("§cТеневой клинок перезаряжается! Осталось: §e" + remainingSeconds + " сек");
            return;
        }
        
        if (shadowBlade.isInShadowMode(player)) {
            return;
        }
        
        shadowBlade.activateShadowBlade(player);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        shadowBlade.hidePlayerFromNewJoiner(event.getPlayer());
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            
            if (shadowBlade.isInShadowMode(victim)) {
                shadowBlade.returnFromShadow(victim);
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        shadowBlade.removePlayerData(event.getPlayer());
    }
}
