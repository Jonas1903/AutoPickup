package com.autopickup.gui;

import com.autopickup.AutoPickupPlugin;
import com.autopickup.listeners.InventoryClickListener;
import com.autopickup.managers.ConversionRecipe;
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
    public static final String CONVERTER_GUI_TITLE = "Converter Recipes";
    public static final String EDIT_RECIPE_GUI_TITLE = "Edit Recipe";
    public static final String SMELTING_GUI_TITLE = "Auto Smelt Config";

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

    /**
     * Opens the multi-recipe converter GUI (54 slots)
     * Layout:
     * [Info] [   ] [   ] [   ] [   ] [   ] [   ] [   ] [   ]
     * [   ] [IN1] [ → ] [OUT1] [   ] [IN2] [ → ] [OUT2] [   ]
     * [   ] [IN3] [ → ] [OUT3] [   ] [IN4] [ → ] [OUT4] [   ]
     * [   ] [IN5] [ → ] [OUT5] [   ] [IN6] [ → ] [OUT6] [   ]
     * [   ] [IN7] [ → ] [OUT7] [   ] [IN8] [ → ] [OUT8] [   ]
     * [ADD] [   ] [   ] [   ] [BACK] [   ] [   ] [   ] [   ]
     */
    public void openConverterGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, Component.text(CONVERTER_GUI_TITLE, NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true));

        // Fill background
        ItemStack background = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, background);
        }

        ConverterManager cm = plugin.getConverterManager();
        List<ConversionRecipe> recipes = cm.getRecipes();

        // Info item (slot 4)
        gui.setItem(4, createConverterInfoItem());

        // Recipe slot positions (left column: 0,2,4,6 and right column: 1,3,5,7)
        // Row 1: slots 10,11,12 (recipe 0) and 14,15,16 (recipe 1)
        // Row 2: slots 19,20,21 (recipe 2) and 23,24,25 (recipe 3)
        // Row 3: slots 28,29,30 (recipe 4) and 32,33,34 (recipe 5)
        // Row 4: slots 37,38,39 (recipe 6) and 41,42,43 (recipe 7)
        
        int[][] recipeSlots = {
            {10, 11, 12}, // Recipe 0
            {14, 15, 16}, // Recipe 1
            {19, 20, 21}, // Recipe 2
            {23, 24, 25}, // Recipe 3
            {28, 29, 30}, // Recipe 4
            {32, 33, 34}, // Recipe 5
            {37, 38, 39}, // Recipe 6
            {41, 42, 43}  // Recipe 7
        };

        for (int i = 0; i < recipes.size() && i < recipeSlots.length; i++) {
            ConversionRecipe recipe = recipes.get(i);
            int[] slots = recipeSlots[i];
            
            // Input item
            gui.setItem(slots[0], createRecipeInputItem(recipe, i));
            // Arrow
            gui.setItem(slots[1], createArrowItem());
            // Output item
            gui.setItem(slots[2], createRecipeOutputItem(recipe, i));
        }

        // Add New Recipe button (slot 45)
        if (cm.getRecipeCount() < ConverterManager.MAX_RECIPES) {
            gui.setItem(45, createAddRecipeButton());
        } else {
            gui.setItem(45, createMaxRecipesItem());
        }

        // Back button (slot 49)
        gui.setItem(49, createBackButton());

        player.openInventory(gui);
    }

    /**
     * Opens the Add/Edit Recipe GUI (27 slots)
     * Layout:
     * [   ] [   ] [   ] [   ] [INFO] [   ] [   ] [   ] [   ]
     * [   ] [INPUT] [-1] [+1] [   ] [OUTPUT] [-1] [+1] [   ]
     * [   ] [DEL] [   ] [   ] [SAVE] [   ] [   ] [BACK] [   ]
     */
    public void openEditRecipeGUI(Player player, InventoryClickListener.RecipeEditData editData) {
        String title = editData.isNewRecipe() ? "Add New Recipe" : "Edit Recipe";
        Inventory gui = Bukkit.createInventory(null, 27, Component.text(EDIT_RECIPE_GUI_TITLE, NamedTextColor.AQUA)
                .decoration(TextDecoration.BOLD, true));

        // Fill background
        ItemStack background = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, background);
        }

        // Info (slot 4)
        gui.setItem(4, createEditRecipeInfoItem(editData.isNewRecipe()));

        // Input item slot (slot 10)
        gui.setItem(10, createEditInputSlotItem(editData.getInputItem(), editData.getInputAmount()));

        // Input amount -1 button (slot 11)
        gui.setItem(11, createAmountAdjustButton(-1, editData.getInputAmount()));

        // Input amount +1 button (slot 12)
        gui.setItem(12, createAmountAdjustButton(1, editData.getInputAmount()));

        // Output item slot (slot 14)
        gui.setItem(14, createEditOutputSlotItem(editData.getOutputItem(), editData.getOutputAmount()));

        // Output amount -1 button (slot 15)
        gui.setItem(15, createAmountAdjustButton(-1, editData.getOutputAmount()));

        // Output amount +1 button (slot 16)
        gui.setItem(16, createAmountAdjustButton(1, editData.getOutputAmount()));

        // Delete button (slot 19) - only shown when editing existing recipe
        if (!editData.isNewRecipe()) {
            gui.setItem(19, createDeleteButton());
        }

        // Save button (slot 22)
        gui.setItem(22, createSaveButton());

        // Back/Cancel button (slot 25)
        gui.setItem(25, createCancelButton());

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

    // ===== Item creation methods =====

    private ItemStack createConverterConfigItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Ore Converter Settings", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Configure conversion recipes.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Add, edit, or delete recipes.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            
            int recipeCount = plugin.getConverterManager().getRecipeCount();
            lore.add(Component.text("Active Recipes: ", NamedTextColor.GRAY)
                    .append(Component.text(recipeCount, NamedTextColor.GREEN))
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

    private ItemStack createRecipeInputItem(ConversionRecipe recipe, int index) {
        ItemStack item = new ItemStack(recipe.getInputItem());
        item.setAmount(Math.min(recipe.getInputAmount(), 64));
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Recipe #" + (index + 1) + " - Input", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Item: ", NamedTextColor.GRAY)
                    .append(Component.text(formatMaterialName(recipe.getInputItem()), NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Amount: ", NamedTextColor.GRAY)
                    .append(Component.text(recipe.getInputAmount(), NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Click to edit this recipe!", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createRecipeOutputItem(ConversionRecipe recipe, int index) {
        ItemStack item = new ItemStack(recipe.getOutputItem());
        item.setAmount(Math.min(recipe.getOutputAmount(), 64));
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Recipe #" + (index + 1) + " - Output", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Item: ", NamedTextColor.GRAY)
                    .append(Component.text(formatMaterialName(recipe.getOutputItem()), NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Amount: ", NamedTextColor.GRAY)
                    .append(Component.text(recipe.getOutputAmount(), NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Click to edit this recipe!", NamedTextColor.YELLOW)
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
            meta.displayName(Component.text("→", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createAddRecipeButton() {
        ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Add New Recipe", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Click to add a new", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("conversion recipe.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createMaxRecipesItem() {
        ItemStack item = new ItemStack(Material.GRAY_CONCRETE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Max Recipes Reached", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("You have reached the maximum", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("of " + ConverterManager.MAX_RECIPES + " recipes.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Delete a recipe to add more.", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createEditInputSlotItem(Material currentInput, int amount) {
        ItemStack item = new ItemStack(currentInput);
        item.setAmount(Math.min(amount, 64));
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
            lore.add(Component.text("Amount: ", NamedTextColor.GRAY)
                    .append(Component.text(amount, NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Click with an item to change!", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createEditOutputSlotItem(Material currentOutput, int amount) {
        ItemStack item = new ItemStack(currentOutput);
        item.setAmount(Math.min(amount, 64));
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
            lore.add(Component.text("Amount: ", NamedTextColor.GRAY)
                    .append(Component.text(amount, NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Click with an item to change!", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createEditRecipeInfoItem(boolean isNew) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String title = isNew ? "Add New Recipe" : "Edit Recipe";
            meta.displayName(Component.text(title, NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Click on input/output slots", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("with an item to set it.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Use +/- buttons to adjust", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("the conversion amounts.", NamedTextColor.GRAY)
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
            meta.displayName(Component.text("Converter Recipes", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Click on any recipe to edit it.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Click Add to create new recipes.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Max recipes: ", NamedTextColor.GRAY)
                    .append(Component.text(ConverterManager.MAX_RECIPES, NamedTextColor.GREEN))
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

    private ItemStack createSaveButton() {
        ItemStack item = new ItemStack(Material.LIME_WOOL);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Save Recipe", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Click to save this recipe.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createDeleteButton() {
        ItemStack item = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Delete Recipe", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Click to delete this recipe.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("This cannot be undone!", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createCancelButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("Cancel", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Go back without saving.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
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
