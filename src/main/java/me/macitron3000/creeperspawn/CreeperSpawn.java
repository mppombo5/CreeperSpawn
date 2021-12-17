package me.macitron3000.creeperspawn;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CreeperSpawn extends JavaPlugin {
    // Maintain a list of player UUIDs for which the spawning is currently
    // active, so that multiple texts and creepers don't bog them down
    public List<UUID> playersActive;

    @Override
    public void onEnable() {
        playersActive = new ArrayList<>();

        // Send default config to plugin directory
        saveDefaultConfig();

        // Register listener
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);

        // Make this the last thing you do, everything else should succeed first
        getLogger().info("CreeperSpawn has been enabled!");
    }

    @Override
    public void onDisable() {
        // bye-bye
        getLogger().info("CreeperSpawn has been disabled. Thank you for using!");
    }
}
