package com.autopickup.listeners;

import com.autopickup.AutoPickupPlugin;
import com.autopickup.items.OreConverterItem;
import com.autopickup.managers.ConverterManager;
import com.autopickup.managers.PlayerDataManager;
import com.autopickup.managers.SmeltingManager;
import org.bukkit.GameMode;
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

            // Check for ore converter in offhand
            if (hasConverter && finalDrop.getType() == cm.getInputItem()) {
                finalDrop = processConverter(finalDrop, cm, player);
                // If fully converted, skip to next drop
                if (finalDrop == null) {
                    continue;
                }
            }

            // Check for auto smelt
            if (pdm.isAutoSmeltEnabled(player) && sm.canSmelt(finalDrop.getType())) {
                finalDrop = sm.getSmeltedItem(finalDrop);
            }

            // Try to add to inventory
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(finalDrop);

            // Drop any items that couldn't fit in inventory
            if (!leftover.isEmpty()) {
                for (ItemStack item : leftover.values()) {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
        }

        // Give experience if block would normally give experience
        int exp = event.getExpToDrop();
        if (exp > 0) {
            player.giveExp(exp);
        }
    }

    private ItemStack processConverter(ItemStack drop, ConverterManager cm, Player player) {
        int totalAmount = drop.getAmount();
        int conversions = cm.getConversionCount(totalAmount);
        int remainder = cm.getRemainder(totalAmount);

        if (conversions > 0) {
            // Give converted items
            ItemStack convertedItem = new ItemStack(cm.getOutputItem(), conversions * cm.getOutputAmount());
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(convertedItem);

            // Drop any converted items that couldn't fit
            if (!leftover.isEmpty()) {
                for (ItemStack item : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
        }

        // Return remainder (or null if fully converted)
        if (remainder > 0) {
            return new ItemStack(drop.getType(), remainder);
        } else {
            return null;
        }
    }
}
