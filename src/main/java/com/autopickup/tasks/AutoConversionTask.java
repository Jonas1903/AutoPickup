package com.autopickup.tasks;

import com.autopickup.AutoPickupPlugin;
import com.autopickup.managers.ConversionRecipe;
import com.autopickup.managers.ConverterManager;
import com.autopickup.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Periodic task that automatically converts items in players' inventories
 * based on configured recipes.
 */
public class AutoConversionTask extends BukkitRunnable {

    private final AutoPickupPlugin plugin;

    public AutoConversionTask(AutoPickupPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ConverterManager cm = plugin.getConverterManager();
        
        // Process all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Skip if player doesn't have permission
            if (!player.hasPermission("autopickup.autoconvert")) {
                continue;
            }
            
            processPlayerInventory(player, cm);
        }
    }

    /**
     * Process a player's inventory and automatically convert matching items.
     */
    private void processPlayerInventory(Player player, ConverterManager cm) {
        PlayerInventory inventory = player.getInventory();
        
        // Check each recipe
        for (ConversionRecipe recipe : cm.getRecipes()) {
            Material inputMaterial = recipe.getInputItem();
            int inputRequired = recipe.getInputAmount();
            
            // Count how many of the input item the player has (only plain items)
            int totalInputInInventory = InventoryUtils.countPlainItemsInInventory(inventory, inputMaterial);
            
            // Calculate how many conversions we can do
            int possibleConversions = totalInputInInventory / inputRequired;
            
            if (possibleConversions > 0) {
                // Remove input items from inventory (only plain items)
                int toRemove = possibleConversions * inputRequired;
                if (InventoryUtils.removePlainItemsFromInventory(inventory, inputMaterial, toRemove)) {
                    // Give output items to player (preserving NBT data from recipe)
                    ItemStack outputItem = recipe.getOutputItemStack();
                    int outputAmount = possibleConversions * recipe.getOutputAmount();
                    InventoryUtils.giveItemsToPlayer(player, outputItem, outputAmount);
                }
            }
        }
    }

    /**
     * Start the periodic task.
     */
    public void start() {
        // Get interval from config (in seconds), default to 5 seconds
        int intervalSeconds = plugin.getConfig().getInt("ore-converter.auto-conversion-interval", 5);
        // Convert to ticks (20 ticks = 1 second)
        int intervalTicks = intervalSeconds * 20;
        
        this.runTaskTimer(plugin, intervalTicks, intervalTicks);
        plugin.getLogger().info("Auto-conversion task started with interval: " + intervalSeconds + " seconds");
    }
}
