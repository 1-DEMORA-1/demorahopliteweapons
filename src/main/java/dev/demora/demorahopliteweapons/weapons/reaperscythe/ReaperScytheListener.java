package dev.demora.demorahopliteweapons.weapons.reaperscythe;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ReaperScytheListener implements Listener {
    
    private final ReaperScythe reaperScythe;
    
    public ReaperScytheListener(ReaperScythe reaperScythe) {
        this.reaperScythe = reaperScythe;
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        Player target = (Player) event.getEntity();
        
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (!reaperScythe.isReaperScythe(weapon)) {
            return;
        }
        
        if (reaperScythe.isOnCooldown(attacker)) {
            long remaining = reaperScythe.getRemainingCooldown(attacker);
            attacker.sendMessage("§cКоса жнеца перезаряжается! Осталось: §e" + remaining + " сек");
            return;
        }
        
        reaperScythe.performReaperAttack(attacker, target);
    }
}
