package dev.demora.demorahopliteweapons.weapons.chainsawsword;

import dev.demora.demorahopliteweapons.DemoraHopliteWeapons;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ChainsawSwordListener implements Listener {
    
    private final DemoraHopliteWeapons plugin;
    private final ChainsawSword chainsawSword;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Set<UUID> bleeding = new HashSet<>();
    
    public ChainsawSwordListener(DemoraHopliteWeapons plugin, ChainsawSword chainsawSword) {
        this.plugin = plugin;
        this.chainsawSword = chainsawSword;
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!chainsawSword.isChainsawSword(item)) {
            return;
        }
        if (chainsawSword.hasCurse(item)) {
            player.sendMessage("§cПроклятие уже готово к удару.");
            return;
        }
        if (isOnCooldown(player)) {
            long remaining = getRemainingCooldown(player);
            player.sendMessage("§cПила остывает. Осталось §e" + remaining + "§c сек.");
            return;
        }
        chainsawSword.setCurse(item, true);
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + getCooldownMillis());
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.6f);
    }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!chainsawSword.isChainsawSword(item)) {
            return;
        }
        if (event.getEntity() instanceof LivingEntity living) {
            double max = living.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null ? living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : living.getHealth();
            if (max > 0 && living.getHealth() / max < 0.2) {
                event.setDamage(event.getDamage() + 1.5);
            }
        }
        if (chainsawSword.hasCurse(item)) {
            chainsawSword.setCurse(item, false);
            event.setDamage(event.getDamage() * 1.5);
            if (event.getEntity() instanceof LivingEntity living) {
                applyBleed(living, player);
            }
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VEX_AMBIENT, 1.0f, 0.8f);
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) {
            return;
        }
        ItemStack item = killer.getInventory().getItemInMainHand();
        if (!chainsawSword.isChainsawSword(item)) {
            return;
        }
        killer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 1, true, true));
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
    
    private boolean isOnCooldown(Player player) {
        Long end = cooldowns.get(player.getUniqueId());
        if (end == null) {
            return false;
        }
        if (System.currentTimeMillis() >= end) {
            cooldowns.remove(player.getUniqueId());
            return false;
        }
        return true;
    }
    
    private long getRemainingCooldown(Player player) {
        Long end = cooldowns.get(player.getUniqueId());
        if (end == null) {
            return 0;
        }
        long diff = end - System.currentTimeMillis();
        if (diff <= 0) {
            cooldowns.remove(player.getUniqueId());
            return 0;
        }
        return (long) Math.ceil(diff / 1000.0);
    }
    
    private void applyBleed(LivingEntity target, Player source) {
        UUID id = target.getUniqueId();
        if (bleeding.contains(id)) {
            return;
        }
        bleeding.add(id);
        int duration = getBleedDurationTicks();
        if (duration <= 0) {
            bleeding.remove(id);
            return;
        }
        double damage = getBleedDamage();
        new BukkitRunnable() {
            private int ticks = 0;
            @Override
            public void run() {
                if (!target.isValid() || target.isDead()) {
                    bleeding.remove(id);
                    cancel();
                    return;
                }
                target.getWorld().spawnParticle(Particle.DUST, target.getLocation().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0, new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 0, 0), 1.2f));
                target.damage(damage, source);
                ticks += 20;
                if (ticks >= duration) {
                    bleeding.remove(id);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private long getCooldownMillis() {
        long seconds = plugin.getConfig().getLong("weapons.chainsaw_sword.curse-cooldown-seconds", 20);
        if (seconds < 0) {
            seconds = 0;
        }
        return seconds * 1000L;
    }
    private int getBleedDurationTicks() {
        int seconds = plugin.getConfig().getInt("weapons.chainsaw_sword.bleed-duration-seconds", 3);
        if (seconds < 0) {
            seconds = 0;
        }
        return seconds * 20;
    }
    private double getBleedDamage() {
        double damage = plugin.getConfig().getDouble("weapons.chainsaw_sword.bleed-damage", 1.0);
        if (damage < 0) {
            damage = 0;
        }
        return damage;
    }
}

