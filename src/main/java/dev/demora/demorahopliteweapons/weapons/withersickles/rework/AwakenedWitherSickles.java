package dev.demora.demorahopliteweapons.weapons.withersickles.rework;

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
public class AwakenedWitherSickles {
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey awakenedSickleKey;
    private final NamespacedKey additionalAwakenedSickleKey;
    public AwakenedWitherSickles(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.awakenedSickleKey = new NamespacedKey(plugin, "awakened_wither_sickle");
        this.additionalAwakenedSickleKey = new NamespacedKey(plugin, "additional_awakened_sickle");
    }
    
    public ItemStack createAwakenedWitherSickle() {
        ItemStack sickle = new ItemStack(Material.STONE_HOE);
        ItemMeta meta = sickle.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§6§lПробуждённые серпы визера"));
            meta.setCustomModelData(6);
            meta.setUnbreakable(true);
            double damage = plugin.getConfig().getDouble("weapons.awakened_wither_sickles.damage", 13.0);
            double attackSpeed = plugin.getConfig().getDouble("weapons.awakened_wither_sickles.attack_speed", -1.6);
            AttributeModifier damageModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_damage",
                damage,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
            AttributeModifier speedModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_speed",
                attackSpeed,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, speedModifier);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(awakenedSickleKey, PersistentDataType.BOOLEAN, true);
            sickle.setItemMeta(meta);
        }
        return sickle;
    }
    
    public ItemStack createAdditionalAwakenedSickle() {
        ItemStack sickle = new ItemStack(Material.STONE_HOE);
        ItemMeta meta = sickle.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§6§lДополнительный пробуждённый серп"));
            meta.setCustomModelData(6);
            meta.setUnbreakable(true);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(additionalAwakenedSickleKey, PersistentDataType.BOOLEAN, true);
            sickle.setItemMeta(meta);
        }
        return sickle;
    }
    
    public boolean isAwakenedWitherSickle(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(awakenedSickleKey, PersistentDataType.BOOLEAN);
    }
    
    public boolean isAdditionalAwakenedSickle(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(additionalAwakenedSickleKey, PersistentDataType.BOOLEAN);
    }
    
    public NamespacedKey getAwakenedSickleKey() {
        return awakenedSickleKey;
    }
    public NamespacedKey getAdditionalAwakenedSickleKey() {
        return additionalAwakenedSickleKey;
    }
}

