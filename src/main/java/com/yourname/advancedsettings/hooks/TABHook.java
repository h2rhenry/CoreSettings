package com.yourname.advancedsettings.hooks;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.entity.Player;

public class TABHook {
    
    public void updateClanTagVisibility(Player player, boolean show) {
        try {
            TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
            if (tabPlayer == null) return;
            
            // Standard approach to remove prefix dynamically if clan tag is disabled.
            // Assumes the clan tag is in the standard prefix or a custom property.
            if (!show) {
                // If they have it off, set a temporary empty clan property 
                // (Requires TAB placeholder %rel_clan% or custom %clan% to check this)
                tabPlayer.setValueTemporarily(me.neznamy.tab.api.PropertyConfigurationType.TABLIST, "custom-clan", "");
            } else {
                tabPlayer.removeTemporaryValue(me.neznamy.tab.api.PropertyConfigurationType.TABLIST, "custom-clan");
            }
        } catch (Exception e) {
            // Graceful fallback for API differences
        }
    }
}
