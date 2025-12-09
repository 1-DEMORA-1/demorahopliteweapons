package dev.demora.demorahopliteweapons.commands;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
public class DemoraMenuCommand implements CommandExecutor {
    private final DemoraHopliteWeapons plugin;
    public DemoraMenuCommand(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
    } 
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage(plugin.getMessageWithPrefix("no-permission"));
            return true;
        }
        
        openWeaponsMenu(player);
        return true;
    }
    public void openWeaponsMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 18, "§c§lЛегендарные оружия");
        menu.setItem(0, createMenuItem(plugin.getSculkCrossbow().createSculkCrossbow(), "sculk_crossbow"));
        menu.setItem(1, createMenuItem(plugin.getMidasSword().createMidasSword(), "midas_sword"));
        menu.setItem(2, createMenuItem(plugin.getMjolnir().createMjolnir(), "mjolnir"));
        menu.setItem(3, createMenuItem(plugin.getShadowBlade().createShadowBlade(), "shadow_blade"));
        menu.setItem(4, createMenuItem(plugin.getReaperScythe().createReaperScythe(), "reaper_scythe"));
        menu.setItem(5, createMenuItem(plugin.getVillagerStaff().createVillagerStaff(), "villager_staff"));
        menu.setItem(6, createMenuItem(plugin.getDragonKatana().createDragonKatana(), "dragon_katana"));
        menu.setItem(7, createMenuItem(plugin.getExcalibur().createExcalibur(), "excalibur"));
        menu.setItem(8, createMenuItem(plugin.getVoidStaff().createVoidStaff(), "void_staff"));
        menu.setItem(9, createMenuItem(plugin.getMagmaClub().createMagmaClub(), "magma_club"));
        menu.setItem(10, createMenuItem(plugin.getShrinkRay().createShrinkRay(), "shrink_ray"));
        menu.setItem(11, createMenuItem(plugin.getChainsawSword().createChainsawSword(), "chainsaw_sword"));
        menu.setItem(12, createMenuItem(plugin.getWitherSickles().createWitherSickle(), "wither_sickles"));
        menu.setItem(13, createMenuItem(plugin.getCloudSword().createCloudSword(), "cloud_sword"));
        menu.setItem(14, createMenuItem(plugin.getHypnosisStaff().createHypnosisStaff(), "hypnosis_staff"));
        menu.setItem(15, createMenuItem(plugin.getEmeraldBlade().createEmeraldBlade(), "emerald_blade"));
        menu.setItem(16, createMenuItem(plugin.getGolemHammer().createGolemHammer(), "golem_hammer"));
        player.openInventory(menu);
    }
    private ItemStack createMenuItem(ItemStack weapon, String weaponId) {
        ItemStack item = weapon.clone();
        return item;
    }
    
    public boolean isWeaponsMenu(Inventory inventory) {
        return inventory.getSize() == 18 && 
               inventory.getViewers().size() > 0 &&
               "§c§lЛегендарные оружия".equals(inventory.getViewers().get(0).getOpenInventory().getTitle());
    }
}

