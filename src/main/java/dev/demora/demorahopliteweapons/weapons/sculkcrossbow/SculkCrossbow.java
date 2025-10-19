package dev.demora.demorahopliteweapons.weapons.sculkcrossbow;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SculkCrossbow implements Listener {
    
    private final DemoraHopliteWeapons plugin;
    private final NamespacedKey sculkCrossbowKey;
    private final NamespacedKey sculkArrowKey;
    private final Map<UUID, Long> cooldowns;
    private final Set<UUID> sculkArrows;
    private final Set<Material> replaceableBlocks;
    private final List<Material> sculkBlocks;
    private final Random random;
    
    public SculkCrossbow(DemoraHopliteWeapons plugin) {
        this.plugin = plugin;
        this.sculkCrossbowKey = new NamespacedKey(plugin, "sculk_crossbow");
        this.sculkArrowKey = new NamespacedKey(plugin, "sculk_arrow");
        this.cooldowns = new HashMap<>();
        this.sculkArrows = new HashSet<>();
        this.replaceableBlocks = new HashSet<>();
        this.sculkBlocks = new ArrayList<>();
        this.random = new Random();
        
        initReplaceableBlocks();
        initSculkBlocks();
    }
    
    private void initReplaceableBlocks() {
        replaceableBlocks.addAll(Arrays.asList(
            Material.STONE, Material.COBBLESTONE, Material.DIRT, Material.GRASS_BLOCK,
            Material.SAND, Material.GRAVEL, Material.ANDESITE, Material.DIORITE,
            Material.GRANITE, Material.DEEPSLATE, Material.COBBLED_DEEPSLATE,
            Material.TUFF, Material.CALCITE, Material.DRIPSTONE_BLOCK,
            Material.SMOOTH_BASALT, Material.CLAY, Material.TERRACOTTA,
            Material.NETHERRACK, Material.BLACKSTONE, Material.BASALT
        ));
    }
    
    private void initSculkBlocks() {
        sculkBlocks.add(Material.SCULK);
        sculkBlocks.add(Material.SCULK_VEIN);
    }
    
    public ItemStack createSculkCrossbow() {
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        ItemMeta meta = crossbow.getItemMeta();
        
        if (meta != null) {
            meta.displayName(Component.text("Скалковый Арбалет", NamedTextColor.DARK_AQUA, TextDecoration.BOLD));
            meta.setCustomModelData(1);
            
            meta.addEnchant(Enchantment.QUICK_CHARGE, 3, true);
            
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setUnbreakable(true);
            
            meta.getPersistentDataContainer().set(sculkCrossbowKey, PersistentDataType.BYTE, (byte) 1);
            
            crossbow.setItemMeta(meta);
        }
        
        return crossbow;
    }
    
    public boolean isSculkCrossbow(ItemStack item) {
        if (item == null || item.getType() != Material.CROSSBOW) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        return meta.getPersistentDataContainer().has(sculkCrossbowKey, PersistentDataType.BYTE);
    }
    
    public boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        
        long cooldownTime = plugin.getConfig().getInt("weapons.sculk_crossbow.cooldown") * 1000L;
        long lastUse = cooldowns.get(playerId);
        return System.currentTimeMillis() - lastUse < cooldownTime;
    }
    
    public int getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long cooldownTime = plugin.getConfig().getInt("weapons.sculk_crossbow.cooldown") * 1000L;
        long lastUse = cooldowns.get(playerId);
        long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
        
        return remaining > 0 ? (int) (remaining / 1000) + 1 : 0;
    }
    
    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public void handleSculkArrowShoot(Arrow arrow, Player shooter) {
        arrow.setVisualFire(false);
        arrow.setGlowing(false);
        arrow.setSilent(true);
        
        arrow.getPersistentDataContainer().set(sculkArrowKey, PersistentDataType.STRING, shooter.getUniqueId().toString());
        sculkArrows.add(arrow.getUniqueId());
        
        Location shooterLoc = shooter.getLocation();
        if (shooterLoc.getWorld() != null) {
            shooterLoc.getWorld().playSound(shooterLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.8f);
        }
        
        startParticleEffect(arrow);
    }
    
    private void startParticleEffect(Arrow arrow) {
        if (!plugin.getConfig().getBoolean("weapons.sculk_crossbow.particles.warden-particles")) {
            return;
        }
        
        int particlesPerTick = plugin.getConfig().getInt("weapons.sculk_crossbow.particles.particles-per-tick");
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arrow.isDead() || !arrow.isValid() || arrow.isOnGround()) {
                    cancel();
                    return;
                }
                
                Location loc = arrow.getLocation();
                
                if (loc.getWorld() != null) {
                    loc.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 1, 0, 0, 0, 0);
                    loc.getWorld().spawnParticle(Particle.SCULK_SOUL, loc, particlesPerTick, 0.2, 0.2, 0.2, 0.02);
                    loc.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP, loc, particlesPerTick / 2, 0.1, 0.1, 0.1, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    public boolean isSculkArrow(Arrow arrow) {
        return sculkArrows.contains(arrow.getUniqueId()) && 
               arrow.getPersistentDataContainer().has(sculkArrowKey, PersistentDataType.STRING);
    }
    
    public Player getShooter(Arrow arrow) {
        String shooterUuid = arrow.getPersistentDataContainer().get(sculkArrowKey, PersistentDataType.STRING);
        if (shooterUuid == null) {
            return null;
        }
        
        try {
            UUID uuid = UUID.fromString(shooterUuid);
            return Bukkit.getPlayer(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public void removeSculkArrow(UUID arrowId) {
        sculkArrows.remove(arrowId);
    }
    
    public void handleExplosionAndSpread(Location location, Player shooter) {
        createExplosion(location);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                startSculkSpread(location);
            }
        }.runTaskLater(plugin, 20L);
    }
    
    private void createExplosion(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        
        float power = (float) plugin.getConfig().getDouble("weapons.sculk_crossbow.explosion.power");
        boolean breakBlocks = plugin.getConfig().getBoolean("weapons.sculk_crossbow.explosion.break-blocks");
        boolean setFire = plugin.getConfig().getBoolean("weapons.sculk_crossbow.explosion.set-fire");
        
        world.createExplosion(location, power, setFire, breakBlocks);
        
        world.spawnParticle(Particle.SONIC_BOOM, location, 3, 1, 1, 1, 0);
        world.spawnParticle(Particle.SCULK_SOUL, location, 20, 2, 2, 2, 0.1);
        world.playSound(location, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.8f);
        world.playSound(location, Sound.BLOCK_SCULK_CATALYST_BLOOM, 1.0f, 1.0f);
    }
    
    private void startSculkSpread(Location centerLocation) {
        int radius = plugin.getConfig().getInt("weapons.sculk_crossbow.sculk-spread.radius");
        int blocksPerTick = plugin.getConfig().getInt("weapons.sculk_crossbow.sculk-spread.blocks-per-tick");
        int tickDelay = plugin.getConfig().getInt("weapons.sculk_crossbow.sculk-spread.tick-delay");
        double replaceChance = plugin.getConfig().getDouble("weapons.sculk_crossbow.sculk-spread.replace-chance");
        
        List<Block> blocksToProcess = getBlocksInRadius(centerLocation, radius);
        Collections.shuffle(blocksToProcess);
        
        new BukkitRunnable() {
            private int processedBlocks = 0;
            
            @Override
            public void run() {
                int processed = 0;
                
                while (processed < blocksPerTick && processedBlocks < blocksToProcess.size()) {
                    Block block = blocksToProcess.get(processedBlocks);
                    processedBlocks++;
                    
                    if (shouldReplaceBlock(block, replaceChance)) {
                        replaceBlockWithSculk(block);
                        processed++;
                    }
                }
                
                if (processedBlocks >= blocksToProcess.size()) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, tickDelay);
    }
    
    private List<Block> getBlocksInRadius(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();
        if (world == null) return blocks;
        
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = Math.max(world.getMinHeight(), centerY - radius); 
                 y <= Math.min(world.getMaxHeight() - 1, centerY + radius); y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    
                    double distance = Math.sqrt(
                        Math.pow(x - centerX, 2) + 
                        Math.pow(y - centerY, 2) + 
                        Math.pow(z - centerZ, 2)
                    );
                    
                    if (distance <= radius) {
                        blocks.add(world.getBlockAt(x, y, z));
                    }
                }
            }
        }
        
        return blocks;
    }
    
    private boolean shouldReplaceBlock(Block block, double chance) {
        if (!replaceableBlocks.contains(block.getType())) {
            return false;
        }
        
        return random.nextDouble() < chance;
    }
    
    private void replaceBlockWithSculk(Block block) {
        Material sculkType = getRandomSculkBlock(block);
        block.setType(sculkType);
        
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        World world = loc.getWorld();
        
        if (world != null) {
            world.spawnParticle(Particle.SCULK_CHARGE_POP, loc, 5, 0.3, 0.3, 0.3, 0);
            world.playSound(loc, Sound.BLOCK_SCULK_PLACE, 0.5f, 1.0f + (random.nextFloat() * 0.4f - 0.2f));
        }
    }
    
    private Material getRandomSculkBlock(Block block) {
        boolean isSurface = block.getRelative(0, 1, 0).getType() == Material.AIR;
        
        int rand = random.nextInt(100);
        
        if (isSurface) {
            if (rand < 70) {
                return Material.SCULK;
            } else {
                return Material.SCULK_VEIN;
            }
        } else {
            return Material.SCULK;
        }
    }
    
    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();
        
        if (!isSculkCrossbow(bow)) {
            return;
        }
        
        if (isOnCooldown(player)) {
            int remaining = getRemainingCooldown(player);
            player.sendMessage("§cСкалковый арбалет перезаряжается! Осталось: §e" + remaining + " сек");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            event.setCancelled(true);
            return;
        }
        
        setCooldown(player);
        
        if (event.getProjectile() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getProjectile();
            handleSculkArrowShoot(arrow, player);
        }
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }
        
        Arrow arrow = (Arrow) event.getEntity();
        
        if (!isSculkArrow(arrow)) {
            return;
        }
        
        removeSculkArrow(arrow.getUniqueId());
        
        Player shooter = getShooter(arrow);
        
        arrow.remove();
        
        handleExplosionAndSpread(arrow.getLocation(), shooter);
    }
}
