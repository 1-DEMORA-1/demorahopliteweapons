package dev.demora.demorahopliteweapons.weapons.chainsawsword;

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

public class ChainsawSword {
    
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey chainsawSwordKey;
    private final NamespacedKey curseKey;
    
    public ChainsawSword(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.chainsawSwordKey = new NamespacedKey(plugin, "chainsaw_sword");
        this.curseKey = new NamespacedKey(plugin, "chainsaw_curse");
    }
    public ItemStack createChainsawSword() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§c§lМеч-пила"));
            meta.setCustomModelData(8);
            meta.setUnbreakable(true);
            
            double damage = plugin.getConfig().getDouble("weapons.chainsaw_sword.attribute_damage", 11.0);
            AttributeModifier damageModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_damage",
                damage,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier);
            
            double attackSpeed = plugin.getConfig().getDouble("weapons.chainsaw_sword.attack_speed", -2.4);
            AttributeModifier attackSpeedModifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_speed",
                attackSpeed,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.HAND
            );
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, attackSpeedModifier);
            
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(chainsawSwordKey, PersistentDataType.BOOLEAN, true);
            container.remove(curseKey);
            sword.setItemMeta(meta);
        }
        return sword;
    }
    public boolean isChainsawSword(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(chainsawSwordKey, PersistentDataType.BOOLEAN);
    }
    
    public boolean hasCurse(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(curseKey, PersistentDataType.BOOLEAN, false);
    }
    
    public void setCurse(ItemStack item, boolean value) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (value) {
            container.set(curseKey, PersistentDataType.BOOLEAN, true);
        } else {
            container.remove(curseKey);
        }
        item.setItemMeta(meta);
    }
}

