package com.core.settings.manager;

import com.core.settings.CoreSettings;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DataManager {

    private final CoreSettings plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;
    
    // In-memory cache to prevent constant disk reads
    private final Map<UUID, Map<String, Boolean>> playerCache = new HashMap<>();

    public DataManager(CoreSettings plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create playerdata.yml", e);
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public boolean getToggle(UUID uuid, String optionId) {
        // Load into cache if not present
        if (!playerCache.containsKey(uuid)) {
            playerCache.put(uuid, new HashMap<>());
            if (dataConfig.contains(uuid.toString())) {
                for (String key : dataConfig.getConfigurationSection(uuid.toString()).getKeys(false)) {
                    playerCache.get(uuid).put(key, dataConfig.getBoolean(uuid.toString() + "." + key));
                }
            }
        }

        // Return from cache if exists, otherwise fallback to config.yml default
        Map<String, Boolean> toggles = playerCache.get(uuid);
        if (toggles.containsKey(optionId)) {
            return toggles.get(optionId);
        }
        return plugin.getConfigManager().getDefaultToggle(optionId);
    }

    public void setToggle(UUID uuid, String optionId, boolean state) {
        playerCache.computeIfAbsent(uuid, k -> new HashMap<>()).put(optionId, state);
        
        // Update YamlConfig synchronously (it is thread safe when done on main thread)
        dataConfig.set(uuid.toString() + "." + optionId, state);
        
        // Save to disk asynchronously to prevent lag spikes
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveToFile);
    }

    private void saveToFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save playerdata.yml", e);
        }
    }

    public void saveSynchronously() {
        saveToFile();
    }
    
    public void clearCache(UUID uuid) {
        playerCache.remove(uuid);
    }
}
