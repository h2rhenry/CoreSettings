package com.core.settings.manager;

import com.core.settings.CoreSettings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {

    private final CoreSettings plugin;

    public ConfigManager(CoreSettings plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public String getMessage(String key) {
        String msg = plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key);
        return color(msg);
    }

    public String getGuiTitle() {
        return color(plugin.getConfig().getString("gui.title", "&6Settings"));
    }

    public int getGuiSize() {
        int size = plugin.getConfig().getInt("gui.size", 54);
        return Math.max(9, Math.min(54, size));
    }

    public Material getBorderMaterial() {
        String mat = plugin.getConfig().getString("gui.border-material", "GRAY_STAINED_GLASS_PANE");
        Material material = Material.matchMaterial(mat);
        return material != null ? material : Material.GRAY_STAINED_GLASS_PANE;
    }
    
    public ConfigurationSection getCloseButtonConfig() {
        return plugin.getConfig().getConfigurationSection("gui.close");
    }

    public ConfigurationSection getOptions() {
        return plugin.getConfig().getConfigurationSection("options");
    }

    public boolean getDefaultToggle(String optionId) {
        return plugin.getConfig().getBoolean("options." + optionId + ".default", false);
    }

    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(ConfigManager::color).collect(Collectors.toList());
    }
}
