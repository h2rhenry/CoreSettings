package com.core.settings.gui;

import com.core.settings.CoreSettings;
import com.core.settings.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

public class CoreSettingsGUI {

    public static void open(Player player, CoreSettings plugin) {
        ConfigManager cm = plugin.getConfigManager();
        int size = cm.getGuiSize();
        String title = cm.getGuiTitle();

        // Uses CoreSettingsHolder to safely identify the custom GUI
        Inventory inv = Bukkit.createInventory(new CoreSettingsHolder(), size, title);

        // Apply Border
        ItemStack borderItem = new ItemStack(cm.getBorderMaterial());
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            borderItem.setItemMeta(borderMeta);
        }

        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, borderItem);
            }
        }

        // Apply Close Button
        ConfigurationSection closeSec = cm.getCloseButtonConfig();
        if (closeSec != null) {
            int closeSlot = closeSec.getInt("slot", size - 5);
            Material closeMat = Material.matchMaterial(closeSec.getString("material", "BARRIER"));
            if (closeMat == null) closeMat = Material.BARRIER;
            
            ItemStack closeItem = createItem(closeMat, closeSec.getString("name", "&cClose"), closeSec.getStringList("lore"));
            
            // Tag it so we know it's the close button
            ItemMeta meta = closeItem.getItemMeta();
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "settings_action"), PersistentDataType.STRING, "CLOSE");
            closeItem.setItemMeta(meta);
            
            inv.setItem(closeSlot, closeItem);
        }

        // Apply Options
        ConfigurationSection options = cm.getOptions();
        if (options != null) {
            for (String key : options.getKeys(false)) {
                ConfigurationSection opt = options.getConfigurationSection(key);
                if (opt == null) continue;

                int slot = opt.getInt("slot");
                Material mat = Material.matchMaterial(opt.getString("material", "STONE"));
                if (mat == null) mat = Material.STONE;

                boolean currentState = plugin.getDataManager().getToggle(player.getUniqueId(), key);
                String stateKey = currentState ? "enabled" : "disabled";
                
                ConfigurationSection stateSec = opt.getConfigurationSection(stateKey);
                if (stateSec == null) continue;

                ItemStack item = createItem(mat, stateSec.getString("name"), stateSec.getStringList("lore"));
                
                // Tag item with option ID for the click listener
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "settings_option"), PersistentDataType.STRING, key);
                item.setItemMeta(meta);

                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material mat, String name, java.util.List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(ConfigManager.color(name));
            if (lore != null) meta.setLore(ConfigManager.color(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
