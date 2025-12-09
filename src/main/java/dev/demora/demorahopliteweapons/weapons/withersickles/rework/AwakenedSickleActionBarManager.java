package dev.demora.demorahopliteweapons.weapons.withersickles.rework;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
public class AwakenedSickleActionBarManager {
    private final DemoraHopliteWeapons plugin;
    private final AwakenedWitherSickles awakenedSickles;
    private final AwakenedSickleInventoryManager inventoryManager;
    private final AwakenedSickleCooldownManager cooldownManager; 
    public AwakenedSickleActionBarManager(DemoraHopliteWeapons plugin, AwakenedWitherSickles awakenedSickles, AwakenedSickleCooldownManager cooldownManager, AwakenedSickleInventoryManager inventoryManager) {
        this.plugin = plugin;
        this.awakenedSickles = awakenedSickles;
        this.cooldownManager = cooldownManager;
        this.inventoryManager = inventoryManager;
    }
    
    public void startActionBarTask() {
        new BukkitRunnable() {
            private int tickCounter = 0;
            @Override
            public void run() {
                tickCounter++;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    ItemStack offHand = player.getInventory().getItemInOffHand();
                    if (awakenedSickles.isAwakenedWitherSickle(mainHand)) {
                        if (!awakenedSickles.isAdditionalAwakenedSickle(offHand) && tickCounter % 4 == 0 && !cooldownManager.isOnCooldown(player)) {
                            inventoryManager.equipOffhandSickle(player);
                        }
                        if (awakenedSickles.isAdditionalAwakenedSickle(offHand)) {
                            player.sendActionBar(Component.text("\uE020"));
                        } else {
                            player.sendActionBar(Component.text("\uE021"));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}

