package com.autopickup.listeners;

import com.autopickup.AutoPickupPlugin;
import com.autopickup.items.OreConverterItem;
import com.autopickup.managers.ConversionRecipe;
import com.autopickup.managers.ConverterManager;
import com.autopickup.managers.PlayerDataManager;
import com.autopickup.managers.SmeltingManager;
import com.autopickup.utils.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;

public class BlockBreakListener implements Listener {

    private final AutoPickupPlugin plugin;

    public BlockBreakListener(AutoPickupPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Skip if player is in creative mode
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Check if player has permission
        if (!player.hasPermission("autopickup.use")) {
            return;
        }

        PlayerDataManager pdm = plugin.getPlayerDataManager();

        // Check if auto pickup is enabled for this player
        if (!pdm.isAutoPickupEnabled(player)) {
            return;
        }

        // Get the drops
        Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());

        if (drops.isEmpty()) {
            return;
        }

        // Cancel the default drops
        event.setDropItems(false);

        // Process each drop
        SmeltingManager sm = plugin.getSmeltingManager();
        ConverterManager cm = plugin.getConverterManager();
        OreConverterItem oci = plugin.getOreConverterItem();
        ItemStack offhandItem = player.getInventory().getItemOffHand();
        boolean hasConverter = player.hasPermission("autopickup.converter") && oci.isConverterItem(offhandItem);

        for (ItemStack drop : drops) {
            ItemStack finalDrop = drop.clone();

            // Check for ore converter in offhand - uses accumulator system
            if (hasConverter) {
                ConversionRecipe recipe = cm.findRecipeForItem(finalDrop.getType());
                if (recipe != null) {
                    finalDrop = processConverterWithAccumulator(finalDrop, recipe, player, cm);
                    // If absorbed into accumulator, skip to next drop
                    if (finalDrop == null) {
                        continue;
                    }
                }
            }

            // Check for auto smelt
            if (pdm.isAutoSmeltEnabled(player) && sm.canSmelt(finalDrop.getType())) {
                finalDrop = sm.getSmeltedItem(finalDrop);
            }

            // Try to add to inventory
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(finalDrop);

            // If items couldn't fit in inventory, delete them instead of dropping
            // (leftover items are now deleted rather than dropped on ground)
            if (!leftover.isEmpty()) {
                // Items are simply not added to the world - they're deleted
                // Could add optional message to player here if desired
            }
        }

        // Give experience if block would normally give experience
        int exp = event.getExpToDrop();
        if (exp > 0) {
            player.giveExp(exp);
        }
    }

    /**
     * Process converter with accumulator system - items are absorbed until threshold is reached.
     */
    private ItemStack processConverterWithAccumulator(ItemStack drop, ConversionRecipe recipe, Player player, ConverterManager cm) {
        Material inputMaterial = drop.getType();
        int dropAmount = drop.getAmount();
        
        // Add to accumulator
        cm.addToAccumulator(player.getUniqueId(), inputMaterial, dropAmount);
        
        // Check accumulated amount
        int accumulated = cm.getAccumulatedAmount(player.getUniqueId(), inputMaterial);
        int inputRequired = recipe.getInputAmount();
        
        if (accumulated >= inputRequired) {
            // Perform conversion
            int conversions = accumulated / inputRequired;
            int remainder = accumulated % inputRequired;
            
            // Give converted items using the full ItemStack (preserves custom name, lore, enchants, etc.)
            ItemStack outputItem = recipe.getOutputItemStack();
            outputItem.setAmount(conversions * recipe.getOutputAmount());
            
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(outputItem);
            
            // If converted items couldn't fit, delete them instead of dropping
            // (leftover items are now deleted rather than dropped on ground)
            if (!leftover.isEmpty()) {
                // Items are simply not added to the world - they're deleted
            }
            
            // Update accumulator with remainder
            cm.setAccumulator(player.getUniqueId(), inputMaterial, remainder);
            
            // Show conversion message
            sendConversionMessage(player, recipe, conversions, remainder);
        } else {
            // Show progress in action bar
            sendProgressActionBar(player, accumulated, inputRequired, inputMaterial);
        }
        
        // Return null - item has been absorbed into accumulator or converted
        return null;
    }
    
    /**
     * Send action bar showing accumulator progress.
     */
    private void sendProgressActionBar(Player player, int accumulated, int required, Material material) {
        Component message = Component.text("Converter: ", NamedTextColor.GRAY)
                .append(Component.text(accumulated, NamedTextColor.YELLOW))
                .append(Component.text("/" + required + " ", NamedTextColor.GOLD))
                .append(Component.text(ConfigUtils.formatMaterialName(material), NamedTextColor.AQUA));
        player.sendActionBar(message);
    }
    
    /**
     * Send message when conversion completes.
     */
    private void sendConversionMessage(Player player, ConversionRecipe recipe, int conversions, int remainder) {
        String outputName = ConfigUtils.formatMaterialName(recipe.getOutputItem());
        int outputTotal = conversions * recipe.getOutputAmount();
        
        Component message = Component.text("Converted! ", NamedTextColor.GREEN)
                .append(Component.text("+" + outputTotal + " ", NamedTextColor.YELLOW))
                .append(Component.text(outputName, NamedTextColor.AQUA));
        
        if (remainder > 0) {
            message = message.append(Component.text(" (" + remainder + " remaining)", NamedTextColor.GRAY));
        }
        
        player.sendActionBar(message);
    }
}
