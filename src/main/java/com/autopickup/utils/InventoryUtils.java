package com.autopickup.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

/**
 * Utility class for inventory operations.
 */
public class InventoryUtils {

    /**
     * Count how many of a specific material the player has in their inventory.
     * Only counts items in storage slots (not armor/offhand).
     */
    public static int countItemsInInventory(PlayerInventory inventory, Material material) {
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
     * Count how many of a specific material the player has in their inventory.
     * Only counts items in storage slots (not armor/offhand).
     * Only counts plain vanilla items without custom NBT data.
     */
    public static int countPlainItemsInInventory(PlayerInventory inventory, Material material) {
        int count = 0;
        ItemStack[] storageContents = inventory.getStorageContents();
        for (ItemStack item : storageContents) {
            if (item != null && item.getType() == material) {
                // Only count items without custom NBT data to avoid converting enchanted/custom items
                if (!item.hasItemMeta() || !hasCustomData(item)) {
                    count += item.getAmount();
                }
            }
        }
        return count;
    }

    /**
     * Check if an item has custom data (display name, lore, enchantments, etc.)
     */
    public static boolean hasCustomData(ItemStack item) {
        if (!item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() || 
               meta.hasLore() || 
               meta.hasEnchants() || 
               meta.hasAttributeModifiers();
    }

    /**
     * Remove a specific amount of a material from player's inventory.
     * Only removes from storage slots (not armor/offhand).
     * @return true if the full amount was removed, false otherwise
     */
    public static boolean removeItemsFromInventory(PlayerInventory inventory, Material material, int amount) {
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
     * Remove a specific amount of a material from player's inventory.
     * Only removes from storage slots (not armor/offhand).
     * Only removes plain vanilla items without custom NBT data.
     * @return true if the full amount was removed, false otherwise
     */
    public static boolean removePlainItemsFromInventory(PlayerInventory inventory, Material material, int amount) {
        int remaining = amount;
        ItemStack[] storageContents = inventory.getStorageContents();
        
        for (int i = 0; i < storageContents.length && remaining > 0; i++) {
            ItemStack item = storageContents[i];
            if (item != null && item.getType() == material) {
                // Only remove items without custom NBT data to avoid removing enchanted/custom items
                if (!item.hasItemMeta() || !hasCustomData(item)) {
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
        }
        
        // Update the inventory with modified storage contents
        inventory.setStorageContents(storageContents);
        return remaining == 0;
    }

    /**
     * Give items to player, handling stacking and overflow deletion.
     * Items that don't fit are deleted (not dropped on ground).
     */
    public static void giveItemsToPlayer(Player player, ItemStack baseItem, int totalAmount) {
        PlayerInventory inventory = player.getInventory();
        int remaining = totalAmount;
        
        while (remaining > 0) {
            ItemStack toGive = baseItem.clone();
            int stackSize = Math.min(remaining, toGive.getMaxStackSize());
            toGive.setAmount(stackSize);
            
            HashMap<Integer, ItemStack> leftover = inventory.addItem(toGive);
            
            // If items couldn't fit, they are deleted (as per requirement)
            if (!leftover.isEmpty()) {
                // Items are simply not added to the world - they're deleted
                break;
            }
            
            remaining -= stackSize;
        }
    }
}
