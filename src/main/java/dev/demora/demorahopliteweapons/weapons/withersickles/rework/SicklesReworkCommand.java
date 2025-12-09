package dev.demora.demorahopliteweapons.weapons.withersickles.rework;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
public class SicklesReworkCommand implements CommandExecutor {
    private final DemoraHopliteWeapons plugin;
    private FileConfiguration reworkConfig;
    public SicklesReworkCommand(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        loadReworkConfig();
    }
    private void loadReworkConfig() {
        File reworkFile = new File(plugin.getDataFolder(), "rework.yml");
        if (!reworkFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                InputStream defaultConfig = plugin.getResource("rework.yml");
                if (defaultConfig != null) {
                    Files.copy(defaultConfig, reworkFile.toPath());
                    defaultConfig.close();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать rework.yml: " + e.getMessage());
            }
        }
        
        reworkConfig = YamlConfiguration.loadConfiguration(reworkFile);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cТолько игроки могут использовать эту команду!");
            return true;
        }
        Player player = (Player) sender;
        if (!reworkConfig.getBoolean("awakened_wither_sickles.enabled", false)) {
            player.sendMessage("§cПробуждение серпов визера отключено!");
            return true;
        }
        
        if (!hasRequiredItems(player)) {
            player.sendMessage("§cУ вас нет необходимых предметов для пробуждения!");
            player.sendMessage("§eТребуется: §f1x Незеритовый слиток, 1x Череп скелета-иссушителя, 64x Огненный порошок, 1x Серпы визера");
            return true;
        }
        
        removeRequiredItems(player);
        AwakenedWitherSickles awakenedSickles = new AwakenedWitherSickles(plugin);
        ItemStack sickle = awakenedSickles.createAwakenedWitherSickle();
        player.getInventory().addItem(sickle);
        player.sendMessage("§6§lПробуждённые серпы визера созданы!");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 1.2f);
        Location loc = player.getLocation();
        for (int i = 0; i < 30; i++) {
            loc.getWorld().spawnParticle(Particle.FLAME, 
                loc.clone().add(Math.random() * 2 - 1, Math.random() * 2, Math.random() * 2 - 1), 
                5, 0.1, 0.1, 0.1, 0.02);
            loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, 
                loc.clone().add(Math.random() * 2 - 1, Math.random() * 2, Math.random() * 2 - 1), 
                3, 0.1, 0.1, 0.1, 0.01);
        }
        return true;
    }
    
    private boolean hasRequiredItems(Player player) {
        int netheriteIngot = 0;
        int witherSkeletonSkull = 0;
        int blazePowder = 0;
        boolean hasWitherSickle = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType() == Material.NETHERITE_INGOT) {
                netheriteIngot += item.getAmount();
            } else if (item.getType() == Material.WITHER_SKELETON_SKULL) {
                witherSkeletonSkull += item.getAmount();
            } else if (item.getType() == Material.BLAZE_POWDER) {
                blazePowder += item.getAmount();
            } else if (isWitherSickle(item)) {
                hasWitherSickle = true;
            }
        }
        
        return netheriteIngot >= 1 && witherSkeletonSkull >= 1 && blazePowder >= 64 && hasWitherSickle;
    }
    private void removeRequiredItems(Player player) {
        int netheriteToRemove = 1;
        int skullToRemove = 1;
        int blazeToRemove = 64;
        boolean sickleRemoved = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType() == Material.NETHERITE_INGOT && netheriteToRemove > 0) {
                int amount = Math.min(item.getAmount(), netheriteToRemove);
                item.setAmount(item.getAmount() - amount);
                netheriteToRemove -= amount;
                if (item.getAmount() == 0) {
                    player.getInventory().remove(item);
                }
            } else if (item.getType() == Material.WITHER_SKELETON_SKULL && skullToRemove > 0) {
                int amount = Math.min(item.getAmount(), skullToRemove);
                item.setAmount(item.getAmount() - amount);
                skullToRemove -= amount;
                if (item.getAmount() == 0) {
                    player.getInventory().remove(item);
                }
            } else if (item.getType() == Material.BLAZE_POWDER && blazeToRemove > 0) {
                int amount = Math.min(item.getAmount(), blazeToRemove);
                item.setAmount(item.getAmount() - amount);
                blazeToRemove -= amount;
                if (item.getAmount() == 0) {
                    player.getInventory().remove(item);
                }
            } else if (isWitherSickle(item) && !sickleRemoved) {
                player.getInventory().remove(item);
                sickleRemoved = true;
            }
        }
    }
    
    private boolean isWitherSickle(ItemStack item) {
        if (item == null || item.getType() != Material.STONE_HOE) {
            return false;
        }
        return plugin.getWitherSickles().isWitherSickle(item);
    }
}

