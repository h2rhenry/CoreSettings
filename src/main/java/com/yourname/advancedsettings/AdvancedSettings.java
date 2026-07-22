package com.yourname.advancedsettings;

import com.yourname.advancedsettings.commands.SettingsCommand;
import com.yourname.advancedsettings.hooks.HookManager;
import com.yourname.advancedsettings.listeners.GUIListener;
import com.yourname.advancedsettings.listeners.PlayerListener;
import com.yourname.advancedsettings.managers.ConfigManager;
import com.yourname.advancedsettings.managers.GUIManager;
import com.yourname.advancedsettings.managers.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvancedSettings extends JavaPlugin {

    private static AdvancedSettings instance;
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private GUIManager guiManager;
    private HookManager hookManager;

    @Override
    public void onEnable() {
        instance = this;

        // Managers
        this.configManager = new ConfigManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.hookManager = new HookManager(this);
        this.guiManager = new GUIManager(this);

        // Listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);

        // Commands
        getCommand("settings").setExecutor(new SettingsCommand(this));

        // Load online players (in case of reload)
        Bukkit.getOnlinePlayers().forEach(p -> playerDataManager.loadPlayer(p.getUniqueId()));

        getLogger().info("AdvancedSettings enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        getLogger().info("AdvancedSettings disabled!");
    }

    public static AdvancedSettings getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public HookManager getHookManager() { return hookManager; }
}
