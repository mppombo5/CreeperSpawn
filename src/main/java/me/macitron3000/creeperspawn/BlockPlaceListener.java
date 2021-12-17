package me.macitron3000.creeperspawn;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class BlockPlaceListener implements Listener {

    // Register the main class as a private field, so we can access config.yml.
    private final CreeperSpawn plugin;

    // Constructor that identifies main plugin class
    public BlockPlaceListener(CreeperSpawn p) {
        this.plugin = p;
    }

    /**
     * General idea for implementation:
     * Around the player, check a 9x9x5 area. For each block in that space,
     * check if a creeper can spawn there (block spawnable, 2 blocks of air
     * above). Add each candidate block to a vector of candidates.
     * Once populated, choose a random block from that vector and spawn a
     * creeper on it. Have fun Thien
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Stop if the player is already being jebaited.
        if (this.plugin.playersActive.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        FileConfiguration config = plugin.getConfig();
        double chance = config.getDouble("spawn-chance");

        // On each block place, use `chance` percent chance of this being called.
        Random rand = new Random();
        if (rand.nextDouble() > chance) {
            return;
        }

        // Retrieve relevant values from config.yml
        List<String> victimList = config.getStringList("victims");
        boolean defaultVictim = config.getBoolean("default-victimize");

        Player p = event.getPlayer();
        String pName = p.getName();
        // When do we know we have nothing to do?
        // - list empty and default-victimize = false
        // - list not empty and player name not in it
        if ( (victimList.isEmpty() && !defaultVictim) ||
             !victimList.contains(pName) ) {
            return;
        }

        UUID playerId = p.getUniqueId();
        BukkitRunnable task = new SpawnRunnable(this.plugin, playerId);
        task.runTaskTimer(this.plugin, 10, 60);
        this.plugin.playersActive.add(playerId);
    }
}
