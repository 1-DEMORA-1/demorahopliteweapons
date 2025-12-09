package dev.demora.demorahopliteweapons.weapons.cloudsword;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.UUID;
public class CloudSword {
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey cloudSwordKey;
    public CloudSword(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.cloudSwordKey = new NamespacedKey(plugin, "cloud_sword");
    }
    
    public ItemStack createCloudSword() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§f§lОблачный меч"));
            meta.setCustomModelData(7);
            meta.setUnbreakable(true);
            
            double damage = plugin.getConfig().getDouble("weapons.cloud_sword.attribute_damage", 11.0);
            AttributeModifier damageModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_damage",
                damage,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
            double attackSpeed = plugin.getConfig().getDouble("weapons.cloud_sword.attack_speed", -2.4);
            AttributeModifier attackSpeedModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_speed",
                attackSpeed,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, attackSpeedModifier);
            double speedBoost = plugin.getConfig().getDouble("weapons.cloud_sword.speed_boost", 0.15);
            AttributeModifier speedModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.movement_speed",
                speedBoost,
                AttributeModifier.Operation.ADD_SCALAR,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, speedModifier);
            
            meta.getPersistentDataContainer().set(cloudSwordKey, PersistentDataType.BOOLEAN, true);
            sword.setItemMeta(meta);
        }
        return sword;
    }
    public boolean isCloudSword(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(cloudSwordKey, PersistentDataType.BOOLEAN);
    }
}

