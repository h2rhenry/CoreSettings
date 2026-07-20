package com.core.settings.api;

import com.core.settings.CoreSettings;
import org.bukkit.entity.Player;
import java.util.UUID;

public class CoreSettingsAPI {

    /**
     * Checks if a specific setting is enabled for a player.
     * 
     * @param player   The player to check.
     * @param optionId The ID of the option from config.yml (e.g. "toggle_tpa", "toggle_party").
     * @return true if enabled, false if disabled. Will fall back to the config default if the player has no saved data.
     */
    public static boolean isToggled(Player player, String optionId) {
        if (player == null || optionId == null) return false;
        return isToggled(player.getUniqueId(), optionId);
    }
    
    /**
     * Checks if a specific setting is enabled for a player's UUID.
     * 
     * @param uuid     The UUID of the player.
     * @param optionId The ID of the option from config.yml.
     * @return true if enabled, false if disabled.
     */
    public static boolean isToggled(UUID uuid, String optionId) {
        if (CoreSettings.getInstance() == null) return false;
        return CoreSettings.getInstance().getDataManager().getToggle(uuid, optionId);
    }
}
