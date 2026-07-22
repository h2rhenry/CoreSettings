package com.yourname.advancedsettings.managers;

import com.yourname.advancedsettings.AdvancedSettings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final AdvancedSettings plugin;
    private final File dataFolder;
    private final Map<UUID, Map<String, Boolean>> playerCache = new HashMap<>();

    public PlayerDataManager(AdvancedSettings plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) dataFolder.mkdirs();
    }

    public void loadPlayer(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        Map<String, Boolean> settings = new HashMap<>();

        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            if (config.contains("settings")) {
                for (String key : config.getConfigurationSection("settings").getKeys(false)) {
                    settings.put(key, config.getBoolean("settings." + key));
                }
            }
        } else {
            // Load defaults
            FileConfiguration pluginConfig = plugin.getConfig();
            if (pluginConfig.contains("settings")) {
                for (String key : pluginConfig.getConfigurationSection("settings").getKeys(false)) {
                    settings.put(key, pluginConfig.getBoolean("settings." + key + ".default", true));
                }
            }
        }
        playerCache.put(uuid, settings);
        
        // Notify Hooks (e.g. TAB, ProtocolLib visibility update)
        plugin.getHookManager().applySettings(Bukkit.getPlayer(uuid), settings);
    }

    public void savePlayerAsync(UUID uuid) {
        if (!playerCache.containsKey(uuid)) return;
        Map<String, Boolean> data = new HashMap<>(playerCache.get(uuid));

        new BukkitRunnable() {
            @Override
            public void run() {
                File file = new File(dataFolder, uuid.toString() + ".yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                config.set("uuid", uuid.toString());
                for (Map.Entry<String, Boolean> entry : data.entrySet()) {
                    config.set("settings." + entry.getKey(), entry.getValue());
                }
                try {
                    config.save(file);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not save data for " + uuid);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void saveAll() {
        for (UUID uuid : playerCache.keySet()) {
            savePlayerAsync(uuid);
        }
    }

    public void unloadPlayer(UUID uuid) {
        savePlayerAsync(uuid);
        playerCache.remove(uuid);
    }

    public boolean getSetting(UUID uuid, String setting) {
        if (!playerCache.containsKey(uuid)) return plugin.getConfig().getBoolean("settings." + setting + ".default", true);
        return playerCache.get(uuid).getOrDefault(setting, plugin.getConfig().getBoolean("settings." + setting + ".default", true));
    }

    public void toggleSetting(UUID uuid, String setting) {
        if (playerCache.containsKey(uuid)) {
            boolean current = getSetting(uuid, setting);
            playerCache.get(uuid).put(setting, !current);
            savePlayerAsync(uuid);
            plugin.getHookManager().applySettings(Bukkit.getPlayer(uuid), playerCache.get(uuid));
        }
    }
}
