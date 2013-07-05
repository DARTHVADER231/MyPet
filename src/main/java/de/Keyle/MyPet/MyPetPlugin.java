/*
 * This file is part of MyPet
 *
 * Copyright (C) 2011-2013 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet;

import de.Keyle.MyPet.chatcommands.*;
import de.Keyle.MyPet.chatcommands.CommandHelp;
import de.Keyle.MyPet.chatcommands.CommandStop;
import de.Keyle.MyPet.entity.types.InactiveMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPet.PetState;
import de.Keyle.MyPet.entity.types.MyPetList;
import de.Keyle.MyPet.entity.types.MyPetType;
import de.Keyle.MyPet.entity.types.bat.EntityMyBat;
import de.Keyle.MyPet.entity.types.blaze.EntityMyBlaze;
import de.Keyle.MyPet.entity.types.cavespider.EntityMyCaveSpider;
import de.Keyle.MyPet.entity.types.chicken.EntityMyChicken;
import de.Keyle.MyPet.entity.types.cow.EntityMyCow;
import de.Keyle.MyPet.entity.types.creeper.EntityMyCreeper;
import de.Keyle.MyPet.entity.types.enderman.EntityMyEnderman;
import de.Keyle.MyPet.entity.types.ghast.EntityMyGhast;
import de.Keyle.MyPet.entity.types.giant.EntityMyGiant;
import de.Keyle.MyPet.entity.types.horse.EntityMyHorse;
import de.Keyle.MyPet.entity.types.irongolem.EntityMyIronGolem;
import de.Keyle.MyPet.entity.types.magmacube.EntityMyMagmaCube;
import de.Keyle.MyPet.entity.types.mooshroom.EntityMyMooshroom;
import de.Keyle.MyPet.entity.types.ocelot.EntityMyOcelot;
import de.Keyle.MyPet.entity.types.pig.EntityMyPig;
import de.Keyle.MyPet.entity.types.pigzombie.EntityMyPigZombie;
import de.Keyle.MyPet.entity.types.sheep.EntityMySheep;
import de.Keyle.MyPet.entity.types.silverfish.EntityMySilverfish;
import de.Keyle.MyPet.entity.types.skeleton.EntityMySkeleton;
import de.Keyle.MyPet.entity.types.slime.EntityMySlime;
import de.Keyle.MyPet.entity.types.snowman.EntityMySnowman;
import de.Keyle.MyPet.entity.types.spider.EntityMySpider;
import de.Keyle.MyPet.entity.types.villager.EntityMyVillager;
import de.Keyle.MyPet.entity.types.witch.EntityMyWitch;
import de.Keyle.MyPet.entity.types.wither.EntityMyWither;
import de.Keyle.MyPet.entity.types.wolf.EntityMyWolf;
import de.Keyle.MyPet.entity.types.zombie.EntityMyZombie;
import de.Keyle.MyPet.listeners.*;
import de.Keyle.MyPet.skill.MyPetMonsterExperience;
import de.Keyle.MyPet.skill.MyPetSkillTreeMobType;
import de.Keyle.MyPet.skill.MyPetSkills;
import de.Keyle.MyPet.skill.MyPetSkillsInfo;
import de.Keyle.MyPet.skill.skills.implementation.*;
import de.Keyle.MyPet.skill.skills.info.*;
import de.Keyle.MyPet.skill.skilltreeloader.MyPetSkillTreeLoader;
import de.Keyle.MyPet.skill.skilltreeloader.MyPetSkillTreeLoaderJSON;
import de.Keyle.MyPet.skill.skilltreeloader.MyPetSkillTreeLoaderNBT;
import de.Keyle.MyPet.skill.skilltreeloader.MyPetSkillTreeLoaderYAML;
import de.Keyle.MyPet.util.*;
import de.Keyle.MyPet.util.configuration.NBT_Configuration;
import de.Keyle.MyPet.util.configuration.YAML_Configuration;
import de.Keyle.MyPet.util.locale.MyPetLocales;
import de.Keyle.MyPet.util.logger.DebugLogger;
import de.Keyle.MyPet.util.logger.MyPetLogger;
import de.Keyle.MyPet.util.support.*;
import de.Keyle.nbt.ByteTag;
import de.Keyle.nbt.CompoundTag;
import de.Keyle.nbt.ListTag;
import de.Keyle.nbt.StringTag;
import net.minecraft.server.v1_6_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_6_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

public class MyPetPlugin extends JavaPlugin implements IScheduler
{
    private static MyPetPlugin plugin;
    private File NBTPetFile;
    private boolean isReady = false;
    private int autoSaveTimer = 0;

    public static MyPetPlugin getPlugin()
    {
        return plugin;
    }

    public void onDisable()
    {
        if (isReady)
        {
            int petCount = savePets(true);
            MyPetLogger.write("" + ChatColor.YELLOW + petCount + ChatColor.RESET + " pet(s) saved");
            for (MyPet myPet : MyPetList.getAllActiveMyPets())
            {
                myPet.removePet(myPet.wantToRespawn());
            }
            MyPetList.clearList();
        }
        MyPetTimer.reset();
        MyPetLogger.setConsole(null);
        Bukkit.getServer().getScheduler().cancelTasks(getPlugin());
        DebugLogger.info("MyPet disabled!");
    }

    public void onEnable()
    {
        plugin = this;
        this.isReady = false;
        new File(getPlugin().getDataFolder().getAbsolutePath() + File.separator + "skilltrees" + File.separator).mkdirs();
        new File(getPlugin().getDataFolder().getAbsolutePath() + File.separator + "backups" + File.separator).mkdirs();
        new File(getPlugin().getDataFolder().getAbsolutePath() + File.separator + "locale" + File.separator).mkdirs();
        new File(getPlugin().getDataFolder().getAbsolutePath() + File.separator + "logs" + File.separator).mkdirs();
        NBTPetFile = new File(getPlugin().getDataFolder().getPath() + File.separator + "My.Pets");

        MyPetVersion.reset();
        MyPetLogger.setConsole(getServer().getConsoleSender());
        MyPetPvP.reset();
        MyPetEconomy.reset();
        MyPetConfiguration.config = this.getConfig();
        MyPetConfiguration.setDefault();
        MyPetConfiguration.loadConfiguration();

        if(MyPetConfiguration.USE_MYSQL)
        {
            MyPetMySQL.setupMySQL();
        }

        DebugLogger.setup(MyPetConfiguration.USE_DEBUG_LOGGER);

        String minecraftVersion = ((CraftServer) getServer()).getHandle().getServer().getVersion();

        if (!MyPetVersion.getMinecraftVersion().equalsIgnoreCase(minecraftVersion))
        {
            MyPetLogger.write(ChatColor.RED + "---------------------------------------------------------");
            MyPetLogger.write(ChatColor.RED + "This version of MyPet only works with:");
            MyPetLogger.write(ChatColor.RED + "   Minecraft " + MyPetVersion.getMinecraftVersion());
            MyPetLogger.write(ChatColor.RED + "MyPet disabled!");
            MyPetLogger.write(ChatColor.RED + "---------------------------------------------------------");
            this.setEnabled(false);
            return;
        }

        DebugLogger.info("----------- loading MyPet ... -----------");
        DebugLogger.info("MyPet " + MyPetVersion.getMyPetVersion() + " build: " + MyPetVersion.getMyPetBuild());
        DebugLogger.info("Bukkit " + getServer().getVersion());

        DebugLogger.info("Plugins: " + Arrays.toString(getServer().getPluginManager().getPlugins()));

        List<String> nodeList = new ArrayList<String>();
        String allNodes = "";
        MyPetConfiguration.getConfigOptionList(nodeList, "MyPet");
        for (String node : nodeList)
        {
            allNodes += "\n   " + node;
        }
        DebugLogger.info("Config: -------------------------------" + allNodes);
        DebugLogger.info("---------------------------------------");

        DebugLogger.info("MobEXP table: -------------------------");
        for (MyPetMonsterExperience monsterExperience : MyPetMonsterExperience.mobExp.values())
        {
            DebugLogger.info("   " + monsterExperience.toString());
        }
        DebugLogger.info("MobEXP table end ----------------------");

        MyPetPlayerListener playerListener = new MyPetPlayerListener();
        getServer().getPluginManager().registerEvents(playerListener, this);

        MyPetVehicleListener vehicleListener = new MyPetVehicleListener();
        getServer().getPluginManager().registerEvents(vehicleListener, this);

        MyPetEntityListener entityListener = new MyPetEntityListener();
        getServer().getPluginManager().registerEvents(entityListener, this);

        MyPetLevelUpListener levelupListener = new MyPetLevelUpListener();
        getServer().getPluginManager().registerEvents(levelupListener, this);

        MyPetWorldListener worldListener = new MyPetWorldListener();
        getServer().getPluginManager().registerEvents(worldListener, this);

        getCommand("petname").setExecutor(new CommandName());
        getCommand("petcall").setExecutor(new CommandCall());
        getCommand("petsendaway").setExecutor(new CommandSendAway());
        getCommand("petstop").setExecutor(new CommandStop());
        getCommand("petrelease").setExecutor(new CommandRelease());
        getCommand("mypet").setExecutor(new CommandHelp());
        getCommand("petinventory").setExecutor(new CommandInventory());
        getCommand("petpickup").setExecutor(new CommandPickup());
        getCommand("petbehavior").setExecutor(new CommandBehavior());
        getCommand("petinfo").setExecutor(new CommandInfo());
        getCommand("petadmin").setExecutor(new CommandAdmin());
        getCommand("petskill").setExecutor(new CommandSkill());
        getCommand("petskilltree").setExecutor(new CommandShowSkillTree());
        getCommand("petchooseskilltree").setExecutor(new CommandChooseSkilltree());
        getCommand("petbeacon").setExecutor(new CommandBeacon());
        getCommand("petrespawn").setExecutor(new CommandRespawn());
        getCommand("pettype").setExecutor(new CommandPetType());
        getCommand("petcapturehelper").setExecutor(new CommandCaptureHelper());

        registerSkillsInfo();
        registerSkills();

        File defaultSkillConfigNBT = new File(getPlugin().getDataFolder().getPath() + File.separator + "skilltrees" + File.separator + "default.st");
        File defaultSkillConfigYAML = new File(getPlugin().getDataFolder().getPath() + File.separator + "skilltrees" + File.separator + "default.yml");
        File defaultSkillConfigJSON = new File(getPlugin().getDataFolder().getPath() + File.separator + "skilltrees" + File.separator + "default.json");

        if (!defaultSkillConfigNBT.exists() && !defaultSkillConfigYAML.exists() && !defaultSkillConfigJSON.exists())
        {
            try
            {
                InputStream template = getPlugin().getResource("skilltrees/default.st");
                OutputStream out = new FileOutputStream(defaultSkillConfigNBT);

                byte[] buf = new byte[1024];
                int len;
                while ((len = template.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }
                template.close();
                out.close();
                MyPetLogger.write("Default skilltree configfile created.");
                DebugLogger.info("created default.st");
            }
            catch (IOException ex)
            {
                MyPetLogger.write(ChatColor.RED + "Unable" + ChatColor.RESET + " to create the default.st!");
                DebugLogger.info("unable to create default.st");
            }
        }

        String[] petTypes = new String[MyPetType.values().length];
        for (int i = 0 ; i < MyPetType.values().length ; i++)
        {
            petTypes[i] = MyPetType.values()[i].getTypeName();
        }

        MyPetSkillTreeMobType.clearMobTypes();
        MyPetSkillTreeLoaderNBT.getSkilltreeLoader().loadSkillTrees(getPlugin().getDataFolder().getPath() + File.separator + "skilltrees", petTypes);
        MyPetSkillTreeLoaderYAML.getSkilltreeLoader().loadSkillTrees(getPlugin().getDataFolder().getPath() + File.separator + "skilltrees", petTypes);
        MyPetSkillTreeLoaderJSON.getSkilltreeLoader().loadSkillTrees(getPlugin().getDataFolder().getPath() + File.separator + "skilltrees", petTypes);

        for (MyPetType mobType : MyPetType.values())
        {
            MyPetSkillTreeMobType skillTreeMobType = MyPetSkillTreeMobType.getMobTypeByName(mobType.getTypeName());
            MyPetSkillTreeLoader.addDefault(skillTreeMobType);
            MyPetSkillTreeLoader.manageInheritance(skillTreeMobType);
        }

        try
        {
            Method a = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, Integer.TYPE);
            a.setAccessible(true);

            // https://github.com/Bukkit/mc-dev/blob/master/net/minecraft/server/EntityTypes.java
            a.invoke(a, EntityMyCreeper.class, "Creeper", 50);
            a.invoke(a, EntityCreeper.class, "Creeper", 50);
            a.invoke(a, EntityMySkeleton.class, "Skeleton", 51);
            a.invoke(a, EntitySkeleton.class, "Skeleton", 51);
            a.invoke(a, EntityMySpider.class, "Spider", 52);
            a.invoke(a, EntitySpider.class, "Spider", 52);
            a.invoke(a, EntityMyGiant.class, "Giant", 53);
            a.invoke(a, EntityGiantZombie.class, "Giant", 53);
            a.invoke(a, EntityMyZombie.class, "Zombie", 54);
            a.invoke(a, EntityZombie.class, "Zombie", 54);
            a.invoke(a, EntityMySlime.class, "Slime", 55);
            a.invoke(a, EntitySlime.class, "Slime", 55);
            a.invoke(a, EntityMyGhast.class, "Ghast", 56);
            a.invoke(a, EntityGhast.class, "Ghast", 56);
            a.invoke(a, EntityMyPigZombie.class, "PigZombie", 57);
            a.invoke(a, EntityPigZombie.class, "PigZombie", 57);
            a.invoke(a, EntityMyEnderman.class, "Enderman", 58);
            a.invoke(a, EntityEnderman.class, "Enderman", 58);
            a.invoke(a, EntityMyCaveSpider.class, "CaveSpider", 59);
            a.invoke(a, EntityCaveSpider.class, "CaveSpider", 59);
            a.invoke(a, EntityMySilverfish.class, "Silverfish", 60);
            a.invoke(a, EntitySilverfish.class, "Silverfish", 60);
            a.invoke(a, EntityMyBlaze.class, "Blaze", 61);
            a.invoke(a, EntityBlaze.class, "Blaze", 61);
            a.invoke(a, EntityMyMagmaCube.class, "LavaSlime", 62);
            a.invoke(a, EntityMagmaCube.class, "LavaSlime", 62);
            a.invoke(a, EntityMyWither.class, "WitherBoss", 64);
            a.invoke(a, EntityWither.class, "WitherBoss", 64);
            a.invoke(a, EntityMyBat.class, "Bat", 65);
            a.invoke(a, EntityBat.class, "Bat", 65);
            a.invoke(a, EntityMyWitch.class, "Witch", 66);
            a.invoke(a, EntityWitch.class, "Witch", 66);
            a.invoke(a, EntityMyPig.class, "Pig", 90);
            a.invoke(a, EntityPig.class, "Pig", 90);
            a.invoke(a, EntityMySheep.class, "Sheep", 91);
            a.invoke(a, EntitySheep.class, "Sheep", 91);
            a.invoke(a, EntityMyCow.class, "Cow", 92);
            a.invoke(a, EntityCow.class, "Cow", 92);
            a.invoke(a, EntityMyChicken.class, "Chicken", 93);
            a.invoke(a, EntityChicken.class, "Chicken", 93);
            a.invoke(a, EntityMyWolf.class, "Wolf", 95);
            a.invoke(a, EntityWolf.class, "Wolf", 95);
            a.invoke(a, EntityMyMooshroom.class, "MushroomCow", 96);
            a.invoke(a, EntityMushroomCow.class, "MushroomCow", 96);
            a.invoke(a, EntityMySnowman.class, "SnowMan", 97);
            a.invoke(a, EntitySnowman.class, "SnowMan", 97);
            a.invoke(a, EntityMyOcelot.class, "Ozelot", 98);
            a.invoke(a, EntityOcelot.class, "Ozelot", 98);
            a.invoke(a, EntityMyIronGolem.class, "VillagerGolem", 99);
            a.invoke(a, EntityIronGolem.class, "VillagerGolem", 99);
            a.invoke(a, EntityMyHorse.class, "EntityHorse", 100);
            a.invoke(a, EntityHorse.class, "EntityHorse", 100);
            a.invoke(a, EntityMyVillager.class, "Villager", 120);
            a.invoke(a, EntityVillager.class, "Villager", 120);

            DebugLogger.info("registered MyPet entities.");
        }
        catch (Exception e)
        {
            MyPetLogger.write("version " + MyPetPlugin.plugin.getDescription().getVersion() + ChatColor.RED + " NOT ENABLED");
            DebugLogger.severe(Arrays.toString(e.getStackTrace()));
            DebugLogger.severe("error while registering MyPet entity.");
            DebugLogger.severe(e.getMessage());
            setEnabled(false);
            return;
        }

        DebugLogger.info("Pet type: ----------");
        for (MyPetType myPetType : MyPetType.values())
        {
            DebugLogger.info("  " + myPetType.getTypeName() + " { " +
                    "startHP:" + MyPet.getStartHP(myPetType.getMyPetClass()) + ", " +
                    "speed:" + MyPet.getStartSpeed(myPetType.getMyPetClass()) + ", " +
                    "food:" + MyPet.getFood(myPetType.getMyPetClass()) + ", " +
                    "leashFlags:" + MyPet.getLeashFlags(myPetType.getMyPetClass()) + " }");
        }

        new MyPetLocales();

        File groupsFile = new File(getPlugin().getDataFolder().getPath() + File.separator + "worldgroups.yml");

        if (MyPetBackup.MAKE_BACKUPS)
        {
            new MyPetBackup(NBTPetFile, new File(getPlugin().getDataFolder().getPath() + File.separator + "backups" + File.separator));
        }
        loadGroups(groupsFile);
        loadPets(NBTPetFile);

        MyPetTimer.startTimer();

        MobArena.findPlugin();
        Minigames.findPlugin();
        PvPArena.findPlugin();
        BattleArena.findPlugin();
        SurvivalGames.findPlugin();
        MyHungerGames.findPlugin();

        try
        {
            Metrics metrics = new Metrics(this);

            Graph graphPercent = metrics.createGraph("Percentage of every MyPet type");
            Graph graphCount = metrics.createGraph("Counted MyPets per type");
            Graph graphTotalCount = metrics.createGraph("Total MyPets");

            for (final MyPetType petType : MyPetType.values())
            {
                Plotter plotter = new Metrics.Plotter(petType.getTypeName())
                {
                    final MyPetType type = petType;

                    @Override
                    public int getValue()
                    {
                        return MyPetList.countMyPets(type);
                    }
                };
                graphPercent.addPlotter(plotter);
                graphCount.addPlotter(plotter);
            }

            Plotter plotter = new Metrics.Plotter("Total MyPets")
            {
                @Override
                public int getValue()
                {
                    return MyPetList.countMyPets();
                }
            };
            graphTotalCount.addPlotter(plotter);
            plotter = new Metrics.Plotter("Active MyPets")
            {
                @Override
                public int getValue()
                {
                    return MyPetList.countActiveMyPets();
                }
            };
            graphTotalCount.addPlotter(plotter);

            boolean metricsActive = metrics.start();
            DebugLogger.info("Metrics " + (metricsActive ? "" : "not ") + "activated");
        }
        catch (IOException e)
        {
            MyPetLogger.write(e.getMessage());
        }

        MyPetLogger.write("version " + MyPetVersion.getMyPetVersion() + "-b" + MyPetVersion.getMyPetBuild() + ChatColor.GREEN + " ENABLED");

        for (Player player : getServer().getOnlinePlayers())
        {
            if (MyPetPlayer.isMyPetPlayer(player))
            {
                MyPetPlayer myPetPlayer = MyPetPlayer.getMyPetPlayer(player);
                MyPetWorldGroup joinGroup = MyPetWorldGroup.getGroup(player.getWorld().getName());
                if (joinGroup != null && !myPetPlayer.hasMyPet() && myPetPlayer.hasMyPetInWorldGroup(joinGroup.getName()))
                {
                    UUID groupMyPetUUID = myPetPlayer.getMyPetForWorldGroup(joinGroup.getName());
                    for (InactiveMyPet inactiveMyPet : myPetPlayer.getInactiveMyPets())
                    {
                        if (inactiveMyPet.getUUID().equals(groupMyPetUUID))
                        {
                            MyPetList.setMyPetActive(inactiveMyPet);
                            break;
                        }
                    }
                    if (!myPetPlayer.hasMyPet())
                    {
                        myPetPlayer.setMyPetForWorldGroup(joinGroup.getName(), null);
                    }
                }
                if (myPetPlayer.hasMyPet())
                {
                    MyPet myPet = MyPetList.getMyPet(player);
                    if (myPet.getStatus() == PetState.Dead)
                    {
                        player.sendMessage(MyPetBukkitUtil.setColors(MyPetLocales.getString("Message.RespawnIn", MyPetBukkitUtil.getPlayerLanguage(player)).replace("%petname%", myPet.getPetName()).replace("%time%", "" + myPet.getRespawnTime())));
                    }
                    else if (myPet.wantToRespawn() && myPet.getLocation().getWorld() == player.getLocation().getWorld() && myPet.getLocation().distance(player.getLocation()) < 75)
                    {
                        myPet.createPet();
                    }
                    else
                    {
                        myPet.setStatus(PetState.Despawned);
                    }
                }
            }
        }
        this.isReady = true;
        savePets(false);
        MyPetTimer.addTask(this);
        DebugLogger.info("----------- MyPet ready -----------");
    }

    public static void registerSkills()
    {
        MyPetSkills.registerSkill(Inventory.class);
        MyPetSkills.registerSkill(HPregeneration.class);
        MyPetSkills.registerSkill(Pickup.class);
        MyPetSkills.registerSkill(Behavior.class);
        MyPetSkills.registerSkill(Damage.class);
        MyPetSkills.registerSkill(Control.class);
        MyPetSkills.registerSkill(HP.class);
        MyPetSkills.registerSkill(Poison.class);
        MyPetSkills.registerSkill(Ride.class);
        MyPetSkills.registerSkill(Thorns.class);
        MyPetSkills.registerSkill(Fire.class);
        MyPetSkills.registerSkill(Beacon.class);
        MyPetSkills.registerSkill(Wither.class);
        MyPetSkills.registerSkill(Lightning.class);
        MyPetSkills.registerSkill(Slow.class);
        MyPetSkills.registerSkill(Knockback.class);
        MyPetSkills.registerSkill(Ranged.class);
        MyPetSkills.registerSkill(Sprint.class);
    }

    public static void registerSkillsInfo()
    {
        MyPetSkillsInfo.registerSkill(InventoryInfo.class);
        MyPetSkillsInfo.registerSkill(HPregenerationInfo.class);
        MyPetSkillsInfo.registerSkill(PickupInfo.class);
        MyPetSkillsInfo.registerSkill(BehaviorInfo.class);
        MyPetSkillsInfo.registerSkill(DamageInfo.class);
        MyPetSkillsInfo.registerSkill(ControlInfo.class);
        MyPetSkillsInfo.registerSkill(HPInfo.class);
        MyPetSkillsInfo.registerSkill(PoisonInfo.class);
        MyPetSkillsInfo.registerSkill(RideInfo.class);
        MyPetSkillsInfo.registerSkill(ThornsInfo.class);
        MyPetSkillsInfo.registerSkill(FireInfo.class);
        MyPetSkillsInfo.registerSkill(BeaconInfo.class);
        MyPetSkillsInfo.registerSkill(WitherInfo.class);
        MyPetSkillsInfo.registerSkill(LightningInfo.class);
        MyPetSkillsInfo.registerSkill(SlowInfo.class);
        MyPetSkillsInfo.registerSkill(KnockbackInfo.class);
        MyPetSkillsInfo.registerSkill(RangedInfo.class);
        MyPetSkillsInfo.registerSkill(SprintInfo.class);
    }

    int loadPets(File f)
    {
        if (!f.exists())
        {
            MyPetLogger.write(ChatColor.YELLOW + "0" + ChatColor.RESET + " pet(s) loaded");
            return 0;
        }
        int petCount = 0;

        NBT_Configuration nbtConfiguration = new NBT_Configuration(f);
        if (!nbtConfiguration.load())
        {
            return 0;
        }
        ListTag petList = (ListTag) nbtConfiguration.getNBTCompound().getValue().get("Pets");
        if (nbtConfiguration.getNBTCompound().getValue().containsKey("CleanShutdown"))
        {
            DebugLogger.info("Clean shutdown: " + ((ByteTag) nbtConfiguration.getNBTCompound().getValue().get("CleanShutdown")).getBooleanValue());
        }

        if(!MyPetConfiguration.USE_MYSQL)
        {
            DebugLogger.info("Loading players -------------------------");
            if (nbtConfiguration.getNBTCompound().getValue().containsKey("Players"))
            {
                DebugLogger.info(loadPlayers(nbtConfiguration) + " PetPlayer(s) loaded");
            }
            DebugLogger.info("-----------------------------------------");
        }
        else
        {
            DebugLogger.info("Loading players(SQL) -------------------------");
            if (nbtConfiguration.getNBTCompound().getValue().containsKey("Players"))
            {
                try {
                    DebugLogger.info(MyPetMySQL.readAllMyPetPlayers() + " PetPlayer(s) loaded");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            DebugLogger.info("-----------------------------------------");
        }

        if(!MyPetConfiguration.USE_MYSQL)
        {
            DebugLogger.info("loading Pets: -----------------------------");
            for (int i = 0 ; i < petList.getValue().size() ; i++)
            {
                CompoundTag myPetNBT = (CompoundTag) petList.getValue().get(i);
                String petOwner = ((StringTag) myPetNBT.getValue().get("Owner")).getValue();
                InactiveMyPet inactiveMyPet = new InactiveMyPet(MyPetPlayer.getMyPetPlayer(petOwner));
                inactiveMyPet.load(myPetNBT);

                MyPetList.addInactiveMyPet(inactiveMyPet);

                DebugLogger.info("   " + inactiveMyPet.toString());

                petCount++;
            }
            MyPetLogger.write("" + ChatColor.YELLOW + petCount + ChatColor.RESET + " pet(s) loaded");
        }
        else
        {
            try {
                DebugLogger.info("loading Pets(SQL): -----------------------------");
                List<InactiveMyPet> tmpList = MyPetMySQL.readAllInactiveMyPets();

                for (InactiveMyPet aTmpList : tmpList) {

                    MyPetList.addInactiveMyPet(aTmpList);

                    DebugLogger.info("   " + aTmpList.toString());

                    petCount++;
                }
                MyPetLogger.write("" + ChatColor.YELLOW + petCount + ChatColor.RESET + " pet(s) loaded");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return petCount;
    }

    public int savePets(boolean shutdown)
    {
        autoSaveTimer = MyPetConfiguration.AUTOSAVE_TIME;
        int petCount = 0;
        if(!MyPetConfiguration.USE_MYSQL)
        {
            NBT_Configuration nbtConfiguration = new NBT_Configuration(NBTPetFile);
            List<CompoundTag> petList = new ArrayList<CompoundTag>();

            for (MyPet myPet : MyPetList.getAllActiveMyPets())
            {
                CompoundTag petNBT = myPet.save();
                petList.add(petNBT);
                petCount++;
            }
            for (InactiveMyPet inactiveMyPet : MyPetList.getAllInactiveMyPets())
            {
                CompoundTag petNBT = inactiveMyPet.save();
                petList.add(petNBT);
                petCount++;
            }
            nbtConfiguration.getNBTCompound().getValue().put("Version", new StringTag("Version", MyPetVersion.getMyPetVersion()));
            nbtConfiguration.getNBTCompound().getValue().put("Build", new StringTag("Build", MyPetVersion.getMyPetBuild()));
            nbtConfiguration.getNBTCompound().getValue().put("CleanShutdown", new ByteTag("CleanShutdown", shutdown));
            nbtConfiguration.getNBTCompound().getValue().put("Pets", new ListTag<CompoundTag>("Pets", CompoundTag.class, petList));
            nbtConfiguration.getNBTCompound().getValue().put("Players", savePlayers());
            nbtConfiguration.save();
        }
        else
        {
            for (MyPet myPet : MyPetList.getAllActiveMyPets())
            {
                MyPetList.getInactiveMyPetFromMyPet(myPet).save_SQL();
                petCount++;
            }
            for (InactiveMyPet inactiveMyPet : MyPetList.getAllInactiveMyPets())
            {
                inactiveMyPet.save_SQL();
                petCount++;
            }

            try {
                MyPetMySQL.writeAllMyPetPlayers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return petCount;
    }

    private ListTag savePlayers()
    {
        List<CompoundTag> playerList = new ArrayList<CompoundTag>();
        for (MyPetPlayer myPetPlayer : MyPetPlayer.getMyPetPlayers())
        {
            if (myPetPlayer.hasCustomData())
            {
                playerList.add(myPetPlayer.save());
            }
        }
        return new ListTag<CompoundTag>("Players", CompoundTag.class, playerList);
    }

    private int loadPlayers(NBT_Configuration nbtConfiguration)
    {
        int playerCount = 0;
        ListTag playerList = (ListTag) nbtConfiguration.getNBTCompound().getValue().get("Players");

        for (int i = 0 ; i < playerList.getValue().size() ; i++)
        {
            CompoundTag myplayerNBT = (CompoundTag) playerList.getValue().get(i);
            MyPetPlayer petPlayer = MyPetPlayer.getMyPetPlayer(((StringTag) myplayerNBT.getValue().get("Name")).getValue());
            petPlayer.load(myplayerNBT);

            playerCount++;
            DebugLogger.info("   " + petPlayer);
        }
        return playerCount;
    }

    private int loadGroups(File f)
    {
        YAML_Configuration yamlConfiguration = new YAML_Configuration(f);
        FileConfiguration config = yamlConfiguration.getConfig();

        if (config == null)
        {
            return 0;
        }

        MyPetWorldGroup.clearGroups();

        Set<String> nodes;
        try
        {
            nodes = config.getConfigurationSection("Groups").getKeys(false);
        }
        catch (NullPointerException e)
        {
            nodes = new HashSet<String>();
            MyPetLogger.write("No groups found. Everything will be in 'default' group.");
        }

        DebugLogger.info("--- Load WorldGroups ---------------------------");
        if (nodes.size() == 0)
        {
            List<String> worldNames = new ArrayList<String>();
            MyPetWorldGroup defaultGroup = new MyPetWorldGroup("default");
            defaultGroup.registerGroup();
            for (org.bukkit.World world : this.getServer().getWorlds())
            {
                MyPetLogger.write("added " + ChatColor.GOLD + world.getName() + ChatColor.RESET + " to 'default' group.");
                worldNames.add(world.getName());
                DebugLogger.info("   added " + world.getName() + " to " + defaultGroup.getName());
                defaultGroup.addWorld(world.getName());
            }
            config.set("Groups.default", worldNames);
            yamlConfiguration.saveConfig();
        }
        else
        {
            for (String node : nodes)
            {
                List<String> worlds = config.getStringList("Groups." + node);
                if (worlds.size() > 0)
                {
                    MyPetWorldGroup newGroup = new MyPetWorldGroup(node);
                    for (String world : worlds)
                    {
                        if (getServer().getWorld(world) != null)
                        {
                            DebugLogger.info("   added '" + world + "' to '" + newGroup.getName() + "'");
                            newGroup.addWorld(world);
                        }
                    }
                    if (newGroup.getWorlds().size() > 0)
                    {
                        DebugLogger.info("   registered '" + newGroup.getName() + "' group");
                        newGroup.registerGroup();
                    }
                }
            }

            MyPetWorldGroup defaultGroup = null;
            for (MyPetWorldGroup group : MyPetWorldGroup.getGroups())
            {
                if (group.getName().equalsIgnoreCase("default"))
                {
                    defaultGroup = group;
                    break;
                }
            }
            if (defaultGroup == null)
            {
                defaultGroup = new MyPetWorldGroup("default");
                defaultGroup.registerGroup();
                DebugLogger.info("   registered 'default' group");
            }

            boolean saveConfig = false;
            for (org.bukkit.World world : getServer().getWorlds())
            {
                if (MyPetWorldGroup.getGroup(world.getName()) == null)
                {
                    MyPetLogger.write("added " + ChatColor.GOLD + world.getName() + ChatColor.RESET + " to 'default' group.");
                    defaultGroup.addWorld(world.getName());
                    saveConfig = true;
                }
            }
            if (saveConfig)
            {
                config.set("Groups.default", defaultGroup.getWorlds());
                yamlConfiguration.saveConfig();
            }
        }
        DebugLogger.info("-------------------------------------------------");
        return 0;
    }

    @Override
    public void schedule()
    {
        if (MyPetConfiguration.AUTOSAVE_TIME > 0 && autoSaveTimer-- <= 0)
        {
            MyPetPlugin.getPlugin().savePets(false);
            autoSaveTimer = MyPetConfiguration.AUTOSAVE_TIME;
        }
    }

    public File getFile()
    {
        return super.getFile();
    }
}