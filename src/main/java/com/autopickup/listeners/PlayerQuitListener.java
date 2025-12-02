package com.autopickup.listeners;

import com.autopickup.AutoPickupPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener to clean up player data when they leave the server.
 */
public class PlayerQuitListener implements Listener {

    private final AutoPickupPlugin plugin;

    public PlayerQuitListener(AutoPickupPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clear accumulator data when player logs out
        plugin.getConverterManager().clearAccumulators(event.getPlayer().getUniqueId());
    }
}
