package dev.demora.demorahopliteweapons.weapons.mjolnir;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class MjolnirListener implements Listener {
    
    private final Mjolnir mjolnir;
    private final DemoraHopliteWeapons plugin;
    
    public MjolnirListener(Mjolnir mjolnir, DemoraHopliteWeapons plugin) {
        this.mjolnir = mjolnir;
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !mjolnir.isMjolnir(item)) {
            return;
        }
        
        boolean isRightClick = (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK);
        if (!isRightClick) {
            return;
        }
        
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        event.setCancelled(true);
        
        if (mjolnir.isOnThrowCooldown(player)) {
            long remaining = mjolnir.getRemainingThrowCooldown(player);
            player.sendMessage("§cМьёльнир перезаряжается! Осталось: §e" + remaining + " сек");
            return;
        }
        
        mjolnir.setThrowCooldown(player);
        mjolnir.throwMjolnir(player);
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        ItemStack zov = attacker.getInventory().getItemInMainHand();
        
        if (!mjolnir.isMjolnir(zov)) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player target = (Player) event.getEntity();
        
        if (mjolnir.isOnMeleeCooldown(attacker)) {
            long remaining = mjolnir.getRemainingMeleeCooldown(attacker);
            attacker.sendMessage("§cМьёльнир перезаряжается! Осталось: §e" + remaining + " сек");
            event.setCancelled(true);
            return;
        }
        
        if (mjolnir.isProcessingDamage(target.getUniqueId())) {
            return;
        }
        mjolnir.setMeleeCooldown(attacker);
        
        mjolnir.addProcessingDamage(target.getUniqueId());
        
        try {
            double meleeDamage = plugin.getConfig().getDouble("weapons.mjolnir.damage.melee_damage", 20.0);
            event.setDamage(meleeDamage);
            
            mjolnir.performMeleeAttack(target, attacker);
        } finally {
            mjolnir.removeProcessingDamage(target.getUniqueId());
        }
    }
}
