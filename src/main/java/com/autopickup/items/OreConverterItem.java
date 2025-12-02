package com.autopickup.items;

import com.autopickup.AutoPickupPlugin;
import com.autopickup.managers.ConversionRecipe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class OreConverterItem {

    private final AutoPickupPlugin plugin;
    private final NamespacedKey converterKey;

    public OreConverterItem(AutoPickupPlugin plugin) {
        this.plugin = plugin;
        this.converterKey = new NamespacedKey(plugin, "ore_converter");
    }

    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Set display name
            meta.displayName(Component.text("Ore Converter", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));

            // Set lore
            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Hold in your offhand while mining", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("to convert ores automatically!", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            
            // Show all conversion recipes
            List<ConversionRecipe> recipes = plugin.getConverterManager().getRecipes();
            if (recipes.isEmpty()) {
                lore.add(Component.text("No recipes configured.", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false));
            } else {
                lore.add(Component.text("Active Conversions:", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
                
                // Show up to 5 recipes in lore to keep it readable
                int displayCount = Math.min(recipes.size(), 5);
                for (int i = 0; i < displayCount; i++) {
                    ConversionRecipe recipe = recipes.get(i);
                    lore.add(getConversionLore(recipe));
                }
                
                if (recipes.size() > 5) {
                    lore.add(Component.text("  ... and " + (recipes.size() - 5) + " more", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, true));
                }
            }
            
            lore.add(Component.empty());
            lore.add(Component.text("✦ Magical Item ✦", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);

            // Mark as converter item using PersistentDataContainer
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(converterKey, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }

        return item;
    }

    private Component getConversionLore(ConversionRecipe recipe) {
        return Component.text("  " + recipe.getInputAmount() + "x ", NamedTextColor.WHITE)
                .append(Component.text(formatMaterialName(recipe.getInputItem()), NamedTextColor.AQUA))
                .append(Component.text(" → ", NamedTextColor.GRAY))
                .append(Component.text(recipe.getOutputAmount() + "x ", NamedTextColor.WHITE))
                .append(Component.text(formatMaterialName(recipe.getOutputItem()), NamedTextColor.GREEN))
                .decoration(TextDecoration.ITALIC, false);
    }

    public boolean isConverterItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(converterKey, PersistentDataType.BYTE);
    }

    public NamespacedKey getConverterKey() {
        return converterKey;
    }

    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : name.toCharArray()) {
            if (c == ' ') {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
