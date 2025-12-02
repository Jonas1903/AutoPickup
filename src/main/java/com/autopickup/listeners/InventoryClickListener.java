package com.autopickup.listeners;

import com.autopickup.AutoPickupPlugin;
import com.autopickup.gui.AdminConfigGUI;
import com.autopickup.gui.PlayerToggleGUI;
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

public class InventoryClickListener implements Listener {

    private final AutoPickupPlugin plugin;
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    public InventoryClickListener(AutoPickupPlugin plugin) {
        this.plugin = plugin;
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

        // Handle Converter Config GUI
        if (title.equals(AdminConfigGUI.CONVERTER_GUI_TITLE)) {
            handleConverterGUI(event, player);
            return;
        }

        // Handle Smelting Config GUI
        if (title.equals(AdminConfigGUI.SMELTING_GUI_TITLE)) {
            handleSmeltingGUI(event, player);
            return;
        }

        // Handle Input Amount GUI
        if (title.equals(AdminConfigGUI.INPUT_AMOUNT_GUI_TITLE)) {
            handleInputAmountGUI(event, player);
            return;
        }

        // Handle Output Amount GUI
        if (title.equals(AdminConfigGUI.OUTPUT_AMOUNT_GUI_TITLE)) {
            handleOutputAmountGUI(event, player);
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

        // Cancel drag in any custom GUI
        if (title.equals(PlayerToggleGUI.getGuiTitle()) ||
                title.equals(AdminConfigGUI.MAIN_GUI_TITLE) ||
                title.equals(AdminConfigGUI.CONVERTER_GUI_TITLE) ||
                title.equals(AdminConfigGUI.SMELTING_GUI_TITLE) ||
                title.equals(AdminConfigGUI.INPUT_AMOUNT_GUI_TITLE) ||
                title.equals(AdminConfigGUI.OUTPUT_AMOUNT_GUI_TITLE)) {
            event.setCancelled(true);
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

    private void handleConverterGUI(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();
        ItemStack cursor = event.getCursor();
        ConverterManager cm = plugin.getConverterManager();

        // Input slot (slot 19) - Allow drag and drop
        if (slot == 19) {
            event.setCancelled(true);
            if (cursor != null && cursor.getType() != Material.AIR) {
                cm.setInputItem(cursor.getType());
                new AdminConfigGUI(plugin).openConverterGUI(player);
            }
            return;
        }

        // Output slot (slot 25) - Allow drag and drop
        if (slot == 25) {
            event.setCancelled(true);
            if (cursor != null && cursor.getType() != Material.AIR) {
                cm.setOutputItem(cursor.getType());
                new AdminConfigGUI(plugin).openConverterGUI(player);
            }
            return;
        }

        // Input amount button (slot 28)
        if (slot == 28) {
            event.setCancelled(true);
            new AdminConfigGUI(plugin).openInputAmountGUI(player);
            return;
        }

        // Output amount button (slot 34)
        if (slot == 34) {
            event.setCancelled(true);
            new AdminConfigGUI(plugin).openOutputAmountGUI(player);
            return;
        }

        // Back button (slot 40)
        if (slot == 40) {
            event.setCancelled(true);
            new AdminConfigGUI(plugin).openMainGUI(player);
            return;
        }

        // Cancel all other clicks
        event.setCancelled(true);
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

    private void handleInputAmountGUI(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        int slot = event.getRawSlot();
        ConverterManager cm = plugin.getConverterManager();

        // -10 button (slot 10)
        if (slot == 10) {
            int newAmount = Math.max(1, cm.getInputAmount() - 10);
            cm.setInputAmount(newAmount);
            new AdminConfigGUI(plugin).openInputAmountGUI(player);
            return;
        }

        // -1 button (slot 11)
        if (slot == 11) {
            int newAmount = Math.max(1, cm.getInputAmount() - 1);
            cm.setInputAmount(newAmount);
            new AdminConfigGUI(plugin).openInputAmountGUI(player);
            return;
        }

        // +1 button (slot 15)
        if (slot == 15) {
            int newAmount = Math.min(64, cm.getInputAmount() + 1);
            cm.setInputAmount(newAmount);
            new AdminConfigGUI(plugin).openInputAmountGUI(player);
            return;
        }

        // +10 button (slot 16)
        if (slot == 16) {
            int newAmount = Math.min(64, cm.getInputAmount() + 10);
            cm.setInputAmount(newAmount);
            new AdminConfigGUI(plugin).openInputAmountGUI(player);
            return;
        }

        // Back button (slot 22)
        if (slot == 22) {
            new AdminConfigGUI(plugin).openConverterGUI(player);
        }
    }

    private void handleOutputAmountGUI(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        int slot = event.getRawSlot();
        ConverterManager cm = plugin.getConverterManager();

        // -10 button (slot 10)
        if (slot == 10) {
            int newAmount = Math.max(1, cm.getOutputAmount() - 10);
            cm.setOutputAmount(newAmount);
            new AdminConfigGUI(plugin).openOutputAmountGUI(player);
            return;
        }

        // -1 button (slot 11)
        if (slot == 11) {
            int newAmount = Math.max(1, cm.getOutputAmount() - 1);
            cm.setOutputAmount(newAmount);
            new AdminConfigGUI(plugin).openOutputAmountGUI(player);
            return;
        }

        // +1 button (slot 15)
        if (slot == 15) {
            int newAmount = Math.min(64, cm.getOutputAmount() + 1);
            cm.setOutputAmount(newAmount);
            new AdminConfigGUI(plugin).openOutputAmountGUI(player);
            return;
        }

        // +10 button (slot 16)
        if (slot == 16) {
            int newAmount = Math.min(64, cm.getOutputAmount() + 10);
            cm.setOutputAmount(newAmount);
            new AdminConfigGUI(plugin).openOutputAmountGUI(player);
            return;
        }

        // Back button (slot 22)
        if (slot == 22) {
            new AdminConfigGUI(plugin).openConverterGUI(player);
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
