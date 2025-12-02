package com.autopickup.managers;

import org.bukkit.Material;

/**
 * Represents a single conversion recipe for the Ore Converter.
 */
public class ConversionRecipe {

    private Material inputItem;
    private int inputAmount;
    private Material outputItem;
    private int outputAmount;

    public ConversionRecipe(Material inputItem, int inputAmount, Material outputItem, int outputAmount) {
        this.inputItem = inputItem;
        this.inputAmount = Math.max(1, Math.min(64, inputAmount));
        this.outputItem = outputItem;
        this.outputAmount = Math.max(1, Math.min(64, outputAmount));
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
        this.inputAmount = Math.max(1, Math.min(64, inputAmount));
    }

    public Material getOutputItem() {
        return outputItem;
    }

    public void setOutputItem(Material outputItem) {
        this.outputItem = outputItem;
    }

    public int getOutputAmount() {
        return outputAmount;
    }

    public void setOutputAmount(int outputAmount) {
        this.outputAmount = Math.max(1, Math.min(64, outputAmount));
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
        return inputAmount + "x " + inputItem.name() + " -> " + outputAmount + "x " + outputItem.name();
    }
}
