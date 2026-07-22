package com.yourname.advancedsettings.listeners;

import com.yourname.advancedsettings.AdvancedSettings;
import com.yourname.advancedsettings.hooks.EssentialsHook;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {
    private final AdvancedSettings plugin;

    public GUIListener(AdvancedSettings plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.title", "Settings"));
        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getClickedInventory() == null) return;

        Player player = (Player) event.getWhoClicked();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("settings");
        
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            if (section.getInt(key + ".slot") == event.getSlot()) {
                
                double cost = section.getDouble(key + ".cost", 0.0);
                EssentialsHook essHook = plugin.getHookManager().getEssentialsHook();
                
                if (essHook != null && cost > 0) {
                    if (!essHook.chargePlayer(player, cost)) {
                        String msg = plugin.getConfigManager().getMessage("insufficient-funds").replace("{cost}", String.valueOf(cost));
                        player.sendMessage(msg);
                        player.closeInventory();
                        return;
                    }
                }

                // Toggle setting
                plugin.getPlayerDataManager().toggleSetting(player.getUniqueId(), key);
                boolean newState = plugin.getPlayerDataManager().getSetting(player.getUniqueId(), key);
                
                String display = ChatColor.translateAlternateColorCodes('&', section.getString(key + ".display-name"));
                String msgKey = newState ? "toggle-on" : "toggle-off";
                player.sendMessage(plugin.getConfigManager().getMessage(msgKey).replace("{setting}", display));

                // Refresh GUI
                plugin.getGuiManager().openSettingsGUI(player);
                break;
            }
        }
    }
}
