package dev.demora.demorahopliteweapons;

import dev.demora.demorahopliteweapons.commands.DemoraHopCommand;
import dev.demora.demorahopliteweapons.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class DemoraHopliteWeapons extends JavaPlugin {
    
    private static DemoraHopliteWeapons instance;
    
    private SculkCrossbowManager sculkCrossbowManager;
    private MidasSwordManager midasSwordManager;
    private MjolnirManager mjolnirManager;
    private ShadowBladeManager shadowBladeManager;
    private ReaperScytheManager reaperScytheManager;
    private VillagerStaffManager villagerStaffManager;
    private DragonKatanaManager dragonKatanaManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        initializeManagers();
        registerEvents();
        registerCommands();
        startTasks();
        
        getLogger().info("DemoraHopliteWeapons v" + getDescription().getVersion() + " успешно загружен!");
        getLogger().info("Автор: __DEMORA__");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("DemoraHopliteWeapons отключен!");
    }
    
    private void initializeManagers() {
        sculkCrossbowManager = new SculkCrossbowManager(this);
        midasSwordManager = new MidasSwordManager(this);
        mjolnirManager = new MjolnirManager(this);
        shadowBladeManager = new ShadowBladeManager(this);
        reaperScytheManager = new ReaperScytheManager(this);
        villagerStaffManager = new VillagerStaffManager(this);
        dragonKatanaManager = new DragonKatanaManager(this);
    }
    
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(sculkCrossbowManager, this);
        getServer().getPluginManager().registerEvents(midasSwordManager, this);
        getServer().getPluginManager().registerEvents(mjolnirManager, this);
        getServer().getPluginManager().registerEvents(shadowBladeManager, this);
        getServer().getPluginManager().registerEvents(reaperScytheManager, this);
        getServer().getPluginManager().registerEvents(villagerStaffManager, this);
        getServer().getPluginManager().registerEvents(dragonKatanaManager, this);
    }
    
    private void registerCommands() {
        DemoraHopCommand commandExecutor = new DemoraHopCommand(this);
        getCommand("demorahop").setExecutor(commandExecutor);
        getCommand("demorahop").setTabCompleter(commandExecutor);
    }
    
    private void startTasks() {
        Bukkit.getScheduler().runTaskTimer(this, shadowBladeManager::updateTimers, 0L, 1L);
        Bukkit.getScheduler().runTaskTimer(this, dragonKatanaManager::updateSpeedEffects, 0L, 60L);
    }
    
    public static DemoraHopliteWeapons getInstance() {
        return instance;
    }
    
    public String getMessage(String path) {
        String message = getConfig().getString("messages." + path, "");
        return message.replace('&', '§');
    }
    
    public String getMessageWithPrefix(String path) {
        return getMessage("prefix") + getMessage(path);
    }
    
    public void reloadPluginConfig() {
        reloadConfig();
        shadowBladeManager.reloadConfig();
        getLogger().info("Конфигурация перезагружена!");
    }
    
    public SculkCrossbowManager getSculkCrossbowManager() { return sculkCrossbowManager; }
    public MidasSwordManager getMidasSwordManager() { return midasSwordManager; }
    public MjolnirManager getMjolnirManager() { return mjolnirManager; }
    public ShadowBladeManager getShadowBladeManager() { return shadowBladeManager; }
    public ReaperScytheManager getReaperScytheManager() { return reaperScytheManager; }
    public VillagerStaffManager getVillagerStaffManager() { return villagerStaffManager; }
    public DragonKatanaManager getDragonKatanaManager() { return dragonKatanaManager; }
}
