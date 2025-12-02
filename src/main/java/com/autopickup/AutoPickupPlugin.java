package com.autopickup;

import com.autopickup.commands.AutoPickupCommand;
import com.autopickup.items.OreConverterItem;
import com.autopickup.listeners.BlockBreakListener;
import com.autopickup.listeners.InventoryClickListener;
import com.autopickup.listeners.PlayerQuitListener;
import com.autopickup.managers.ConverterManager;
import com.autopickup.managers.PlayerDataManager;
import com.autopickup.managers.SmeltingManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoPickupPlugin extends JavaPlugin {

    private static AutoPickupPlugin instance;
    private PlayerDataManager playerDataManager;
    private SmeltingManager smeltingManager;
    private ConverterManager converterManager;
    private OreConverterItem oreConverterItem;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize managers
        playerDataManager = new PlayerDataManager(this);
        smeltingManager = new SmeltingManager(this);
        converterManager = new ConverterManager(this);
        oreConverterItem = new OreConverterItem(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        // Register commands
        AutoPickupCommand commandExecutor = new AutoPickupCommand(this);
        getCommand("autopickup").setExecutor(commandExecutor);
        getCommand("autopickup").setTabCompleter(commandExecutor);

        getLogger().info("AutoPickup has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save player data
        if (playerDataManager != null) {
            playerDataManager.saveAllData();
        }

        getLogger().info("AutoPickup has been disabled!");
    }

    public static AutoPickupPlugin getInstance() {
        return instance;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public SmeltingManager getSmeltingManager() {
        return smeltingManager;
    }

    public ConverterManager getConverterManager() {
        return converterManager;
    }

    public OreConverterItem getOreConverterItem() {
        return oreConverterItem;
    }

    public void reloadPlugin() {
        reloadConfig();
        smeltingManager.loadConfig();
        converterManager.loadConfig();
        playerDataManager.reloadData();
    }
}
