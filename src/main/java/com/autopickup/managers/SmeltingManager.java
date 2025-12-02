package com.autopickup.managers;

import com.autopickup.AutoPickupPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmeltingManager {

    private final AutoPickupPlugin plugin;
    private final Map<Material, Material> smeltingRecipes;
    private final Set<Material> enabledItems;

    public SmeltingManager(AutoPickupPlugin plugin) {
        this.plugin = plugin;
        this.smeltingRecipes = new HashMap<>();
        this.enabledItems = new HashSet<>();

        initializeSmeltingRecipes();
        loadConfig();
    }

    private void initializeSmeltingRecipes() {
        // Ore to Ingot/Material mappings
        smeltingRecipes.put(Material.IRON_ORE, Material.IRON_INGOT);
        smeltingRecipes.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        smeltingRecipes.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        smeltingRecipes.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        smeltingRecipes.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        smeltingRecipes.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        smeltingRecipes.put(Material.RAW_IRON, Material.IRON_INGOT);
        smeltingRecipes.put(Material.RAW_GOLD, Material.GOLD_INGOT);
        smeltingRecipes.put(Material.RAW_COPPER, Material.COPPER_INGOT);
        smeltingRecipes.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);

        // Sand to Glass
        smeltingRecipes.put(Material.SAND, Material.GLASS);
        smeltingRecipes.put(Material.RED_SAND, Material.GLASS);

        // Clay
        smeltingRecipes.put(Material.CLAY_BALL, Material.BRICK);
        smeltingRecipes.put(Material.CLAY, Material.TERRACOTTA);

        // Stone
        smeltingRecipes.put(Material.COBBLESTONE, Material.STONE);
        smeltingRecipes.put(Material.STONE, Material.SMOOTH_STONE);
        smeltingRecipes.put(Material.COBBLED_DEEPSLATE, Material.DEEPSLATE);

        // Nether
        smeltingRecipes.put(Material.NETHERRACK, Material.NETHER_BRICK);

        // Wood to Charcoal
        smeltingRecipes.put(Material.OAK_LOG, Material.CHARCOAL);
        smeltingRecipes.put(Material.SPRUCE_LOG, Material.CHARCOAL);
        smeltingRecipes.put(Material.BIRCH_LOG, Material.CHARCOAL);
        smeltingRecipes.put(Material.JUNGLE_LOG, Material.CHARCOAL);
        smeltingRecipes.put(Material.ACACIA_LOG, Material.CHARCOAL);
        smeltingRecipes.put(Material.DARK_OAK_LOG, Material.CHARCOAL);
        smeltingRecipes.put(Material.MANGROVE_LOG, Material.CHARCOAL);
        smeltingRecipes.put(Material.CHERRY_LOG, Material.CHARCOAL);

        // Cactus
        smeltingRecipes.put(Material.CACTUS, Material.GREEN_DYE);

        // Kelp
        smeltingRecipes.put(Material.KELP, Material.DRIED_KELP);

        // Chorus Fruit
        smeltingRecipes.put(Material.CHORUS_FRUIT, Material.POPPED_CHORUS_FRUIT);

        // Wet Sponge
        smeltingRecipes.put(Material.WET_SPONGE, Material.SPONGE);
    }

    public void loadConfig() {
        enabledItems.clear();
        List<String> items = plugin.getConfig().getStringList("auto-smelt.enabled-items");
        for (String item : items) {
            try {
                Material material = Material.valueOf(item.toUpperCase());
                enabledItems.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in config: " + item);
            }
        }
    }

    public void saveConfig() {
        List<String> items = enabledItems.stream()
                .map(Material::name)
                .toList();
        plugin.getConfig().set("auto-smelt.enabled-items", items);
        plugin.saveConfig();
    }

    public boolean canSmelt(Material material) {
        return smeltingRecipes.containsKey(material) && enabledItems.contains(material);
    }

    public Material getSmeltedMaterial(Material material) {
        return smeltingRecipes.get(material);
    }

    public ItemStack getSmeltedItem(ItemStack item) {
        Material smelted = smeltingRecipes.get(item.getType());
        if (smelted != null) {
            return new ItemStack(smelted, item.getAmount());
        }
        return item;
    }

    public boolean isItemEnabled(Material material) {
        return enabledItems.contains(material);
    }

    public void toggleItem(Material material) {
        if (enabledItems.contains(material)) {
            enabledItems.remove(material);
        } else {
            enabledItems.add(material);
        }
        saveConfig();
    }

    public void enableItem(Material material) {
        enabledItems.add(material);
        saveConfig();
    }

    public void disableItem(Material material) {
        enabledItems.remove(material);
        saveConfig();
    }

    public Set<Material> getEnabledItems() {
        return new HashSet<>(enabledItems);
    }

    public Set<Material> getAllSmeltableItems() {
        return new HashSet<>(smeltingRecipes.keySet());
    }

    public Map<Material, Material> getSmeltingRecipes() {
        return new HashMap<>(smeltingRecipes);
    }
}
