package com.autopickup.gui;

import com.autopickup.AutoPickupPlugin;
import com.autopickup.managers.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerToggleGUI {

    private final AutoPickupPlugin plugin;
    private static final String GUI_TITLE = "AutoPickup Settings";

    public PlayerToggleGUI(AutoPickupPlugin plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text(GUI_TITLE, NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.BOLD, true));

        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        // Fill background with gray glass
        ItemStack background = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, background);
        }

        // Auto Pickup Toggle (slot 11)
        gui.setItem(11, createAutoPickupItem(data.isAutoPickupEnabled()));

        // Auto Smelt Toggle (slot 15)
        gui.setItem(15, createAutoSmeltItem(data.isAutoSmeltEnabled()));

        // Info item (slot 22)
        gui.setItem(22, createInfoItem());

        player.openInventory(gui);
    }

    private ItemStack createAutoPickupItem(boolean enabled) {
        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? "ENABLED" : "DISABLED";
        NamedTextColor statusColor = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Auto Pickup", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                    .append(Component.text(status, statusColor))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("When enabled, items from", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("broken blocks go directly", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("into your inventory.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Click to toggle!", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createAutoSmeltItem(boolean enabled) {
        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? "ENABLED" : "DISABLED";
        NamedTextColor statusColor = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Auto Smelt", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                    .append(Component.text(status, statusColor))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("When enabled, ores are", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("automatically smelted when", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("mined (e.g. Iron Ore → Iron Ingot).", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Click to toggle!", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Information", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("AutoPickup lets you collect", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("items directly into your", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("inventory when mining!", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Auto Smelt will convert", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("ores to ingots automatically.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createItem(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (name != null && !name.isEmpty()) {
                meta.displayName(Component.text(name, NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false));
            } else {
                meta.displayName(Component.text(" "));
            }

            if (loreLines != null && !loreLines.isEmpty()) {
                List<Component> lore = new ArrayList<>();
                for (String line : loreLines) {
                    lore.add(Component.text(line, NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(lore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public static String getGuiTitle() {
        return GUI_TITLE;
    }
}
