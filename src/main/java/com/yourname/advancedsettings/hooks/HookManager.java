package com.yourname.advancedsettings.hooks;

import com.yourname.advancedsettings.AdvancedSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public class HookManager {
    private final AdvancedSettings plugin;
    private TABHook tabHook;
    private EssentialsHook essentialsHook;
    private ProtocolLibHook protocolLibHook;

    public HookManager(AdvancedSettings plugin) {
        this.plugin = plugin;
        setupHooks();
    }

    private void setupHooks() {
        if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
            essentialsHook = new EssentialsHook();
            plugin.getLogger().info("Hooked into EssentialsX!");
        }
        if (Bukkit.getPluginManager().getPlugin("TAB") != null) {
            tabHook = new TABHook();
            plugin.getLogger().info("Hooked into TAB!");
        }
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            protocolLibHook = new ProtocolLibHook(plugin);
            plugin.getLogger().info("Hooked into ProtocolLib!");
        }
        // BetterTeams/ClanPlus hooks would follow the same pattern
    }

    public EssentialsHook getEssentialsHook() { return essentialsHook; }
    
    /**
     * Applies settings dynamically via hooks (e.g., hiding players, updating TAB).
     */
    public void applySettings(Player player, Map<String, Boolean> settings) {
        if (player == null) return;

        // Apply Tab Clan Tag visibility
        if (tabHook != null && settings.containsKey("tab-show-clan-tag")) {
            tabHook.updateClanTagVisibility(player, settings.get("tab-show-clan-tag"));
        }
        
        // ProtocolLib Packet Handling (Hide Players)
        if (protocolLibHook != null) {
            protocolLibHook.refreshVisibility(player);
        }
    }
}
