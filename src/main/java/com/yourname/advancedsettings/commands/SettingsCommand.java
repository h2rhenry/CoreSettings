package com.yourname.advancedsettings.commands;

import com.yourname.advancedsettings.AdvancedSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettingsCommand implements CommandExecutor {
    private final AdvancedSettings plugin;

    public SettingsCommand(AdvancedSettings plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("advancedsettings.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }
            plugin.getConfigManager().reloadConfigs();
            sender.sendMessage(plugin.getConfigManager().getMessage("reload-success"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("admin")) {
            if (!sender.hasPermission("advancedsettings.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }
            // Future Admin GUI feature expansion here
            sender.sendMessage("§cAdmin GUI coming soon. Use /settings reload for now.");
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            plugin.getGuiManager().openSettingsGUI(player);
        } else {
            sender.sendMessage("This command is for players only.");
        }
        return true;
    }
}
