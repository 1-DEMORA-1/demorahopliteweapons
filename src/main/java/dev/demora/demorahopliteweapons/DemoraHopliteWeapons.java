package dev.demora.demorahopliteweapons;

import dev.demora.demorahopliteweapons.commands.DemoraHopCommand;
import dev.demora.demorahopliteweapons.commands.DemoraMenuCommand;
import dev.demora.demorahopliteweapons.commands.DemoraMenuListener;
import dev.demora.demorahopliteweapons.commands.DemoraReworkMenuCommand;
import dev.demora.demorahopliteweapons.weapons.sculkcrossbow.SculkCrossbow;
import dev.demora.demorahopliteweapons.weapons.midassword.*;
import dev.demora.demorahopliteweapons.weapons.mjolnir.*;
import dev.demora.demorahopliteweapons.weapons.shadowblade.*;
import dev.demora.demorahopliteweapons.weapons.reaperscythe.*;
import dev.demora.demorahopliteweapons.weapons.villagerstaff.*;
import dev.demora.demorahopliteweapons.weapons.dragonkatana.*;
import dev.demora.demorahopliteweapons.weapons.dragonkatana.rework.*;
import dev.demora.demorahopliteweapons.weapons.excalibur.*;
import dev.demora.demorahopliteweapons.weapons.voidstaff.*;
import dev.demora.demorahopliteweapons.weapons.magmaclub.*;
import dev.demora.demorahopliteweapons.weapons.shrinkray.*;
import dev.demora.demorahopliteweapons.weapons.chainsawsword.*;
import dev.demora.demorahopliteweapons.weapons.withersickles.*;
import dev.demora.demorahopliteweapons.weapons.withersickles.rework.*;
import dev.demora.demorahopliteweapons.weapons.cloudsword.*;
import dev.demora.demorahopliteweapons.weapons.hypnosisstaff.*;
import dev.demora.demorahopliteweapons.weapons.emeraldblade.*;
import dev.demora.demorahopliteweapons.weapons.golemhammer.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
public class DemoraHopliteWeapons extends JavaPlugin {
    private static DemoraHopliteWeapons instance;
    // Что ты тут забыл?
    private SculkCrossbow sculkCrossbow;
    private MidasSword midasSword;
    private MidasSwordListener midasSwordListener;
    private Mjolnir mjolnir;
    private MjolnirListener mjolnirListener;
    private ShadowBlade shadowBlade;
    private ShadowBladeListener shadowBladeListener;
    private ReaperScythe reaperScythe;
    private ReaperScytheListener reaperScytheListener;
    private VillagerStaff villagerStaff;
    private VillagerStaffListener villagerStaffListener;
    private DragonKatana dragonKatana;
    private DragonKatanaListener dragonKatanaListener;
    private Excalibur excalibur;
    private ExcaliburListener excaliburListener;
    private VoidStaff voidStaff;
    private VoidStaffListener voidStaffListener;
    private AwakenedDragonKatana awakenedDragonKatana;
    private AwakenedDragonKatanaListener awakenedDragonKatanaListener;
    private DragonReworkCommand dragonReworkCommand;
    private MagmaClub magmaClub;
    private MagmaClubListener magmaClubListener;
    private ShrinkRay shrinkRay;
    private ShrinkRayListener shrinkRayListener;
    private ChainsawSword chainsawSword;
    private ChainsawSwordListener chainsawSwordListener;
    private WitherSickles witherSickles;
    private WitherSicklesListener witherSicklesListener;
    private AwakenedWitherSickles awakenedWitherSickles;
    private AwakenedWitherSicklesListener awakenedWitherSicklesListener;
    private SicklesReworkCommand sicklesReworkCommand;
    private CloudSword cloudSword;
    private CloudSwordListener cloudSwordListener;
    private HypnosisStaff hypnosisStaff;
    private HypnosisStaffListener hypnosisStaffListener;
    private EmeraldBlade emeraldBlade;
    private EmeraldBladeListener emeraldBladeListener;
    private GolemHammer golemHammer;
    private GolemHammerListener golemHammerListener;
    private DemoraMenuCommand demoraMenuCommand;
    private DemoraMenuListener demoraMenuListener;
    private DemoraReworkMenuCommand demoraReworkMenuCommand;
    
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        initializeWeapons();
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
    
