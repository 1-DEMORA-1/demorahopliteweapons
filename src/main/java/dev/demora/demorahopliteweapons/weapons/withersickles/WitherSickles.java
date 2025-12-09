package dev.demora.demorahopliteweapons.weapons.withersickles;

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
public class WitherSickles {
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey witherSickleKey;
    private final NamespacedKey additionalSickleKey;
    public WitherSickles(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.witherSickleKey = new NamespacedKey(plugin, "wither_sickle");
        this.additionalSickleKey = new NamespacedKey(plugin, "additional_sickle");
    }
    
    public ItemStack createWitherSickle() {
        ItemStack sickle = new ItemStack(Material.STONE_HOE);
        ItemMeta meta = sickle.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§c§lСерпы визера"));
            meta.setCustomModelData(3);
            meta.setUnbreakable(true);
            double damage = plugin.getConfig().getDouble("weapons.wither_sickles.damage", 11.0);
            double attackSpeed = plugin.getConfig().getDouble("weapons.wither_sickles.attack_speed", -1.6);
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
            meta.getPersistentDataContainer().set(witherSickleKey, PersistentDataType.BOOLEAN, true);
            sickle.setItemMeta(meta);
        }
        return sickle;
    }
    
    public ItemStack createAdditionalSickle() {
        ItemStack sickle = new ItemStack(Material.STONE_HOE);
        ItemMeta meta = sickle.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§c§lДополнительный серп"));
            meta.setCustomModelData(3);
            meta.setUnbreakable(true);
            meta.getPersistentDataContainer().set(additionalSickleKey, PersistentDataType.BOOLEAN, true);
            sickle.setItemMeta(meta);
        }
        return sickle;
    }
    
    public boolean isWitherSickle(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(witherSickleKey, PersistentDataType.BOOLEAN);
    }
    
    public boolean isAdditionalSickle(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(additionalSickleKey, PersistentDataType.BOOLEAN);
    }
}

