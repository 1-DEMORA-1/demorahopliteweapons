package dev.demora.demorahopliteweapons.weapons.golemhammer;

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
public class GolemHammer {
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey golemHammerKey;
    public GolemHammer(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.golemHammerKey = new NamespacedKey(plugin, "golem_hammer");
    }
    public ItemStack createGolemHammer() {
        ItemStack hammer = new ItemStack(Material.STONE_AXE);
        ItemMeta meta = hammer.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§7§lМолот голема"));
            meta.setCustomModelData(3);
            meta.setUnbreakable(true);
            double damage = plugin.getConfig().getDouble("weapons.golem_hammer.attribute_damage", 12.0);
            double attackSpeed = plugin.getConfig().getDouble("weapons.golem_hammer.attack_speed", -3.0);
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
            container.set(golemHammerKey, PersistentDataType.BOOLEAN, true);
            hammer.setItemMeta(meta);
        }
        return hammer;
    }
    
    public boolean isGolemHammer(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(golemHammerKey, PersistentDataType.BOOLEAN);
    }
    public NamespacedKey getGolemHammerKey() {
        return golemHammerKey;
    }
}

