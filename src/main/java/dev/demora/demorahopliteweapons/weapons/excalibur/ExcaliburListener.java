package dev.demora.demorahopliteweapons.weapons.excalibur;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class ExcaliburListener implements Listener {
    
    private final Excalibur excalibur;
    
    public ExcaliburListener(Excalibur excalibur) {
        this.excalibur = excalibur;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!excalibur.isExcalibur(item)) {
            return;
        }
        
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        event.setCancelled(true);
        
        if (excalibur.isOnCooldown(player)) {
            long remaining = excalibur.getRemainingCooldown(player);
            player.sendMessage("§cЭкскалибур перезаряжается! Осталось: §e" + remaining + " §cсек");
            return;
        }
        
        excalibur.activateProtection(player);
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        
        if (excalibur.hasActiveProtection(victim)) {
            event.setCancelled(true);
            
            if (event.getDamager() instanceof Player) {
                Player attacker = (Player) event.getDamager();
                attacker.playSound(attacker.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 4.0f);
            }
            
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 4.0f);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        excalibur.removePlayerData(event.getPlayer().getUniqueId());
    }
}
