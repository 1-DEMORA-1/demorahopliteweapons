package dev.demora.demorahopliteweapons.weapons.voidstaff;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PortalManager {
    
    private final DemoraHopliteWeapons plugin;
    private final Map<UUID, PortalPair> playerPortals;
    private final Map<Location, UUID> portalLocations;
    private final Set<UUID> playersInPortal;
    
    public PortalManager(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.playerPortals = new HashMap<>();
        this.portalLocations = new HashMap<>();
        this.playersInPortal = new HashSet<>();
    }
    
    public boolean hasFirstPortal(Player player) {
        PortalPair pair = playerPortals.get(player.getUniqueId());
        return pair != null && pair.getPortal1() != null;
    }
    
    public boolean createPortal(Player player, Location location) {
        UUID playerId = player.getUniqueId();
        
        PortalPair existingPair = playerPortals.get(playerId);
        
        if (existingPair == null) {
            Location portal1 = placePortalBlock(location);
            if (portal1 != null) {
                PortalPair newPair = new PortalPair(portal1, null, playerId);
                playerPortals.put(playerId, newPair);
                portalLocations.put(portal1, playerId);
                startPortalEffects(portal1);
                return false;
            }
        } else if (existingPair.getPortal2() == null) {
            Location portal2 = placePortalBlock(location);
            if (portal2 != null) {
                existingPair.setPortal2(portal2);
                portalLocations.put(portal2, playerId);
                startPortalEffects(portal2);
                
                schedulePortalRemoval(playerId);
                return true;
            }
        } else {
            player.sendMessage("§cУ вас уже есть активные порталы!");
            return false;
        }
        
        return false;
    }
    
    private Location placePortalBlock(Location location) {
        Block block = location.getBlock();
        if (block.getType() == Material.AIR) {
            block.setType(Material.END_GATEWAY);
            return block.getLocation();
        }
        return null;
    }
    
    private void startPortalEffects(Location location) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (location.getBlock().getType() != Material.END_GATEWAY) {
                    cancel();
                    return;
                }
                
                location.getWorld().spawnParticle(Particle.PORTAL, 
                    location.clone().add(0.5, 0.5, 0.5), 
                    10, 0.3, 0.3, 0.3, 0.1);
                
                location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, 
                    location.clone().add(0.5, 1.5, 0.5), 
                    5, 0.2, 0.2, 0.2, 0.05);
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
    
    private void schedulePortalRemoval(UUID playerId) {
        new BukkitRunnable() {
            @Override
            public void run() {
                removePortals(playerId);
            }
        }.runTaskLater(plugin, 45 * 20L);
    }
    
    public void removePortals(UUID playerId) {
        PortalPair pair = playerPortals.remove(playerId);
        if (pair != null) {
            if (pair.getPortal1() != null) {
                pair.getPortal1().getBlock().setType(Material.AIR);
                portalLocations.remove(pair.getPortal1());
            }
            if (pair.getPortal2() != null) {
                pair.getPortal2().getBlock().setType(Material.AIR);
                portalLocations.remove(pair.getPortal2());
            }
            
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§cПорталы исчезли.");
            }
        }
    }
    
    public void handlePortalTouch(Player player, Location portalLocation) {
        UUID playerId = player.getUniqueId();
        
        if (playersInPortal.contains(playerId)) {
            return;
        }
        
        UUID portalOwner = portalLocations.get(portalLocation);
        if (portalOwner == null) {
            return;
        }
        
        PortalPair pair = playerPortals.get(portalOwner);
        if (pair == null || pair.getPortal2() == null) {
            return;
        }
        
        Location destination;
        if (portalLocation.equals(pair.getPortal1())) {
            destination = pair.getPortal2().clone().add(0.5, 1, 0.5);
        } else if (portalLocation.equals(pair.getPortal2())) {
            destination = pair.getPortal1().clone().add(0.5, 1, 0.5);
        } else {
            return;
        }
        
        playersInPortal.add(playerId);
        
        player.teleport(destination);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        
        player.getWorld().spawnParticle(Particle.PORTAL, 
            player.getLocation(), 
            50, 0.5, 1, 0.5, 0.1);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                playersInPortal.remove(playerId);
            }
        }.runTaskLater(plugin, 20L);
    }
    
    public boolean isPortalBlock(Location location) {
        return portalLocations.containsKey(location);
    }
    
    private static class PortalPair {
        private final Location portal1;
        private Location portal2;
        private final UUID owner;
        
        public PortalPair(Location portal1, Location portal2, UUID owner) {
            this.portal1 = portal1;
            this.portal2 = portal2;
            this.owner = owner;
        }
        
        public Location getPortal1() { return portal1; }
        public Location getPortal2() { return portal2; }
        public void setPortal2(Location portal2) { this.portal2 = portal2; }
        public UUID getOwner() { return owner; }
    }
}
