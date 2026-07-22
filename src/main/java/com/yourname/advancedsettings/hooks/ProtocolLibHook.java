package com.yourname.advancedsettings.hooks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.yourname.advancedsettings.AdvancedSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ProtocolLibHook {
    private final AdvancedSettings plugin;
    private final ProtocolManager protocolManager;

    public ProtocolLibHook(AdvancedSettings plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        registerListeners();
    }

    private void registerListeners() {
        // Prevent sending spawn packets if the receiver has "hide-players" enabled
        protocolManager.addPacketListener(new PacketAdapter(plugin, 
            PacketType.Play.Server.NAMED_ENTITY_SPAWN,
            PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player receiver = event.getPlayer();
                boolean hidePlayers = plugin.getPlayerDataManager().getSetting(receiver.getUniqueId(), "hide-players");
                
                if (hidePlayers) {
                    // For thorough packet-level blocking (in addition to Bukkit.hidePlayer)
                    if (event.getPacketType() == PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
                        event.setCancelled(true);
                    }
                }
            }
        });
    }

    public void refreshVisibility(Player player) {
        boolean hideOthers = plugin.getPlayerDataManager().getSetting(player.getUniqueId(), "hide-players");
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.equals(player)) continue;
                if (hideOthers) {
                    player.hidePlayer(plugin, target);
                } else {
                    player.showPlayer(plugin, target);
                }
            }
        });
    }
}
