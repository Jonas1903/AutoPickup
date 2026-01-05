package com.autopickup.managers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a single conversion recipe for the Ore Converter.
 */
public class ConversionRecipe {

    private Material inputItem;
    private int inputAmount;
    private ItemStack outputItemStack; // Store full ItemStack for custom items
    private int outputAmount;

    public ConversionRecipe(Material inputItem, int inputAmount, ItemStack outputItemStack, int outputAmount) {
        this.inputItem = inputItem;
        this.inputAmount = Math.max(1, inputAmount); // Removed 64 max limit
        if (outputItemStack == null) {
            throw new IllegalArgumentException("outputItemStack cannot be null");
        }
        this.outputItemStack = outputItemStack.clone();
        this.outputItemStack.setAmount(1); // Store as single item
        this.outputAmount = Math.max(1, outputAmount); // Removed 64 max limit
    }

    /**
     * Legacy constructor for backward compatibility - creates ItemStack from Material
     */
    public ConversionRecipe(Material inputItem, int inputAmount, Material outputItem, int outputAmount) {
        this(inputItem, inputAmount, new ItemStack(outputItem), outputAmount);
    }

    public Material getInputItem() {
        return inputItem;
    }

    public void setInputItem(Material inputItem) {
        this.inputItem = inputItem;
    }

    public int getInputAmount() {
        return inputAmount;
    }

    public void setInputAmount(int inputAmount) {
        this.inputAmount = Math.max(1, inputAmount); // Removed 64 max limit
    }

    /**
     * Get the output Material type (for display purposes).
     */
    public Material getOutputItem() {
        return outputItemStack.getType();
    }

    /**
     * Set output item from Material (creates a new basic ItemStack).
     */
    public void setOutputItem(Material outputItem) {
        this.outputItemStack = new ItemStack(outputItem);
    }

    /**
     * Get the full output ItemStack (includes custom name, lore, enchants, etc.).
     */
    public ItemStack getOutputItemStack() {
        return outputItemStack.clone();
    }

    /**
     * Set the full output ItemStack (for custom items).
     */
    public void setOutputItemStack(ItemStack itemStack) {
        if (itemStack == null) {
            throw new IllegalArgumentException("itemStack cannot be null");
        }
        this.outputItemStack = itemStack.clone();
        this.outputItemStack.setAmount(1); // Store as single item
    }

    public int getOutputAmount() {
        return outputAmount;
    }

    public void setOutputAmount(int outputAmount) {
        this.outputAmount = Math.max(1, outputAmount); // Removed 64 max limit
    }

    /**
     * Calculate the number of conversions possible for a given item amount.
     */
    public int getConversionCount(int itemAmount) {
        return itemAmount / inputAmount;
    }

    /**
     * Calculate the remainder after conversion.
     */
    public int getRemainder(int itemAmount) {
        return itemAmount % inputAmount;
    }

    @Override
    public String toString() {
        return inputAmount + "x " + inputItem.name() + " -> " + outputAmount + "x " + outputItemStack.getType().name();
    }
}
