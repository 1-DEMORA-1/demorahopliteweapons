package dev.demora.demorahopliteweapons.weapons.hypnosisstaff;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
public class HypnosisStaff {
    private final NamespacedKey hypnosisStaffKey;
    public HypnosisStaff(org.bukkit.plugin.Plugin plugin) {
        this.hypnosisStaffKey = new NamespacedKey(plugin, "hypnosis_staff");
    }
    public ItemStack createHypnosisStaff() {
        ItemStack staff = new ItemStack(Material.STONE_SWORD);
        ItemMeta meta = staff.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§6§lПосох гипноза"));
            meta.setCustomModelData(6);
            meta.setUnbreakable(true);
            meta.getPersistentDataContainer().set(hypnosisStaffKey, PersistentDataType.BOOLEAN, true);
            staff.setItemMeta(meta);
        }
        return staff;
    }  
    public boolean isHypnosisStaff(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(hypnosisStaffKey, PersistentDataType.BOOLEAN);
    }
}
