package dev.demora.demorahopliteweapons.commands;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
public class DemoraReworkMenuCommand implements CommandExecutor {
    private final DemoraHopliteWeapons plugin;
    public DemoraReworkMenuCommand(DemoraHopliteWeapons plugin) {
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
        openReworkMenu(player);
        return true;
    }
    
    public void openReworkMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 9, "§c§lПробуждённые оружия");
        menu.setItem(0, createMenuItem(plugin.getAwakenedDragonKatana().createAwakenedDragonKatana(), "awakened_dragon_katana"));
        menu.setItem(1, createMenuItem(plugin.getAwakenedWitherSickles().createAwakenedWitherSickle(), "awakened_wither_sickles"));
        player.openInventory(menu);
    }
    
    private ItemStack createMenuItem(ItemStack weapon, String weaponId) {
        ItemStack item = weapon.clone();
        return item;
    }
    public boolean isReworkMenu(Inventory inventory) {
        return inventory.getSize() == 9 && 
               inventory.getViewers().size() > 0 &&
               "§c§lПробуждённые оружия".equals(inventory.getViewers().get(0).getOpenInventory().getTitle());
    }
}

