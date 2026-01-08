package com.autopickup.tasks;

import com.autopickup.AutoPickupPlugin;
import com.autopickup.managers.ConversionRecipe;
import com.autopickup.managers.ConverterManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

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
            
            // Count how many of the input item the player has
            int totalInputInInventory = countItemsInInventory(inventory, inputMaterial);
            
            // Calculate how many conversions we can do
            int possibleConversions = totalInputInInventory / inputRequired;
            
            if (possibleConversions > 0) {
                // Remove input items from inventory
                int toRemove = possibleConversions * inputRequired;
                if (removeItemsFromInventory(inventory, inputMaterial, toRemove)) {
                    // Give output items to player (preserving NBT data from recipe)
                    ItemStack outputItem = recipe.getOutputItemStack();
                    int outputAmount = possibleConversions * recipe.getOutputAmount();
                    giveItemsToPlayer(player, outputItem, outputAmount);
                }
            }
        }
    }

    /**
     * Count how many of a specific material the player has in their inventory.
     * Only counts items in storage slots (not armor/offhand).
     */
    private int countItemsInInventory(PlayerInventory inventory, Material material) {
        int count = 0;
        ItemStack[] storageContents = inventory.getStorageContents();
        for (ItemStack item : storageContents) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Remove a specific amount of a material from player's inventory.
     * Only removes from storage slots (not armor/offhand).
     * @return true if the full amount was removed, false otherwise
     */
    private boolean removeItemsFromInventory(PlayerInventory inventory, Material material, int amount) {
        int remaining = amount;
        ItemStack[] storageContents = inventory.getStorageContents();
        
        for (int i = 0; i < storageContents.length && remaining > 0; i++) {
            ItemStack item = storageContents[i];
            if (item != null && item.getType() == material) {
                int stackAmount = item.getAmount();
                if (stackAmount <= remaining) {
                    storageContents[i] = null;
                    remaining -= stackAmount;
                } else {
                    item.setAmount(stackAmount - remaining);
                    remaining = 0;
                }
            }
        }
        
        // Update the inventory with modified storage contents
        inventory.setStorageContents(storageContents);
        return remaining == 0;
    }

    /**
     * Give items to player, handling stacking and overflow deletion.
     * Items that don't fit are deleted (not dropped on ground).
     */
    private void giveItemsToPlayer(Player player, ItemStack baseItem, int totalAmount) {
        PlayerInventory inventory = player.getInventory();
        int remaining = totalAmount;
        
        while (remaining > 0) {
            ItemStack toGive = baseItem.clone();
            int stackSize = Math.min(remaining, toGive.getMaxStackSize());
            toGive.setAmount(stackSize);
            
            HashMap<Integer, ItemStack> leftover = inventory.addItem(toGive);
            
            // If items couldn't fit, they are deleted (as per requirement #3)
            if (!leftover.isEmpty()) {
                // Items are simply not added to the world - they're deleted
                break;
            }
            
            remaining -= stackSize;
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
