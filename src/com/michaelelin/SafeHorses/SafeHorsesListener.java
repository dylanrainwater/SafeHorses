package com.michaelelin.SafeHorses;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class SafeHorsesListener implements Listener {

    private SafeHorsesPlugin plugin;

    public SafeHorsesListener(SafeHorsesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.HORSE && plugin.horseRegistry.isSafeHorse((Horse) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.HORSE && plugin.horseRegistry.isSafeHorse((Horse) event.getRightClicked())) {
            if (plugin.horseRegistry.getSafeHorse(event.getPlayer()) != event.getRightClicked()) {
                if (event.getPlayer().hasPermission("safehorses.info")) {
                    plugin.message(event.getPlayer(), "This horse belongs to " + ChatColor.LIGHT_PURPLE + ((Horse) event.getRightClicked()).getOwner().getName() + ChatColor.GREEN + ".");
                }
                else {
                    plugin.message(event.getPlayer(), "That horse isn't yours!");
                }
                event.setCancelled(true);
            }
            else if (event.getPlayer().getItemInHand().getType() == Material.NAME_TAG) {
                if (!event.getPlayer().hasPermission("safehorses.rename")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.horseRegistry.hasSafeHorse(event.getPlayer())) {
            plugin.horseRegistry.removeSafeHorse(event.getPlayer(), false);
        }
    }
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity e : event.getChunk().getEntities()) {
            if (e.getType() == EntityType.HORSE) {
                // Clean up "stray" horses that were created by the plugin
                // but somehow weren't removed
                Horse horse = (Horse) e;
                if (horse.getMaxHealth() == 1 && !plugin.horseRegistry.isSafeHorse(horse)) {
                    horse.remove();
                }
            }
        }
    }
    
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity e : event.getChunk().getEntities()) {
            if (e.getType() == EntityType.HORSE) {
                Horse horse = (Horse) e;
                if (plugin.horseRegistry.isSafeHorse(horse)) {
                    plugin.horseRegistry.removeSafeHorse((Player) horse.getOwner(), false);
                }
            }
        }
    }
    
}
