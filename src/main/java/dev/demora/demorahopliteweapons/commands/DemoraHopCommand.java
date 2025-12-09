package dev.demora.demorahopliteweapons.commands;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class DemoraHopCommand implements CommandExecutor, TabCompleter {
    
    private final DemoraHopliteWeapons plugin;
    private final List<String> weaponNames = Arrays.asList(
        "sculk_crossbow", "midas_sword", "mjolnir", "shadow_blade", 
        "reaper_scythe", "villager_staff", "dragon_katana", "excalibur", "void_staff", "magma_club", "shrink_ray", "chainsaw_sword", "wither_sickles", "awakened_wither_sickles", "cloud_sword", "hypnosis_staff", "emerald_blade", "golem_hammer"
    );
    
    public DemoraHopCommand(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("demorahopliteweapons.give")) {
            sender.sendMessage(plugin.getMessageWithPrefix("no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage("§cИспользование: /demorahop give <weapon> <player>");
            sender.sendMessage("§eДоступные оружия: " + String.join(", ", weaponNames));
            return true;
        }
        
        if (args[0].equalsIgnoreCase("give")) {
            return handleGiveCommand(sender, args);
        } else if (args[0].equalsIgnoreCase("reload")) {
            return handleReloadCommand(sender);
        }
        
        sender.sendMessage("§cНеизвестная команда! Используйте: give, reload");
        return true;
    }
    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /demorahop give <weapon> <player>");
            return true;
        }
        
        String weaponName = args[1].toLowerCase();
        String playerName = args[2];
        String popa = weaponName;
        
        if (!weaponNames.contains(popa)) {
            sender.sendMessage(plugin.getMessageWithPrefix("weapon-not-found")
                .replace("{weapon}", popa));
            return true;
        }
        
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(plugin.getMessageWithPrefix("player-not-found")
                .replace("{player}", playerName));
            return true;
        }
        
        ItemStack weapon = createWeapon(popa);
        if (weapon != null) {
            target.getInventory().addItem(weapon);
            sender.sendMessage(plugin.getMessageWithPrefix("weapon-given")
                .replace("{weapon}", popa)
                .replace("{player}", target.getName()));
        }
        
        return true;
    }
    
    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("demorahopliteweapons.reload")) {
            sender.sendMessage(plugin.getMessageWithPrefix("no-permission"));
            return true;
        }
        
        plugin.reloadPluginConfig();
        sender.sendMessage(plugin.getMessageWithPrefix("config-reloaded"));
        return true;
    }
    
    private ItemStack createWeapon(String weaponName) {
        return switch (weaponName) {
            case "sculk_crossbow" -> plugin.getSculkCrossbow().createSculkCrossbow();
            case "midas_sword" -> plugin.getMidasSword().createMidasSword();
            case "mjolnir" -> plugin.getMjolnir().createMjolnir();
            case "shadow_blade" -> plugin.getShadowBlade().createShadowBlade();
            case "reaper_scythe" -> plugin.getReaperScythe().createReaperScythe();
            case "villager_staff" -> plugin.getVillagerStaff().createVillagerStaff();
            case "dragon_katana" -> plugin.getDragonKatana().createDragonKatana();
            case "excalibur" -> plugin.getExcalibur().createExcalibur();
            case "void_staff" -> plugin.getVoidStaff().createVoidStaff();
            case "magma_club" -> plugin.getMagmaClub().createMagmaClub();
            case "shrink_ray" -> plugin.getShrinkRay().createShrinkRay();
            case "chainsaw_sword" -> plugin.getChainsawSword().createChainsawSword();
            case "wither_sickles" -> plugin.getWitherSickles().createWitherSickle();
            case "awakened_wither_sickles" -> plugin.getAwakenedWitherSickles().createAwakenedWitherSickle();
            case "cloud_sword" -> plugin.getCloudSword().createCloudSword();
            case "hypnosis_staff" -> plugin.getHypnosisStaff().createHypnosisStaff();
            case "emerald_blade" -> plugin.getEmeraldBlade().createEmeraldBlade();
            case "golem_hammer" -> plugin.getGolemHammer().createGolemHammer();
            default -> null;
        };
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("give");
            if (sender.hasPermission("demorahopliteweapons.reload")) {
                completions.add("reload");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(weaponNames);
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }
        
        return completions.stream()
            .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .toList();
    }
}
