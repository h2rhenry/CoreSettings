package com.core.settings.listener;

import com.core.settings.CoreSettings;
import com.core.settings.gui.CoreSettingsGUI;
import com.core.settings.gui.CoreSettingsHolder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CoreSettingsListener implements Listener {

    private final CoreSettings plugin;
    private final NamespacedKey optionKey;
    private final NamespacedKey actionKey;

    public CoreSettingsListener(CoreSettings plugin) {
        this.plugin = plugin;
        this.optionKey = new NamespacedKey(plugin, "settings_option");
        this.actionKey = new NamespacedKey(plugin, "settings_action");
    }

    // Handle GUI Clicks
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CoreSettingsHolder)) return;
        event.setCancelled(true); // Always cancel to prevent stealing items

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getItemMeta() == null) return;

        ItemMeta meta = clickedItem.getItemMeta();

        // Handle Close Button
        if (meta.getPersistentDataContainer().has(actionKey, PersistentDataType.STRING)) {
            String action = meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
            if ("CLOSE".equals(action)) {
                player.closeInventory();
                return;
            }
        }

        // Handle Toggle Options
        if (meta.getPersistentDataContainer().has(optionKey, PersistentDataType.STRING)) {
            String optionId = meta.getPersistentDataContainer().get(optionKey, PersistentDataType.STRING);
            
            // Check fly permission specifically for fly toggle
            if (optionId.equals("toggle_fly") && !player.hasPermission("coresettings.fly")) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            boolean currentState = plugin.getDataManager().getToggle(player.getUniqueId(), optionId);
            boolean newState = !currentState;

            // Save new state
            plugin.getDataManager().setToggle(player.getUniqueId(), optionId, newState);
            
            // Send feedback
            player.sendMessage(plugin.getConfigManager().getMessage(newState ? "toggled-on" : "toggled-off"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, newState ? 2.0f : 1.0f);

            // Apply immediate effects
            applyImmediateEffect(player, optionId, newState);

            // Redraw GUI to show new item
            CoreSettingsGUI.open(player, plugin);
        }
    }

    // Apply gameplay logic instantly when toggled
    private void applyImmediateEffect(Player player, String optionId, boolean state) {
        switch (optionId) {
            case "toggle_fly":
                player.setAllowFlight(state);
                if (!state) player.setFlying(false);
                break;
            case "toggle_scoreboard":
                if (!state) {
                    player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()); // Blank
                } else {
                    player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                }
                break;
            case "toggle_tablist":
            case "toggle_player_visibility":
                updateVisibilityFor(player);
                break;
        }
    }

    private void updateVisibilityFor(Player player) {
        boolean showMeInTab = plugin.getDataManager().getToggle(player.getUniqueId(), "toggle_tablist");
        boolean hideOthers = plugin.getDataManager().getToggle(player.getUniqueId(), "toggle_player_visibility");

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;

            // Handle my visibility to others
            if (!showMeInTab) {
                other.hidePlayer(plugin, player);
            } else {
                other.showPlayer(plugin, player);
            }

            // Handle others visibility to me
            if (hideOthers) {
                player.hidePlayer(plugin, other);
            } else {
                player.showPlayer(plugin, other);
            }
        }
    }

    // ------------------------------------------------------------------
    // Event Implementations for Options
    // ------------------------------------------------------------------

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getDataManager().getToggle(event.getPlayer().getUniqueId(), "toggle_public_chat")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getConfigManager().getMessage("toggled-off"));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Hide join message if disabled
        if (!plugin.getDataManager().getToggle(player.getUniqueId(), "toggle_join_leave")) {
            event.setJoinMessage(null);
        }

        // Apply visual/passive settings
        if (player.hasPermission("coresettings.fly")) {
            boolean fly = plugin.getDataManager().getToggle(player.getUniqueId(), "toggle_fly");
            player.setAllowFlight(fly);
            if (!fly) player.setFlying(false);
        }

        if (!plugin.getDataManager().getToggle(player.getUniqueId(), "toggle_scoreboard")) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        updateVisibilityFor(player);
        
        // Update all other players regarding this newly joined player
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            boolean otherHideOthers = plugin.getDataManager().getToggle(other.getUniqueId(), "toggle_player_visibility");
            if (otherHideOthers) {
                other.hidePlayer(plugin, player);
            }
            boolean otherShowTab = plugin.getDataManager().getToggle(other.getUniqueId(), "toggle_tablist");
            if (!otherShowTab) {
                player.hidePlayer(plugin, other);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getDataManager().getToggle(player.getUniqueId(), "toggle_join_leave")) {
            event.setQuitMessage(null);
        }
        
        // Clear from cache to avoid memory leaks
        plugin.getDataManager().clearCache(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            Player player = (Player) event.getEntity();
            // Fall Damage config: True means fall damage is prevented
            if (plugin.getDataManager().getToggle(player.getUniqueId(), "toggle_fall_damage")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPvP(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            // If the victim has PvP disabled
            if (!plugin.getDataManager().getToggle(victim.getUniqueId(), "toggle_pvp")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        // Basic interception for vanilla/essentials messaging commands
        if (message.startsWith("/msg ") || message.startsWith("/tell ") || message.startsWith("/w ")) {
            String[] args = message.split(" ");
            if (args.length > 1) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    if (!plugin.getDataManager().getToggle(target.getUniqueId(), "toggle_private_msg")) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(plugin.getConfigManager().getMessage("private-messages-disabled"));
                    }
                }
            }
        }
    }
}
