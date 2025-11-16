package dev.demora.demorahopliteweapons.weapons.chainsawsword;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ChainsawSword {
    
    private final NamespacedKey chainsawSwordKey;
    private final NamespacedKey curseKey;
    
    public ChainsawSword(DemoraHopliteWeapons plugin) {
        this.chainsawSwordKey = new NamespacedKey(plugin, "chainsaw_sword");
        this.curseKey = new NamespacedKey(plugin, "chainsaw_curse");
    }
    public ItemStack createChainsawSword() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§c§lМеч-пила"));
            meta.setCustomModelData(8);
            meta.addEnchant(Enchantment.SHARPNESS, 5, true);
            meta.setUnbreakable(true);
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

