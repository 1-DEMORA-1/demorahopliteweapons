package dev.demora.demorahopliteweapons.weapons.villagerstaff;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class VillagerStaffListener implements Listener {
    
    private final VillagerStaff villagerStaff;
    
    public VillagerStaffListener(VillagerStaff villagerStaff) {
        this.villagerStaff = villagerStaff;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        boolean isRightClick = (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK);
        if (!isRightClick) {
            return;
        }
        
        if (!villagerStaff.isVillagerStaff(item)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (villagerStaff.isOnCooldown(player)) {
            long remaining = villagerStaff.getRemainingCooldown(player);
            player.sendMessage("§cПосох перезаряжается! Осталось: §e" + remaining + "§c сек.");
            return;
        }
        
        villagerStaff.useStaff(player);
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getLastDamageCause() == null) return;
        org.bukkit.event.entity.EntityDamageEvent.DamageCause cause = event.getEntity().getLastDamageCause().getCause();
        if (cause != org.bukkit.event.entity.EntityDamageEvent.DamageCause.BLOCK_EXPLOSION && 
            cause != org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return;
        
        if (!villagerStaff.isTaggedForReward(event.getEntity().getUniqueId())) return;
        
        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), 
            new ItemStack(Material.EMERALD_BLOCK, 1));
        villagerStaff.removeRewardTag(event.getEntity().getUniqueId());
    }
}
