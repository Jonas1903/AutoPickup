package com.autopickup.commands;

import com.autopickup.AutoPickupPlugin;
import com.autopickup.gui.AdminConfigGUI;
import com.autopickup.gui.PlayerToggleGUI;
import com.autopickup.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AutoPickupCommand implements CommandExecutor, TabCompleter {

    private final AutoPickupPlugin plugin;
    private final PlayerToggleGUI playerToggleGUI;
    private final AdminConfigGUI adminConfigGUI;

    public AutoPickupCommand(AutoPickupPlugin plugin) {
        this.plugin = plugin;
        this.playerToggleGUI = new PlayerToggleGUI(plugin);
        this.adminConfigGUI = new AdminConfigGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        if (args.length == 0) {
            // Open player toggle GUI
            if (!player.hasPermission("autopickup.use")) {
                player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "no-permission"));
                return true;
            }
            playerToggleGUI.openGUI(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "admin" -> {
                if (!player.hasPermission("autopickup.admin")) {
                    player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "no-permission"));
                    return true;
                }
                adminConfigGUI.openMainGUI(player);
            }
            case "give" -> {
                if (!player.hasPermission("autopickup.admin")) {
                    player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "no-permission"));
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "usage"));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "player-not-found"));
                    return true;
                }

                ItemStack converterItem = plugin.getOreConverterItem().createItem();
                target.getInventory().addItem(converterItem);

                target.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "converter-received"));
                if (!target.equals(player)) {
                    player.sendMessage(ConfigUtils.getColoredMessageWithPlaceholder(
                            plugin.getConfig(), "converter-given", "%player%", target.getName()));
                }
            }
            case "reload" -> {
                if (!player.hasPermission("autopickup.admin")) {
                    player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "no-permission"));
                    return true;
                }

                plugin.reloadPlugin();
                player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "config-reloaded"));
            }
            default -> player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "usage"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            if (sender.hasPermission("autopickup.admin")) {
                subCommands.addAll(Arrays.asList("admin", "give", "reload"));
            }
            completions = subCommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            if (sender.hasPermission("autopickup.admin")) {
                completions = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }

    public AdminConfigGUI getAdminConfigGUI() {
        return adminConfigGUI;
    }

    public PlayerToggleGUI getPlayerToggleGUI() {
        return playerToggleGUI;
    }
}
