package com.autopickup.gui;

import com.autopickup.AutoPickupPlugin;
import com.autopickup.managers.ConverterManager;
import com.autopickup.managers.SmeltingManager;
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
import java.util.Map;

public class AdminConfigGUI {

    private final AutoPickupPlugin plugin;
    public static final String MAIN_GUI_TITLE = "Admin Configuration";
    public static final String CONVERTER_GUI_TITLE = "Ore Converter Config";
    public static final String SMELTING_GUI_TITLE = "Auto Smelt Config";
    public static final String INPUT_AMOUNT_GUI_TITLE = "Set Input Amount";
    public static final String OUTPUT_AMOUNT_GUI_TITLE = "Set Output Amount";

    public AdminConfigGUI(AutoPickupPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text(MAIN_GUI_TITLE, NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true));

        // Fill background
        ItemStack background = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, background);
        }

        // Ore Converter Config (slot 11)
        gui.setItem(11, createConverterConfigItem());

        // Auto Smelt Config (slot 15)
        gui.setItem(15, createSmeltingConfigItem());

        player.openInventory(gui);
    }

    public void openConverterGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, Component.text(CONVERTER_GUI_TITLE, NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true));

        // Fill background
        ItemStack background = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 45; i++) {
            gui.setItem(i, background);
        }

        ConverterManager cm = plugin.getConverterManager();

        // Input slot (slot 19)
        gui.setItem(19, createInputSlotItem(cm.getInputItem()));

        // Arrow indicator (slot 22)
        gui.setItem(22, createArrowItem());

        // Output slot (slot 25)
        gui.setItem(25, createOutputSlotItem(cm.getOutputItem()));

        // Input amount button (slot 28)
        gui.setItem(28, createAmountButton("Input Amount", cm.getInputAmount(), true));

        // Output amount button (slot 34)
        gui.setItem(34, createAmountButton("Output Amount", cm.getOutputAmount(), false));

        // Info (slot 4)
        gui.setItem(4, createConverterInfoItem());

        // Back button (slot 40)
        gui.setItem(40, createBackButton());

        player.openInventory(gui);
    }

    public void openSmeltingGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, Component.text(SMELTING_GUI_TITLE, NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true));

        SmeltingManager sm = plugin.getSmeltingManager();
        Map<Material, Material> recipes = sm.getSmeltingRecipes();

        int slot = 0;
        for (Map.Entry<Material, Material> entry : recipes.entrySet()) {
            if (slot >= 53) break;

            Material input = entry.getKey();
            Material output = entry.getValue();
            boolean enabled = sm.isItemEnabled(input);

            gui.setItem(slot, createSmeltingToggleItem(input, output, enabled));
            slot++;
        }

        // Back button (slot 53)
        gui.setItem(53, createBackButton());

        player.openInventory(gui);
    }

    public void openInputAmountGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text(INPUT_AMOUNT_GUI_TITLE, NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true));

        // Fill background
        ItemStack background = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, background);
        }

        int currentAmount = plugin.getConverterManager().getInputAmount();

        // Decrease buttons
        gui.setItem(10, createAmountAdjustButton(-10, currentAmount));
        gui.setItem(11, createAmountAdjustButton(-1, currentAmount));

        // Current amount display
        gui.setItem(13, createCurrentAmountDisplay(currentAmount, true));

        // Increase buttons
        gui.setItem(15, createAmountAdjustButton(1, currentAmount));
        gui.setItem(16, createAmountAdjustButton(10, currentAmount));

        // Back button
        gui.setItem(22, createBackToConverterButton());

        player.openInventory(gui);
    }

    public void openOutputAmountGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text(OUTPUT_AMOUNT_GUI_TITLE, NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true));

        // Fill background
        ItemStack background = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, background);
        }

        int currentAmount = plugin.getConverterManager().getOutputAmount();

        // Decrease buttons
        gui.setItem(10, createAmountAdjustButton(-10, currentAmount));
        gui.setItem(11, createAmountAdjustButton(-1, currentAmount));

        // Current amount display
        gui.setItem(13, createCurrentAmountDisplay(currentAmount, false));

        // Increase buttons
        gui.setItem(15, createAmountAdjustButton(1, currentAmount));
        gui.setItem(16, createAmountAdjustButton(10, currentAmount));

        // Back button
        gui.setItem(22, createBackToConverterButton());

        player.openInventory(gui);
    }

    private ItemStack createConverterConfigItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Ore Converter Settings", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Configure the ore converter", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("input/output items and amounts.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Click to configure!", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createSmeltingConfigItem() {
        ItemStack item = new ItemStack(Material.FURNACE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Auto Smelt Settings", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Configure which items", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("can be auto-smelted.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Click to configure!", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createInputSlotItem(Material currentInput) {
        ItemStack item = new ItemStack(currentInput);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Input Item", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Current: ", NamedTextColor.GRAY)
                    .append(Component.text(formatMaterialName(currentInput), NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Click with an item to set", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("as the new input item!", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createOutputSlotItem(Material currentOutput) {
        ItemStack item = new ItemStack(currentOutput);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Output Item", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Current: ", NamedTextColor.GRAY)
                    .append(Component.text(formatMaterialName(currentOutput), NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Click with an item to set", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("as the new output item!", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createArrowItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("→ Converts To →", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createAmountButton(String type, int amount, boolean isInput) {
        ItemStack item = new ItemStack(isInput ? Material.RED_DYE : Material.GREEN_DYE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(type + ": " + amount, isInput ? NamedTextColor.RED : NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Click to change amount!", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createConverterInfoItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("How to Configure", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Drag and drop items onto the", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Input/Output slots to set them.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Click the amount buttons to", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("adjust conversion ratios.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Back", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createBackToConverterButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Back to Converter Config", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createSmeltingToggleItem(Material input, Material output, boolean enabled) {
        ItemStack item = new ItemStack(input);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            NamedTextColor statusColor = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;
            String status = enabled ? "ENABLED" : "DISABLED";

            meta.displayName(Component.text(formatMaterialName(input), NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Smelts to: ", NamedTextColor.GRAY)
                    .append(Component.text(formatMaterialName(output), NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Status: ", NamedTextColor.GRAY)
                    .append(Component.text(status, statusColor))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Click to toggle!", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createAmountAdjustButton(int adjustment, int current) {
        Material material;
        NamedTextColor color;
        String prefix;

        if (adjustment > 0) {
            material = Material.LIME_STAINED_GLASS_PANE;
            color = NamedTextColor.GREEN;
            prefix = "+";
        } else {
            material = Material.RED_STAINED_GLASS_PANE;
            color = NamedTextColor.RED;
            prefix = "";
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(prefix + adjustment, color)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Click to adjust amount", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createCurrentAmountDisplay(int amount, boolean isInput) {
        ItemStack item = new ItemStack(isInput ? Material.RED_WOOL : Material.LIME_WOOL);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Current Amount: " + amount, NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

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

    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : name.toCharArray()) {
            if (c == ' ') {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
