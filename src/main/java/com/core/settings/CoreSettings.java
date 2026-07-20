package com.core.settings;

import com.core.settings.command.CoreSettingsCommand;
import com.core.settings.listener.CoreSettingsListener;
import com.core.settings.manager.ConfigManager;
import com.core.settings.manager.DataManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CoreSettings extends JavaPlugin {
    
    private static CoreSettings instance;
    private ConfigManager configManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize Managers
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);

        // Register Command
        getCommand("settings").setExecutor(new CoreSettingsCommand(this));

        // Register Listeners
        getServer().getPluginManager().registerEvents(new CoreSettingsListener(this), this);

        getLogger().info("CoreSettings has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Ensure data is saved fully before shutdown
        if (dataManager != null) {
            dataManager.saveSynchronously();
        }
        getLogger().info("CoreSettings has been disabled.");
    }

    public static CoreSettings getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
