package com.autopickup.managers;

import com.autopickup.AutoPickupPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ConverterManager {

    private final AutoPickupPlugin plugin;
    private Material inputItem;
    private int inputAmount;
    private Material outputItem;
    private int outputAmount;

    public ConverterManager(AutoPickupPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        String inputItemStr = plugin.getConfig().getString("ore-converter.input-item", "DIAMOND");
        String outputItemStr = plugin.getConfig().getString("ore-converter.output-item", "AMETHYST_SHARD");

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

        inputAmount = plugin.getConfig().getInt("ore-converter.input-amount", 64);
        outputAmount = plugin.getConfig().getInt("ore-converter.output-amount", 10);
        
        plugin.getLogger().info("Loaded ore converter config: " + inputAmount + "x " + inputItem.name() + " -> " + outputAmount + "x " + outputItem.name());
    }

    public void saveConfig() {
        plugin.getConfig().set("ore-converter.input-item", inputItem.name());
        plugin.getConfig().set("ore-converter.input-amount", inputAmount);
        plugin.getConfig().set("ore-converter.output-item", outputItem.name());
        plugin.getConfig().set("ore-converter.output-amount", outputAmount);
        plugin.saveConfig();
        plugin.getLogger().info("Saved ore converter config: " + inputAmount + "x " + inputItem.name() + " -> " + outputAmount + "x " + outputItem.name());
    }

    public Material getInputItem() {
        return inputItem;
    }

    public void setInputItem(Material inputItem) {
        this.inputItem = inputItem;
        saveConfig();
    }

    public int getInputAmount() {
        return inputAmount;
    }

    public void setInputAmount(int inputAmount) {
        this.inputAmount = Math.max(1, Math.min(64, inputAmount));
        saveConfig();
    }

    public Material getOutputItem() {
        return outputItem;
    }

    public void setOutputItem(Material outputItem) {
        this.outputItem = outputItem;
        saveConfig();
    }

    public int getOutputAmount() {
        return outputAmount;
    }

    public void setOutputAmount(int outputAmount) {
        this.outputAmount = Math.max(1, Math.min(64, outputAmount));
        saveConfig();
    }

    public boolean canConvert(ItemStack item) {
        return item != null && item.getType() == inputItem && item.getAmount() >= inputAmount;
    }

    public ItemStack convert(ItemStack item) {
        if (canConvert(item)) {
            return new ItemStack(outputItem, outputAmount);
        }
        return null;
    }

    public int getConversionCount(int itemAmount) {
        return itemAmount / inputAmount;
    }

    public int getRemainder(int itemAmount) {
        return itemAmount % inputAmount;
    }
}
