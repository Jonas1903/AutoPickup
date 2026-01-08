package com.autopickup;

import com.autopickup.commands.AutoPickupCommand;
import com.autopickup.items.OreConverterItem;
import com.autopickup.listeners.BlockBreakListener;
import com.autopickup.listeners.InventoryClickListener;
import com.autopickup.listeners.InventoryConversionListener;
import com.autopickup.listeners.PlayerQuitListener;
import com.autopickup.managers.ConverterManager;
import com.autopickup.managers.PlayerDataManager;
import com.autopickup.managers.SmeltingManager;
import com.autopickup.tasks.AutoConversionTask;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoPickupPlugin extends JavaPlugin {

    private static AutoPickupPlugin instance;
    private PlayerDataManager playerDataManager;
    private SmeltingManager smeltingManager;
    private ConverterManager converterManager;
    private OreConverterItem oreConverterItem;
    private AutoConversionTask autoConversionTask;

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
        getServer().getPluginManager().registerEvents(new InventoryConversionListener(this), this);

        // Register commands
        AutoPickupCommand commandExecutor = new AutoPickupCommand(this);
        getCommand("autopickup").setExecutor(commandExecutor);
        getCommand("autopickup").setTabCompleter(commandExecutor);

        // Start periodic auto-conversion task
        startAutoConversionTask();

        getLogger().info("AutoPickup has been enabled!");
    }

    @Override
    public void onDisable() {
        // Cancel periodic task
        if (autoConversionTask != null) {
            autoConversionTask.cancel();
        }

        // Save player data
        if (playerDataManager != null) {
            playerDataManager.saveAllData();
        }

        getLogger().info("AutoPickup has been disabled!");
    }

    /**
     * Start or restart the auto-conversion task with current config settings.
     */
    private void startAutoConversionTask() {
        if (autoConversionTask != null) {
            autoConversionTask.cancel();
        }
        autoConversionTask = new AutoConversionTask(this);
        autoConversionTask.start();
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
        
        // Restart auto-conversion task with new interval
        startAutoConversionTask();
    }
}
