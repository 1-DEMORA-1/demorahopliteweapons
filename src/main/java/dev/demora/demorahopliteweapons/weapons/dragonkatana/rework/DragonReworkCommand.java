package dev.demora.demorahopliteweapons.weapons.dragonkatana.rework;

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

public class DragonReworkCommand implements CommandExecutor {
    
    private final DemoraHopliteWeapons plugin;
    private FileConfiguration reworkConfig;
    
    public DragonReworkCommand(DemoraHopliteWeapons plugin) {
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
        
        if (!reworkConfig.getBoolean("enabled", false)) {
            player.sendMessage("§cПробуждение драконьей катаны отключено!");
            return true;
        }
        
        if (!hasRequiredItems(player)) {
            player.sendMessage("§cУ вас нет необходимых предметов для пробуждения!");
            player.sendMessage("§eТребуется: §f1x Яйцо дракона, 64x Плод хоруса, 16x Эндер жемчуг, 1x Драконья катана");
            return true;
        }
        
        removeRequiredItems(player);
        
        AwakenedDragonKatana awakenedKatana = new AwakenedDragonKatana(plugin);
        ItemStack katana = awakenedKatana.createAwakenedDragonKatana();
        
        player.getInventory().addItem(katana);
        player.sendMessage("§5§lПробуждённая Драконья катана создана!");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
        
        Location loc = player.getLocation();
        for (int i = 0; i < 20; i++) {
            loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, 
                loc.clone().add(Math.random() * 2 - 1, Math.random() * 2, Math.random() * 2 - 1), 
                5, 0.1, 0.1, 0.1, 0.02);
        }
        
        return true;
    }
    
    private boolean hasRequiredItems(Player player) {
        int dragonEgg = 0;
        int chorusFruit = 0;
        int enderPearl = 0;
        boolean hasDragonKatana = false;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            
            if (item.getType() == Material.DRAGON_EGG) {
                dragonEgg += item.getAmount();
            } else if (item.getType() == Material.CHORUS_FRUIT) {
                chorusFruit += item.getAmount();
            } else if (item.getType() == Material.ENDER_PEARL) {
                enderPearl += item.getAmount();
            } else if (isDragonKatana(item)) {
                hasDragonKatana = true;
            }
        }
        
        return dragonEgg >= 1 && chorusFruit >= 64 && enderPearl >= 16 && hasDragonKatana;
    }
    
    private void removeRequiredItems(Player player) {
        int dragonEggToRemove = 1;
        int chorusFruitToRemove = 64;
        int enderPearlToRemove = 16;
        boolean dragonKatanaRemoved = false;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            
            if (item.getType() == Material.DRAGON_EGG && dragonEggToRemove > 0) {
                int amount = Math.min(item.getAmount(), dragonEggToRemove);
                item.setAmount(item.getAmount() - amount);
                dragonEggToRemove -= amount;
                if (item.getAmount() == 0) {
                    player.getInventory().remove(item);
                }
            } else if (item.getType() == Material.CHORUS_FRUIT && chorusFruitToRemove > 0) {
                int amount = Math.min(item.getAmount(), chorusFruitToRemove);
                item.setAmount(item.getAmount() - amount);
                chorusFruitToRemove -= amount;
                if (item.getAmount() == 0) {
                    player.getInventory().remove(item);
                }
            } else if (item.getType() == Material.ENDER_PEARL && enderPearlToRemove > 0) {
                int amount = Math.min(item.getAmount(), enderPearlToRemove);
                item.setAmount(item.getAmount() - amount);
                enderPearlToRemove -= amount;
                if (item.getAmount() == 0) {
                    player.getInventory().remove(item);
                }
            } else if (isDragonKatana(item) && !dragonKatanaRemoved) {
                player.getInventory().remove(item);
                dragonKatanaRemoved = true;
            }
        }
    }
    
    private boolean isDragonKatana(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD) {
            return false;
        }
        
        return plugin.getDragonKatana().isDragonKatana(item);
    }
}
