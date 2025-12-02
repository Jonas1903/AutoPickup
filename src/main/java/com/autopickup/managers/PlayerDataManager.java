package com.autopickup.managers;

import com.autopickup.AutoPickupPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final AutoPickupPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, PlayerData> playerDataMap;

    public PlayerDataManager(AutoPickupPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.playerDataMap = new HashMap<>();
        loadAllData();
    }

    public void loadAllData() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Load all player data
        if (dataConfig.contains("players")) {
            for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                boolean autoPickup = dataConfig.getBoolean("players." + uuidString + ".auto-pickup", true);
                boolean autoSmelt = dataConfig.getBoolean("players." + uuidString + ".auto-smelt", false);
                playerDataMap.put(uuid, new PlayerData(autoPickup, autoSmelt));
            }
        }
    }

    public void saveAllData() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            String path = "players." + entry.getKey().toString();
            dataConfig.set(path + ".auto-pickup", entry.getValue().isAutoPickupEnabled());
            dataConfig.set(path + ".auto-smelt", entry.getValue().isAutoSmeltEnabled());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml: " + e.getMessage());
        }
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerData(true, false));
    }

    public boolean isAutoPickupEnabled(Player player) {
        return getPlayerData(player).isAutoPickupEnabled();
    }

    public boolean isAutoSmeltEnabled(Player player) {
        return getPlayerData(player).isAutoSmeltEnabled();
    }

    public void setAutoPickup(Player player, boolean enabled) {
        getPlayerData(player).setAutoPickupEnabled(enabled);
        savePlayerData(player.getUniqueId());
    }

    public void setAutoSmelt(Player player, boolean enabled) {
        getPlayerData(player).setAutoSmeltEnabled(enabled);
        savePlayerData(player.getUniqueId());
    }

    public void toggleAutoPickup(Player player) {
        PlayerData data = getPlayerData(player);
        data.setAutoPickupEnabled(!data.isAutoPickupEnabled());
        savePlayerData(player.getUniqueId());
    }

    public void toggleAutoSmelt(Player player) {
        PlayerData data = getPlayerData(player);
        data.setAutoSmeltEnabled(!data.isAutoSmeltEnabled());
        savePlayerData(player.getUniqueId());
    }

    private void savePlayerData(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data != null) {
            String path = "players." + uuid.toString();
            dataConfig.set(path + ".auto-pickup", data.isAutoPickupEnabled());
            dataConfig.set(path + ".auto-smelt", data.isAutoSmeltEnabled());
            try {
                dataConfig.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save playerdata.yml: " + e.getMessage());
            }
        }
    }

    public static class PlayerData {
        private boolean autoPickupEnabled;
        private boolean autoSmeltEnabled;

        public PlayerData(boolean autoPickupEnabled, boolean autoSmeltEnabled) {
            this.autoPickupEnabled = autoPickupEnabled;
            this.autoSmeltEnabled = autoSmeltEnabled;
        }

        public boolean isAutoPickupEnabled() {
            return autoPickupEnabled;
        }

        public void setAutoPickupEnabled(boolean autoPickupEnabled) {
            this.autoPickupEnabled = autoPickupEnabled;
        }

        public boolean isAutoSmeltEnabled() {
            return autoSmeltEnabled;
        }

        public void setAutoSmeltEnabled(boolean autoSmeltEnabled) {
            this.autoSmeltEnabled = autoSmeltEnabled;
        }
    }
}
