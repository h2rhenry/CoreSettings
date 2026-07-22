package com.yourname.advancedsettings.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.math.BigDecimal;

public class EssentialsHook {
    private final Essentials ess;

    public EssentialsHook() {
        this.ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
    }

    public boolean chargePlayer(Player player, double amount) {
        if (amount <= 0) return true;
        try {
            if (Economy.hasEnough(player.getUniqueId(), new BigDecimal(amount))) {
                Economy.subtract(player.getUniqueId(), new BigDecimal(amount));
                return true;
            }
        } catch (UserDoesNotExistException | NoLoanPermittedException e) {
            return false;
        }
        return false;
    }
}
