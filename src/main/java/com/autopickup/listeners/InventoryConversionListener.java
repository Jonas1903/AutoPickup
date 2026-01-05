package com.autopickup.listeners;

import com.autopickup.AutoPickupPlugin;
import com.autopickup.items.OreConverterItem;
import com.autopickup.managers.ConversionRecipe;
import com.autopickup.managers.ConverterManager;
import com.autopickup.utils.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener to handle inventory conversion when player right-clicks with converter item.
 */
public class InventoryConversionListener implements Listener {

    private final AutoPickupPlugin plugin;

    public InventoryConversionListener(AutoPickupPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle offhand interactions to avoid duplicate events
        if (event.getHand() != EquipmentSlot.OFF_HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack offhandItem = player.getInventory().getItemOffHand();
        OreConverterItem oci = plugin.getOreConverterItem();

        // Check if player is holding converter item in offhand
        if (!player.hasPermission("autopickup.converter") || !oci.isConverterItem(offhandItem)) {
            return;
        }

        // Check if player is right-clicking (interact)
        if (!event.getAction().isRightClick()) {
            return;
        }

        // Cancel the event to prevent other interactions
        event.setCancelled(true);

        // Perform inventory conversion
        performInventoryConversion(player);
    }

    /**
     * Scans player's inventory for items matching recipes and converts them.
     */
    private void performInventoryConversion(Player player) {
        ConverterManager cm = plugin.getConverterManager();
        PlayerInventory inventory = player.getInventory();
        
        // Track total conversions per recipe
        Map<ConversionRecipe, Integer> conversionsPerRecipe = new HashMap<>();
        
        // Scan through all recipes
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
                removeItemsFromInventory(inventory, inputMaterial, toRemove);
                
                // Give output items to player
                ItemStack outputItem = recipe.getOutputItemStack();
                int outputAmount = possibleConversions * recipe.getOutputAmount();
                giveItemsToPlayer(player, outputItem, outputAmount);
                
                // Track conversion
                conversionsPerRecipe.put(recipe, possibleConversions);
            }
        }
        
        // Send feedback to player
        if (conversionsPerRecipe.isEmpty()) {
            player.sendMessage(Component.text("No items to convert!", NamedTextColor.RED));
        } else {
            player.sendMessage(Component.text("Converted inventory items:", NamedTextColor.GREEN));
            for (Map.Entry<ConversionRecipe, Integer> entry : conversionsPerRecipe.entrySet()) {
                ConversionRecipe recipe = entry.getKey();
                int conversions = entry.getValue();
                int totalInput = conversions * recipe.getInputAmount();
                int totalOutput = conversions * recipe.getOutputAmount();
                
                String inputName = ConfigUtils.formatMaterialName(recipe.getInputItem());
                String outputName = ConfigUtils.formatMaterialName(recipe.getOutputItem());
                
                player.sendMessage(Component.text("  - ", NamedTextColor.GRAY)
                        .append(Component.text(totalInput + "x " + inputName, NamedTextColor.YELLOW))
                        .append(Component.text(" → ", NamedTextColor.GOLD))
                        .append(Component.text(totalOutput + "x " + outputName, NamedTextColor.AQUA)));
            }
        }
    }

    /**
     * Count how many of a specific material the player has in their inventory.
     */
    private int countItemsInInventory(PlayerInventory inventory, Material material) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Remove a specific amount of a material from player's inventory.
     */
    private void removeItemsFromInventory(PlayerInventory inventory, Material material, int amount) {
        int remaining = amount;
        for (int i = 0; i < inventory.getSize() && remaining > 0; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == material) {
                int stackAmount = item.getAmount();
                if (stackAmount <= remaining) {
                    inventory.setItem(i, null);
                    remaining -= stackAmount;
                } else {
                    item.setAmount(stackAmount - remaining);
                    remaining = 0;
                }
            }
        }
    }

    /**
     * Give items to player, handling stacking and overflow deletion.
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
                int leftoverAmount = 0;
                for (ItemStack item : leftover.values()) {
                    leftoverAmount += item.getAmount();
                }
                // Inform player that some items were deleted
                if (leftoverAmount > 0) {
                    player.sendMessage(Component.text("Inventory full! ", NamedTextColor.RED)
                            .append(Component.text(leftoverAmount + " items deleted.", NamedTextColor.GRAY)));
                }
                break;
            }
            
            remaining -= stackSize;
        }
    }
}
