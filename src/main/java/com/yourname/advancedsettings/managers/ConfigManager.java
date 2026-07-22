package com.yourname.advancedsettings.managers;

import com.yourname.advancedsettings.AdvancedSettings;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {
    private final AdvancedSettings plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public ConfigManager(AdvancedSettings plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        loadMessages();
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        loadMessages();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path) {
        String prefix = messagesConfig.getString("prefix", "");
        String msg = messagesConfig.getString(path, "&cMessage not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }
}
