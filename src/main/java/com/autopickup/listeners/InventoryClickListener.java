package com.autopickup.listeners;

import com.autopickup.AutoPickupPlugin;
import com.autopickup.gui.AdminConfigGUI;
import com.autopickup.gui.PlayerToggleGUI;
import com.autopickup.managers.ConversionRecipe;
import com.autopickup.managers.ConverterManager;
import com.autopickup.managers.SmeltingManager;
import com.autopickup.utils.ConfigUtils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    private final AutoPickupPlugin plugin;
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    
    // Store editing recipe data per player
    private final Map<UUID, RecipeEditData> editingRecipes = new HashMap<>();

    public InventoryClickListener(AutoPickupPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Data class to track recipe editing state per player
     */
    public static class RecipeEditData {
        private int editingIndex = -1; // -1 means new recipe
        private Material inputItem = Material.DIAMOND;
        private int inputAmount = 1;
        private Material outputItem = Material.EMERALD;
        private int outputAmount = 1;
        
        public int getEditingIndex() { return editingIndex; }
        public void setEditingIndex(int index) { this.editingIndex = index; }
        public Material getInputItem() { return inputItem; }
        public void setInputItem(Material item) { this.inputItem = item; }
        public int getInputAmount() { return inputAmount; }
        public void setInputAmount(int amount) { this.inputAmount = Math.max(1, Math.min(64, amount)); }
        public Material getOutputItem() { return outputItem; }
        public void setOutputItem(Material item) { this.outputItem = item; }
        public int getOutputAmount() { return outputAmount; }
        public void setOutputAmount(int amount) { this.outputAmount = Math.max(1, Math.min(64, amount)); }
        
        public boolean isNewRecipe() { return editingIndex == -1; }
    }
    
    public RecipeEditData getOrCreateEditData(Player player) {
        return editingRecipes.computeIfAbsent(player.getUniqueId(), k -> new RecipeEditData());
    }
    
    public void clearEditData(Player player) {
        editingRecipes.remove(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory inventory = event.getInventory();
        String title = getInventoryTitle(inventory);

        if (title == null) {
            return;
        }

        // Handle Player Toggle GUI
        if (title.equals(PlayerToggleGUI.getGuiTitle())) {
            handlePlayerToggleGUI(event, player);
            return;
        }

        // Handle Admin Main GUI
        if (title.equals(AdminConfigGUI.MAIN_GUI_TITLE)) {
            handleAdminMainGUI(event, player);
            return;
        }

        // Handle Converter Recipes List GUI (new multi-recipe GUI)
        if (title.equals(AdminConfigGUI.CONVERTER_GUI_TITLE)) {
            handleConverterRecipesGUI(event, player);
            return;
        }
        
        // Handle Add/Edit Recipe GUI
        if (title.equals(AdminConfigGUI.EDIT_RECIPE_GUI_TITLE)) {
            handleEditRecipeGUI(event, player);
            return;
        }

        // Handle Smelting Config GUI
        if (title.equals(AdminConfigGUI.SMELTING_GUI_TITLE)) {
            handleSmeltingGUI(event, player);
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        String title = getInventoryTitle(event.getInventory());
        if (title == null) {
            return;
        }

        int inventorySize = event.getInventory().getSize();
        
        // For custom GUIs, only cancel if dragging affects GUI slots (not player inventory)
        if (title.equals(PlayerToggleGUI.getGuiTitle()) ||
                title.equals(AdminConfigGUI.MAIN_GUI_TITLE) ||
                title.equals(AdminConfigGUI.CONVERTER_GUI_TITLE) ||
                title.equals(AdminConfigGUI.SMELTING_GUI_TITLE) ||
                title.equals(AdminConfigGUI.EDIT_RECIPE_GUI_TITLE)) {
            
            // Check if any of the dragged slots are in the GUI area
            for (int slot : event.getRawSlots()) {
                if (slot < inventorySize) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private void handlePlayerToggleGUI(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        int slot = event.getRawSlot();

        // Auto Pickup toggle (slot 11)
        if (slot == 11) {
            plugin.getPlayerDataManager().toggleAutoPickup(player);
            boolean enabled = plugin.getPlayerDataManager().isAutoPickupEnabled(player);
            String messageKey = enabled ? "auto-pickup-enabled" : "auto-pickup-disabled";
            player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), messageKey));
            player.closeInventory();
            // Reopen GUI to show updated status
            new PlayerToggleGUI(plugin).openGUI(player);
        }

        // Auto Smelt toggle (slot 15)
        if (slot == 15) {
            plugin.getPlayerDataManager().toggleAutoSmelt(player);
            boolean enabled = plugin.getPlayerDataManager().isAutoSmeltEnabled(player);
            String messageKey = enabled ? "auto-smelt-enabled" : "auto-smelt-disabled";
            player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), messageKey));
            player.closeInventory();
            // Reopen GUI to show updated status
            new PlayerToggleGUI(plugin).openGUI(player);
        }
    }

    private void handleAdminMainGUI(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        int slot = event.getRawSlot();

        // Ore Converter Config (slot 11)
        if (slot == 11) {
            new AdminConfigGUI(plugin).openConverterGUI(player);
        }

        // Auto Smelt Config (slot 15)
        if (slot == 15) {
            new AdminConfigGUI(plugin).openSmeltingGUI(player);
        }
    }

    private void handleConverterRecipesGUI(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();
        int inventorySize = event.getInventory().getSize();
        
        // Allow clicks in player's own inventory (bottom section) for picking up items
        if (slot >= inventorySize) {
            return; // Don't cancel - let player pick up items
        }
        
        // Cancel GUI area clicks
        event.setCancelled(true);
        
        ConverterManager cm = plugin.getConverterManager();
        
        // Add New Recipe button (slot 45)
        if (slot == 45) {
            if (cm.getRecipeCount() >= ConverterManager.MAX_RECIPES) {
                player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "recipe-limit-reached"));
                return;
            }
            // Open add recipe GUI
            RecipeEditData editData = getOrCreateEditData(player);
            editData.setEditingIndex(-1); // New recipe
            editData.setInputItem(Material.DIAMOND);
            editData.setInputAmount(1);
            editData.setOutputItem(Material.EMERALD);
            editData.setOutputAmount(1);
            new AdminConfigGUI(plugin).openEditRecipeGUI(player, editData);
            return;
        }
        
        // Back button (slot 49)
        if (slot == 49) {
            clearEditData(player);
            new AdminConfigGUI(plugin).openMainGUI(player);
            return;
        }
        
        // Check if clicking on a recipe to edit it
        // Recipes are displayed in rows, each taking 3 slots (input, arrow, output)
        // Row 1: slots 10, 11, 12 (recipe 0) and 14, 15, 16 (recipe 1)
        // Row 2: slots 19, 20, 21 (recipe 2) and 23, 24, 25 (recipe 3)
        // Row 3: slots 28, 29, 30 (recipe 4) and 32, 33, 34 (recipe 5)
        // Row 4: slots 37, 38, 39 (recipe 6) and 41, 42, 43 (recipe 7)
        
        int recipeIndex = getRecipeIndexFromSlot(slot);
        if (recipeIndex >= 0 && recipeIndex < cm.getRecipeCount()) {
            // Edit this recipe
            ConversionRecipe recipe = cm.getRecipe(recipeIndex);
            if (recipe != null) {
                RecipeEditData editData = getOrCreateEditData(player);
                editData.setEditingIndex(recipeIndex);
                editData.setInputItem(recipe.getInputItem());
                editData.setInputAmount(recipe.getInputAmount());
                editData.setOutputItem(recipe.getOutputItem());
                editData.setOutputAmount(recipe.getOutputAmount());
                new AdminConfigGUI(plugin).openEditRecipeGUI(player, editData);
            }
        }
    }
    
    /**
     * Get the recipe index from a slot in the converter recipes GUI
     */
    private int getRecipeIndexFromSlot(int slot) {
        // Recipe slot positions (each recipe uses 3 consecutive slots)
        // Left column: recipes 0, 2, 4, 6 at rows starting at slots 10, 19, 28, 37
        // Right column: recipes 1, 3, 5, 7 at rows starting at slots 14, 23, 32, 41
        
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
        
        for (int i = 0; i < recipeSlots.length; i++) {
            for (int s : recipeSlots[i]) {
                if (slot == s) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void handleEditRecipeGUI(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();
        int inventorySize = event.getInventory().getSize();
        ItemStack cursor = event.getCursor();
        
        // Allow clicks in player's own inventory (bottom section) for picking up items
        if (slot >= inventorySize) {
            return; // Don't cancel - let player pick up items
        }
        
        // Cancel GUI area clicks
        event.setCancelled(true);
        
        RecipeEditData editData = getOrCreateEditData(player);
        ConverterManager cm = plugin.getConverterManager();
        
        // Input item slot (slot 10) - Allow drag and drop
        if (slot == 10) {
            if (cursor != null && cursor.getType() != Material.AIR) {
                editData.setInputItem(cursor.getType());
                new AdminConfigGUI(plugin).openEditRecipeGUI(player, editData);
            }
            return;
        }
        
        // Input amount -1 button (slot 11)
        if (slot == 11) {
            editData.setInputAmount(editData.getInputAmount() - 1);
            new AdminConfigGUI(plugin).openEditRecipeGUI(player, editData);
            return;
        }
        
        // Input amount +1 button (slot 12)
        if (slot == 12) {
            editData.setInputAmount(editData.getInputAmount() + 1);
            new AdminConfigGUI(plugin).openEditRecipeGUI(player, editData);
            return;
        }
        
        // Output item slot (slot 14) - Allow drag and drop
        if (slot == 14) {
            if (cursor != null && cursor.getType() != Material.AIR) {
                editData.setOutputItem(cursor.getType());
                new AdminConfigGUI(plugin).openEditRecipeGUI(player, editData);
            }
            return;
        }
        
        // Output amount -1 button (slot 15)
        if (slot == 15) {
            editData.setOutputAmount(editData.getOutputAmount() - 1);
            new AdminConfigGUI(plugin).openEditRecipeGUI(player, editData);
            return;
        }
        
        // Output amount +1 button (slot 16)
        if (slot == 16) {
            editData.setOutputAmount(editData.getOutputAmount() + 1);
            new AdminConfigGUI(plugin).openEditRecipeGUI(player, editData);
            return;
        }
        
        // Delete button (slot 19) - only for existing recipes
        if (slot == 19 && !editData.isNewRecipe()) {
            cm.removeRecipe(editData.getEditingIndex());
            player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "recipe-deleted"));
            clearEditData(player);
            new AdminConfigGUI(plugin).openConverterGUI(player);
            return;
        }
        
        // Save button (slot 22)
        if (slot == 22) {
            ConversionRecipe newRecipe = new ConversionRecipe(
                editData.getInputItem(),
                editData.getInputAmount(),
                editData.getOutputItem(),
                editData.getOutputAmount()
            );
            
            if (editData.isNewRecipe()) {
                if (cm.addRecipe(newRecipe)) {
                    player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "recipe-added"));
                } else {
                    player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "recipe-limit-reached"));
                }
            } else {
                cm.updateRecipe(editData.getEditingIndex(), newRecipe);
                player.sendMessage(ConfigUtils.getColoredMessage(plugin.getConfig(), "recipe-updated"));
            }
            
            clearEditData(player);
            new AdminConfigGUI(plugin).openConverterGUI(player);
            return;
        }
        
        // Back/Cancel button (slot 25)
        if (slot == 25) {
            clearEditData(player);
            new AdminConfigGUI(plugin).openConverterGUI(player);
            return;
        }
    }

    private void handleSmeltingGUI(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        int slot = event.getRawSlot();
        SmeltingManager sm = plugin.getSmeltingManager();

        // Back button (slot 53)
        if (slot == 53) {
            new AdminConfigGUI(plugin).openMainGUI(player);
            return;
        }

        // Toggle smelting for clicked item
        if (slot >= 0 && slot < 53) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                Material material = clickedItem.getType();
                if (sm.getAllSmeltableItems().contains(material)) {
                    sm.toggleItem(material);
                    // Refresh GUI
                    new AdminConfigGUI(plugin).openSmeltingGUI(player);
                }
            }
        }
    }

    private String getInventoryTitle(Inventory inventory) {
        if (inventory.getViewers().isEmpty()) {
            return null;
        }

        try {
            return PLAIN.serialize(inventory.getViewers().get(0).getOpenInventory().title());
        } catch (Exception e) {
            return null;
        }
    }
}
