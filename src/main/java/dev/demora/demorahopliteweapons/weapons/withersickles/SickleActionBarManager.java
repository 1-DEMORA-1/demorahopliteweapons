package dev.demora.demorahopliteweapons.weapons.withersickles;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
public class SickleActionBarManager {
    private final DemoraHopliteWeapons plugin;
    private final WitherSickles witherSickles;
    private final SickleCooldownManager cooldownManager;
    private final SickleInventoryManager inventoryManager;
    public SickleActionBarManager(DemoraHopliteWeapons plugin, WitherSickles witherSickles,
                                 SickleCooldownManager cooldownManager, SickleInventoryManager inventoryManager) {
        this.plugin = plugin;
        this.witherSickles = witherSickles;
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
                    
                    if (witherSickles.isWitherSickle(mainHand)) {
                        if (!witherSickles.isAdditionalSickle(offHand) && tickCounter % 4 == 0 && !cooldownManager.isOnCooldown(player)) {
                            inventoryManager.equipOffhandSickle(player);
                        }
                        if (witherSickles.isAdditionalSickle(offHand)) {
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

