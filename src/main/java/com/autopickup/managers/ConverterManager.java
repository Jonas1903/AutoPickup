package com.autopickup.managers;

import com.autopickup.AutoPickupPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConverterManager {

    private final AutoPickupPlugin plugin;
    private final List<ConversionRecipe> recipes = new ArrayList<>();
    
    // Accumulator system - tracks mined items per player per material
    private final Map<UUID, Map<Material, Integer>> playerAccumulators = new HashMap<>();
    
    // Maximum number of recipes allowed
    public static final int MAX_RECIPES = 8;

    public ConverterManager(AutoPickupPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        recipes.clear();
        
        // Check if new format exists (recipes list)
        if (plugin.getConfig().contains("ore-converter.recipes")) {
            loadRecipesFromList();
        } else {
            // Legacy format: single recipe
            loadLegacyRecipe();
        }
        
        // Ensure at least one default recipe if none loaded
        if (recipes.isEmpty()) {
            recipes.add(new ConversionRecipe(Material.DIAMOND, 64, Material.AMETHYST_SHARD, 10));
            plugin.getLogger().info("No recipes found, added default recipe");
        }
        
        plugin.getLogger().info("Loaded " + recipes.size() + " ore converter recipe(s)");
    }
    
    private void loadRecipesFromList() {
        List<Map<?, ?>> recipeList = plugin.getConfig().getMapList("ore-converter.recipes");
        
        for (Map<?, ?> recipeMap : recipeList) {
            try {
                String inputItemStr = (String) recipeMap.get("input-item");
                Object inputAmountObj = recipeMap.get("input-amount");
                Object outputAmountObj = recipeMap.get("output-amount");
                
                Material inputItem = Material.valueOf(inputItemStr.toUpperCase());
                int inputAmount = inputAmountObj instanceof Number ? ((Number) inputAmountObj).intValue() : 1;
                int outputAmount = outputAmountObj instanceof Number ? ((Number) outputAmountObj).intValue() : 1;
                
                // Try to load full ItemStack first, fallback to Material
                ItemStack outputItemStack;
                if (recipeMap.containsKey("output-itemstack") && recipeMap.get("output-itemstack") instanceof Map) {
                    // ItemStack is serialized as a map in YAML
                    @SuppressWarnings("unchecked")
                    Map<String, Object> itemStackMap = (Map<String, Object>) recipeMap.get("output-itemstack");
                    outputItemStack = ItemStack.deserialize(itemStackMap);
                } else {
                    // Legacy fallback - use output-item Material
                    String outputItemStr = (String) recipeMap.get("output-item");
                    Material outputItem = outputItemStr != null ? Material.valueOf(outputItemStr.toUpperCase()) : Material.AMETHYST_SHARD;
                    outputItemStack = new ItemStack(outputItem);
                }
                
                recipes.add(new ConversionRecipe(inputItem, inputAmount, outputItemStack, outputAmount));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load a recipe from config: " + e.getMessage());
            }
        }
    }
    
    private void loadLegacyRecipe() {
        String inputItemStr = plugin.getConfig().getString("ore-converter.input-item", "DIAMOND");
        String outputItemStr = plugin.getConfig().getString("ore-converter.output-item", "AMETHYST_SHARD");
        int inputAmount = plugin.getConfig().getInt("ore-converter.input-amount", 64);
        int outputAmount = plugin.getConfig().getInt("ore-converter.output-amount", 10);
        
        Material inputItem;
        Material outputItem;
        
        try {
            inputItem = Material.valueOf(inputItemStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            inputItem = Material.DIAMOND;
            plugin.getLogger().warning("Invalid input item in config, defaulting to DIAMOND");
        }
        
        try {
            outputItem = Material.valueOf(outputItemStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            outputItem = Material.AMETHYST_SHARD;
            plugin.getLogger().warning("Invalid output item in config, defaulting to AMETHYST_SHARD");
        }
        
        recipes.add(new ConversionRecipe(inputItem, inputAmount, outputItem, outputAmount));
    }

    public void saveRecipes() {
        // Clear old format
        plugin.getConfig().set("ore-converter.input-item", null);
        plugin.getConfig().set("ore-converter.input-amount", null);
        plugin.getConfig().set("ore-converter.output-item", null);
        plugin.getConfig().set("ore-converter.output-amount", null);
        
        // Save as list format with full ItemStack support
        List<Map<String, Object>> recipeList = new ArrayList<>();
        for (ConversionRecipe recipe : recipes) {
            Map<String, Object> recipeMap = new LinkedHashMap<>();
            recipeMap.put("input-item", recipe.getInputItem().name());
            recipeMap.put("input-amount", recipe.getInputAmount());
            recipeMap.put("output-amount", recipe.getOutputAmount());
            // Save full ItemStack (serialized)
            recipeMap.put("output-itemstack", recipe.getOutputItemStack().serialize());
            // Also save output-item for backward compatibility/readability
            recipeMap.put("output-item", recipe.getOutputItem().name());
            recipeList.add(recipeMap);
        }
        
        plugin.getConfig().set("ore-converter.recipes", recipeList);
        plugin.saveConfig();
        plugin.getLogger().info("Saved " + recipes.size() + " ore converter recipe(s)");
    }
    
    // ===== Accumulator System =====
    
    /**
     * Add items to a player's accumulator for a specific material.
     */
    public void addToAccumulator(UUID uuid, Material material, int amount) {
        playerAccumulators.computeIfAbsent(uuid, k -> new HashMap<>());
        Map<Material, Integer> playerAcc = playerAccumulators.get(uuid);
        int current = playerAcc.getOrDefault(material, 0);
        playerAcc.put(material, current + amount);
    }
    
    /**
     * Get the accumulated amount for a player and material.
     */
    public int getAccumulatedAmount(UUID uuid, Material material) {
        Map<Material, Integer> playerAcc = playerAccumulators.get(uuid);
        if (playerAcc == null) return 0;
        return playerAcc.getOrDefault(material, 0);
    }
    
    /**
     * Consume items from a player's accumulator.
     */
    public void consumeFromAccumulator(UUID uuid, Material material, int amount) {
        Map<Material, Integer> playerAcc = playerAccumulators.get(uuid);
        if (playerAcc != null) {
            int current = playerAcc.getOrDefault(material, 0);
            playerAcc.put(material, Math.max(0, current - amount));
        }
    }
    
    /**
     * Set the accumulator value directly (used for setting remainder after conversion).
     */
    public void setAccumulator(UUID uuid, Material material, int amount) {
        playerAccumulators.computeIfAbsent(uuid, k -> new HashMap<>());
        Map<Material, Integer> playerAcc = playerAccumulators.get(uuid);
        playerAcc.put(material, Math.max(0, amount));
    }
    
    /**
     * Clear all accumulators for a player.
     */
    public void clearAccumulators(UUID uuid) {
        playerAccumulators.remove(uuid);
    }
    
    /**
     * Clear a specific material accumulator for a player.
     */
    public void clearAccumulator(UUID uuid, Material material) {
        Map<Material, Integer> playerAcc = playerAccumulators.get(uuid);
        if (playerAcc != null) {
            playerAcc.remove(material);
        }
    }
    
    /**
     * Get all accumulated materials for a player.
     */
    public Map<Material, Integer> getPlayerAccumulators(UUID uuid) {
        Map<Material, Integer> playerAcc = playerAccumulators.get(uuid);
        if (playerAcc == null) return new HashMap<>();
        return new HashMap<>(playerAcc);
    }
    
    // ===== Recipe Management =====
    
    /**
     * Get all conversion recipes.
     */
    public List<ConversionRecipe> getRecipes() {
        return new ArrayList<>(recipes);
    }
    
    /**
     * Get a recipe by index.
     */
    public ConversionRecipe getRecipe(int index) {
        if (index >= 0 && index < recipes.size()) {
            return recipes.get(index);
        }
        return null;
    }
    
    /**
     * Add a new recipe.
     */
    public boolean addRecipe(ConversionRecipe recipe) {
        if (recipes.size() >= MAX_RECIPES) {
            return false;
        }
        recipes.add(recipe);
        saveRecipes();
        return true;
    }
    
    /**
     * Update an existing recipe.
     */
    public boolean updateRecipe(int index, ConversionRecipe recipe) {
        if (index >= 0 && index < recipes.size()) {
            recipes.set(index, recipe);
            saveRecipes();
            return true;
        }
        return false;
    }
    
    /**
     * Remove a recipe by index.
     */
    public boolean removeRecipe(int index) {
        if (index >= 0 && index < recipes.size()) {
            recipes.remove(index);
            saveRecipes();
            return true;
        }
        return false;
    }
    
    /**
     * Get the number of recipes.
     */
    public int getRecipeCount() {
        return recipes.size();
    }
    
    /**
     * Find a recipe that matches the given item type.
     */
    public ConversionRecipe findRecipeForItem(Material itemType) {
        for (ConversionRecipe recipe : recipes) {
            if (recipe.getInputItem() == itemType) {
                return recipe;
            }
        }
        return null;
    }

    // ===== Legacy methods for backward compatibility =====
    
    /**
     * @deprecated Use getRecipes() instead
     */
    @Deprecated
    public Material getInputItem() {
        return recipes.isEmpty() ? Material.DIAMOND : recipes.get(0).getInputItem();
    }

    /**
     * @deprecated Use getRecipes() instead
     */
    @Deprecated
    public int getInputAmount() {
        return recipes.isEmpty() ? 64 : recipes.get(0).getInputAmount();
    }

    /**
     * @deprecated Use getRecipes() instead
     */
    @Deprecated
    public Material getOutputItem() {
        return recipes.isEmpty() ? Material.AMETHYST_SHARD : recipes.get(0).getOutputItem();
    }

    /**
     * @deprecated Use getRecipes() instead
     */
    @Deprecated
    public int getOutputAmount() {
        return recipes.isEmpty() ? 10 : recipes.get(0).getOutputAmount();
    }
}
