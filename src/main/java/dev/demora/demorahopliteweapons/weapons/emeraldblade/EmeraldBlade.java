package dev.demora.demorahopliteweapons.weapons.emeraldblade;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.UUID;
public class EmeraldBlade {
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey emeraldBladeKey;
    private final NamespacedKey upgradeCountKey;
    private final NamespacedKey emeraldCountKey;
    public EmeraldBlade(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.emeraldBladeKey = new NamespacedKey(plugin, "emerald_blade");
        this.upgradeCountKey = new NamespacedKey(plugin, "emerald_blade_upgrades");
        this.emeraldCountKey = new NamespacedKey(plugin, "emerald_blade_emeralds");
    }
    public ItemStack createEmeraldBlade() {
        return createEmeraldBladeWithEmeralds(0);
    }
    
    public ItemStack createEmeraldBladeWithEmeralds(int emeraldCount) {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§a§lИзумрудный клинок"));
            meta.setCustomModelData(3);
            meta.setUnbreakable(true);
            double baseDamage = plugin.getConfig().getDouble("weapons.emerald_blade.base_damage", 7.0);
            double damagePerUpgrade = plugin.getConfig().getDouble("weapons.emerald_blade.damage_per_upgrade", 2.0);
            int emeraldsPerUpgrade = plugin.getConfig().getInt("weapons.emerald_blade.emeralds_per_upgrade", 64);
            int upgradeCount = emeraldsPerUpgrade > 0 ? emeraldCount / emeraldsPerUpgrade : 0;
            double totalDamage = baseDamage + (upgradeCount * damagePerUpgrade);
            AttributeModifier damageModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_damage",
                totalDamage,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
            double attackSpeed = plugin.getConfig().getDouble("weapons.emerald_blade.attack_speed", -2.4);
            AttributeModifier attackSpeedModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_speed",
                attackSpeed,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, attackSpeedModifier);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(emeraldBladeKey, PersistentDataType.BOOLEAN, true);
            container.set(upgradeCountKey, PersistentDataType.INTEGER, upgradeCount);
            container.set(emeraldCountKey, PersistentDataType.INTEGER, emeraldCount);
            sword.setItemMeta(meta);
        }
        return sword;
    }
    
    public boolean isEmeraldBlade(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(emeraldBladeKey, PersistentDataType.BOOLEAN);
    }
    public int getUpgradeCount(ItemStack item) {
        if (!isEmeraldBlade(item)) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(upgradeCountKey, PersistentDataType.INTEGER, 0);
    }
    public int getEmeraldCount(ItemStack item) {
        if (!isEmeraldBlade(item)) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(emeraldCountKey, PersistentDataType.INTEGER, 0);
    }
    public NamespacedKey getEmeraldBladeKey() {
        return emeraldBladeKey;
    }
    public NamespacedKey getUpgradeCountKey() {
        return upgradeCountKey;
    }
}

