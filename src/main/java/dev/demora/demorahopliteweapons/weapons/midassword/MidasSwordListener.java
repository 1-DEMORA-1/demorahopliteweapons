package dev.demora.demorahopliteweapons.weapons.midassword;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class MidasSwordListener implements Listener {
    
    private final MidasSword midasSword;
    
    public MidasSwordListener(MidasSword midasSword) {
        this.midasSword = midasSword;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer == null) {
            return;
        }
        
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        
        if (!midasSword.isMidasSword(weapon)) {
            return;
        }
        
        midasSword.increaseDamage(weapon);
        
        midasSword.createKillEffects(victim, killer);
        
        for (int i = 0; i < 5; i++) {
            victim.getWorld().dropItemNaturally(victim.getLocation(), new ItemStack(Material.GOLD_BLOCK));
        }
        
        killer.sendMessage(ChatColor.GOLD + "§lМеч Мидаса усилился! Урон: " + 
            midasSword.getDamageLevel(weapon));
    }
}