    private void initializeWeapons() {
        sculkCrossbow = new SculkCrossbow(this);
        midasSword = new MidasSword(this);
        midasSwordListener = new MidasSwordListener(midasSword);
        mjolnir = new Mjolnir(this);
        mjolnirListener = new MjolnirListener(mjolnir, this);
        shadowBlade = new ShadowBlade(this);
        shadowBladeListener = new ShadowBladeListener(shadowBlade);
        reaperScythe = new ReaperScythe(this);
        reaperScytheListener = new ReaperScytheListener(reaperScythe);
        
        villagerStaff = new VillagerStaff(this);
        villagerStaffListener = new VillagerStaffListener(villagerStaff);
        
        dragonKatana = new DragonKatana(this);
        dragonKatanaListener = new DragonKatanaListener(dragonKatana);
        
        excalibur = new Excalibur(this);
        excaliburListener = new ExcaliburListener(excalibur);
        
        voidStaff = new VoidStaff(this);
        voidStaffListener = new VoidStaffListener(voidStaff);
        awakenedDragonKatana = new AwakenedDragonKatana(this);
        awakenedDragonKatanaListener = new AwakenedDragonKatanaListener(this, awakenedDragonKatana);
        dragonReworkCommand = new DragonReworkCommand(this);
        magmaClub = new MagmaClub(this);
        magmaClubListener = new MagmaClubListener(this, magmaClub);
        shrinkRay = new ShrinkRay(this);
        shrinkRayListener = new ShrinkRayListener(this, shrinkRay);
        chainsawSword = new ChainsawSword(this);
        chainsawSwordListener = new ChainsawSwordListener(this, chainsawSword);
        witherSickles = new WitherSickles(this);
        witherSicklesListener = new WitherSicklesListener(this, witherSickles);
        
        awakenedWitherSickles = new AwakenedWitherSickles(this);
        awakenedWitherSicklesListener = new AwakenedWitherSicklesListener(this, awakenedWitherSickles);
        sicklesReworkCommand = new SicklesReworkCommand(this);
        cloudSword = new CloudSword(this);
        cloudSwordListener = new CloudSwordListener(this, cloudSword);
        hypnosisStaff = new HypnosisStaff(this);
        hypnosisStaffListener = new HypnosisStaffListener(this, hypnosisStaff);
        emeraldBlade = new EmeraldBlade(this);
        emeraldBladeListener = new EmeraldBladeListener(this, emeraldBlade);
        
        golemHammer = new GolemHammer(this);
        golemHammerListener = new GolemHammerListener(this, golemHammer);
        
        demoraMenuCommand = new DemoraMenuCommand(this);
        demoraMenuListener = new DemoraMenuListener(this, demoraMenuCommand);
        demoraReworkMenuCommand = new DemoraReworkMenuCommand(this);
    }
    
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(sculkCrossbow, this);
        getServer().getPluginManager().registerEvents(midasSwordListener, this);
        getServer().getPluginManager().registerEvents(mjolnirListener, this);
        getServer().getPluginManager().registerEvents(shadowBladeListener, this);
        getServer().getPluginManager().registerEvents(reaperScytheListener, this);
        getServer().getPluginManager().registerEvents(villagerStaffListener, this);
        getServer().getPluginManager().registerEvents(dragonKatanaListener, this);
        getServer().getPluginManager().registerEvents(excaliburListener, this);
        getServer().getPluginManager().registerEvents(voidStaffListener, this);
        getServer().getPluginManager().registerEvents(awakenedDragonKatanaListener, this);
        getServer().getPluginManager().registerEvents(magmaClubListener, this);
        getServer().getPluginManager().registerEvents(shrinkRayListener, this);
        getServer().getPluginManager().registerEvents(chainsawSwordListener, this);
        getServer().getPluginManager().registerEvents(witherSicklesListener, this);
        getServer().getPluginManager().registerEvents(awakenedWitherSicklesListener, this);
        getServer().getPluginManager().registerEvents(cloudSwordListener, this);
        getServer().getPluginManager().registerEvents(hypnosisStaffListener, this);
        getServer().getPluginManager().registerEvents(emeraldBladeListener, this);
        getServer().getPluginManager().registerEvents(golemHammerListener, this);
        getServer().getPluginManager().registerEvents(demoraMenuListener, this);
    }
    
    private void registerCommands() {
        DemoraHopCommand commandExecutor = new DemoraHopCommand(this);
        getCommand("demorahop").setExecutor(commandExecutor);
        getCommand("demorahop").setTabCompleter(commandExecutor);
        getCommand("dragonrework").setExecutor(dragonReworkCommand);
        getCommand("sicklesrework").setExecutor(sicklesReworkCommand);
        getCommand("demoramenu").setExecutor(demoraMenuCommand);
        getCommand("dm").setExecutor(demoraMenuCommand);
        getCommand("dmr").setExecutor(demoraReworkMenuCommand);
    }
    
    private void startTasks() {
        Bukkit.getScheduler().runTaskTimer(this, shadowBlade::updateTimers, 0L, 1L);
        Bukkit.getScheduler().runTaskTimer(this, dragonKatana::updateSpeedEffects, 0L, 60L);
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
        shadowBlade.reloadConfig();
        awakenedDragonKatana.reloadConfig();
        if (awakenedWitherSicklesListener != null) {
            awakenedWitherSicklesListener.reloadConfig();
        }
        getLogger().info("Конфигурация перезагружена!");
    }
    
    public SculkCrossbow getSculkCrossbow() { return sculkCrossbow; }
    public MidasSword getMidasSword() { return midasSword; }
    public Mjolnir getMjolnir() { return mjolnir; }
    public ShadowBlade getShadowBlade() { return shadowBlade; }
    public ReaperScythe getReaperScythe() { return reaperScythe; }
    public VillagerStaff getVillagerStaff() { return villagerStaff; }
    public DragonKatana getDragonKatana() { return dragonKatana; }
    public Excalibur getExcalibur() { return excalibur; }
    public VoidStaff getVoidStaff() { return voidStaff; }
    public AwakenedDragonKatana getAwakenedDragonKatana() { return awakenedDragonKatana; }
    public MagmaClub getMagmaClub() { return magmaClub; }
    public ShrinkRay getShrinkRay() { return shrinkRay; }
    public ChainsawSword getChainsawSword() { return chainsawSword; }
    public WitherSickles getWitherSickles() { return witherSickles; }
    public AwakenedWitherSickles getAwakenedWitherSickles() { return awakenedWitherSickles; }
    public CloudSword getCloudSword() { return cloudSword; }
    public HypnosisStaff getHypnosisStaff() { return hypnosisStaff; }
    public EmeraldBlade getEmeraldBlade() { return emeraldBlade; }
    public GolemHammer getGolemHammer() { return golemHammer; }
}