package com.yourname.advancedsettings.listeners;

import com.yourname.advancedsettings.AdvancedSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Iterator;

public class PlayerListener implements Listener {
    private final AdvancedSettings plugin;

    public PlayerListener(AdvancedSettings plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPlayerDataManager().loadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().unloadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        // Cancel chat entirely if the sender has chat disabled
        if (!plugin.getPlayerDataManager().getSetting(event.getPlayer().getUniqueId(), "chat-visibility")) {
            event.setCancelled(true);
            return;
        }

        // Remove recipients who have chat disabled
        Iterator<Player> iterator = event.getRecipients().iterator();
        while (iterator.hasNext()) {
            Player recipient = iterator.next();
            if (!plugin.getPlayerDataManager().getSetting(recipient.getUniqueId(), "chat-visibility")) {
                iterator.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPvP(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            boolean victimPvP = plugin.getPlayerDataManager().getSetting(victim.getUniqueId(), "pvp");
            boolean attackerPvP = plugin.getPlayerDataManager().getSetting(attacker.getUniqueId(), "pvp");

            if (!attackerPvP) {
                event.setCancelled(true);
                attacker.sendMessage(plugin.getConfigManager().getMessage("pvp-disabled"));
            } else if (!victimPvP) {
                event.setCancelled(true);
                attacker.sendMessage(plugin.getConfigManager().getMessage("pvp-target-disabled"));
            }
        }
    }
}
