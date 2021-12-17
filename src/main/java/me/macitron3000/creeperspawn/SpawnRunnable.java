package me.macitron3000.creeperspawn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.UUID;

public class SpawnRunnable extends BukkitRunnable {
    private final CreeperSpawn plugin;
    private final UUID playerId;
    private Stage curStage;

    // Stages of the sequence are:
    // "You sure like building, huh?" - START
    // "Maybe you should build yourself some" - ST1
    // "~~bicthes~~" - ST2
    // "You've met with a terrible fate, haven't you?" - ST3
    // "Keep your wits about you." - ST4
    // spawn creeper - END
    private enum Stage {
        START, ST1, ST2, ST3, ST4, END
    }

    public SpawnRunnable(CreeperSpawn plugin, UUID playerId) {
        this.plugin = plugin;
        this.playerId = playerId;
        this.curStage = Stage.START;
    }

    @Override
    public void run() {
        Player player = this.plugin.getServer().getPlayer(playerId);

        // Quick check to make sure the player is still online, since this
        // happens over the course of like 10 seconds
        if (player == null) {
            this.cancel();
            this.plugin.playersActive.remove(this.playerId);
            return;
        }

        // Each switch case represents a different stage,
        // which is updated as we go.
        switch (curStage) {
          case START:
            player.sendMessage("" + ChatColor.GRAY + ChatColor.ITALIC + "You sure like building, huh?");
            this.curStage = Stage.ST1;
            break;
          case ST1:
            player.sendMessage("" + ChatColor.GRAY + ChatColor.ITALIC + "Maybe you should build yourself some");
            this.curStage = Stage.ST2;
            break;
          case ST2:
            player.sendMessage("" + ChatColor.AQUA + ChatColor.ITALIC + "~~bicthes~~");
            this.curStage = Stage.ST3;
            break;
          case ST3:
            player.sendMessage("" + ChatColor.RED + ChatColor.ITALIC + "You've met with a terrible fate.");
            this.curStage = Stage.ST4;
            break;
          case ST4:
            player.sendMessage("" + ChatColor.DARK_RED + ChatColor.ITALIC + "Keep your wits about you.");
            player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, (float) 0.5, 1);
            this.curStage = Stage.END;
            break;
          case END:
            Location loc = player.getLocation();
            int locX = loc.getBlockX();
            int locY = loc.getBlockY();
            int locZ = loc.getBlockZ();
            java.util.Vector<Location> candidates = new java.util.Vector<>(50);

            // Now we iterate through the 9x9x5 area. For each axis, iterate
            // through -2 to +2. Do y last, so we can skip over blocks if we
            // find air spaces.
            int horizRadius = this.plugin.getConfig().getInt("horizontal-radius");
            int vertRadius = this.plugin.getConfig().getInt("vertical-radius");
            for (int i = -horizRadius; i <= horizRadius; i++) {             // x offset
                for (int k = -horizRadius; k <= horizRadius; k++) {         // z offset
                    for (int j = -vertRadius; j <= vertRadius; j++) {     // y offset
                        // Work with the block at i,j,k offsets.
                        Location curLoc = new Location(
                                player.getWorld(),
                                locX + i,
                                locY + j,
                                locZ + k
                        );

                        // If this block is empty, we can't do anything with it.
                        if (curLoc.getBlock().isPassable()) continue;

                        // Otherwise, naively assume we can spawn something on it.
                        // Check the two blocks above for emptiness
                        int curX = curLoc.getBlockX();
                        int curY = curLoc.getBlockY();
                        int curZ = curLoc.getBlockZ();
                        Location oneUp = new Location(
                                player.getWorld(),
                                curX,
                                curY + 1,
                                curZ
                        );
                        Location twoUp = new Location(
                                player.getWorld(),
                                curX,
                                curY + 2,
                                curZ
                        );
                        int toAdd = 0;
                        if (oneUp.getBlock().isPassable()) {
                            toAdd++;
                            if(twoUp.getBlock().isPassable()) {
                                toAdd++;
                                candidates.add(oneUp);
                            }
                        }
                        // toAdd holds the number of blocks above curLoc which are
                        // empty, so we know we can skip them.
                        j += toAdd;
                    }
                }
            }

            // At the end of it all, `candidates` has every block we are able to spawn a creeper on.
            // Unless it's empty, then this was all for naught.
            if (candidates.isEmpty()) return;

            int amt;
            double n = new Random().nextDouble();

            // Why not go with 50% 1 creeper, 30% 2, 20% 3?
            // TODO: make this configurable
            if (n < 0.5) {
                amt = 1;
            } else if (n < 0.8) {
                amt = 2;
            } else {
                amt = 3;
            }

            for (int i = 0; i < amt; i++) {
                int idx = new Random().nextInt(candidates.size());
                Location spawnLoc = candidates.get(idx);

                Creeper creeper = (Creeper) player.getWorld().spawnEntity(spawnLoc, EntityType.CREEPER);
                creeper.setGlowing(true);
                creeper.setPowered(true);
            }

            this.cancel();
            this.plugin.playersActive.remove(playerId);
            break;
        }
    }
}
