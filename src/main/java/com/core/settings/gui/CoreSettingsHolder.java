package com.core.settings.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

// Custom holder to easily identify our custom GUI without relying on titles
public class CoreSettingsHolder implements InventoryHolder {
    @Override
    public Inventory getInventory() {
        return null;
    }
}
