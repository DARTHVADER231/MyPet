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

package de.Keyle.MyPet.commands;

import de.Keyle.MyPet.MyPetPlugin;
import de.Keyle.MyPet.entity.types.InactiveMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPet.PetState;
import de.Keyle.MyPet.entity.types.MyPetList;
import de.Keyle.MyPet.entity.types.MyPetType;
import de.Keyle.MyPet.skill.skills.ISkillStorage;
import de.Keyle.MyPet.skill.skills.implementation.ISkillInstance;
import de.Keyle.MyPet.skill.skilltree.SkillTree;
import de.Keyle.MyPet.skill.skilltree.SkillTreeMobType;
import de.Keyle.MyPet.skill.skilltreeloader.SkillTreeLoader;
import de.Keyle.MyPet.skill.skilltreeloader.SkillTreeLoaderJSON;
import de.Keyle.MyPet.skill.skilltreeloader.SkillTreeLoaderNBT;
import de.Keyle.MyPet.skill.skilltreeloader.SkillTreeLoaderYAML;
import de.Keyle.MyPet.util.*;
import de.Keyle.MyPet.util.locale.Locales;
import de.Keyle.MyPet.util.logger.DebugLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.spout.nbt.ByteTag;
import org.spout.nbt.CompoundTag;
import org.spout.nbt.IntTag;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandAdmin implements CommandExecutor, TabCompleter
{
    private static List<String> optionsList = new ArrayList<String>();
    private static List<String> emptyList = new ArrayList<String>();
    private static List<String> addSetRemoveList = new ArrayList<String>();
    private static List<String> showList = new ArrayList<String>();
    private static List<String> petTypeList = new ArrayList<String>();
    private static Map<String, List<String>> petTypeOptionMap = new HashMap<String, List<String>>();

    static
    {
        optionsList.add("name");
        optionsList.add("exp");
        optionsList.add("respawn");
        optionsList.add("reload");
        optionsList.add("reloadskills");
        optionsList.add("skilltree");
        optionsList.add("build");
        optionsList.add("create");
        optionsList.add("clone");
        optionsList.add("remove");
        optionsList.add("cleanup");

        addSetRemoveList.add("add");
        addSetRemoveList.add("set");
        addSetRemoveList.add("remove");

        showList.add("show");
        showList.add("<number>");

        List<String> petTypeOptionList = new ArrayList<String>();

        petTypeOptionList.add("fire");
        petTypeOptionMap.put("blaze", petTypeOptionList);

        petTypeOptionList = new ArrayList<String>();
        petTypeOptionList.add("baby");
        petTypeOptionMap.put("chicken", petTypeOptionList);
        petTypeOptionMap.put("cow", petTypeOptionList);
        petTypeOptionMap.put("mooshroom", petTypeOptionList);

        petTypeOptionList = new ArrayList<String>();
        petTypeOptionList.add("powered");
        petTypeOptionMap.put("creeper", petTypeOptionList);

        petTypeOptionList = new ArrayList<String>();
        petTypeOptionList.add("block:");
        petTypeOptionMap.put("enderman", petTypeOptionList);

        petTypeOptionList = new ArrayList<String>();
        petTypeOptionList.add("size:");
        petTypeOptionMap.put("magmacube", petTypeOptionList);
        petTypeOptionMap.put("slime", petTypeOptionList);

        petTypeOptionList = new ArrayList<String>();
        petTypeOptionList.add("baby");
        petTypeOptionList.add("cat:");
        petTypeOptionMap.put("ocelot", petTypeOptionList);

        petTypeOptionList = new ArrayList<String>();
        petTypeOptionList.add("baby");
        petTypeOptionList.add("saddle");
        petTypeOptionMap.put("pig", petTypeOptionList);

        petTypeOptionList = new ArrayList<String>();
        petTypeOptionList.add("baby");
        petTypeOptionList.add("color:");
        petTypeOptionMap.put("sheep", petTypeOptionList);

        petTypeOptionList = new ArrayList<String>();
        petTypeOptionList.add("wither");
        petTypeOptionMap.put("skeleton", petTypeOptionList);

        petTypeOptionList = new ArrayList<String>();
        petTypeOptionList.add("baby");
        petTypeOptionList.add("profession:");
        petTypeOptionMap.put("villager", petTypeOptionList);

        petTypeOptionList = new ArrayList<String>();
        petTypeOptionList.add("baby");
        petTypeOptionList.add("angry");
        petTypeOptionList.add("tamed");
        petTypeOptionList.add("collar:");
        petTypeOptionMap.put("wolf", petTypeOptionList);

        petTypeOptionList = new ArrayList<String>();
        petTypeOptionList.add("baby");
        petTypeOptionList.add("villager");
        petTypeOptionMap.put("zombie", petTypeOptionList);

        petTypeOptionList = new ArrayList<String>();
        petTypeOptionList.add("baby");
        petTypeOptionMap.put("pigzombie", petTypeOptionList);

        petTypeOptionList = new ArrayList<String>();
        petTypeOptionList.add("baby");
        petTypeOptionList.add("chest");
        petTypeOptionList.add("saddle");
        petTypeOptionList.add("horse:");
        petTypeOptionList.add("variant:");
        petTypeOptionMap.put("horse", petTypeOptionList);

        for (MyPetType petType : MyPetType.values())
        {
            petTypeList.add(petType.getTypeName());
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        String lang = "en";
        if (sender instanceof Player)
        {
            if (!Permissions.has((Player) sender, "MyPet.admin", false))
            {
                return true;
            }
            lang = BukkitUtil.getPlayerLanguage((Player) sender);
        }
        if (args.length < 1)
        {
            return false;
        }
        String option = args[0];
        String[] parameter = Arrays.copyOfRange(args, 1, args.length);

        if (option.equalsIgnoreCase("name") && parameter.length >= 2)
        {
            Player petOwner = Bukkit.getServer().getPlayer(parameter[0]);

            if (petOwner == null || !petOwner.isOnline())
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Locales.getString("Message.No.PlayerOnline", lang));
                return true;
            }
            else if (!MyPetList.hasMyPet(petOwner))
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Util.formatText(Locales.getString("Message.No.UserHavePet", lang), petOwner.getName()));
                return true;
            }
            MyPet myPet = MyPetList.getMyPet(petOwner);

            String name = "";
            for (int i = 1 ; i < parameter.length ; i++)
            {
                if (!name.equals(""))
                {
                    name += " ";
                }
                name += parameter[i];
            }
            name = Colorizer.setColors(name);

            Pattern regex = Pattern.compile("§[abcdefklmnor0-9]");
            Matcher regexMatcher = regex.matcher(name);
            if (regexMatcher.find())
            {
                name += ChatColor.RESET;
            }

            myPet.setPetName(name);
            sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] new name is now: " + name);
        }
        else if (option.equalsIgnoreCase("exp") && parameter.length >= 2)
        {
            Player petOwner = Bukkit.getServer().getPlayer(parameter[0]);

            if (petOwner == null || !petOwner.isOnline())
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Locales.getString("Message.No.PlayerOnline", lang));
                return true;
            }
            else if (!MyPetList.hasMyPet(petOwner))
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Util.formatText(Locales.getString("Message.No.UserHavePet", lang), petOwner.getName()));
                return true;
            }
            MyPet myPet = MyPetList.getMyPet(petOwner);
            String value = parameter[1];

            if (parameter.length == 2 || (parameter.length >= 3 && parameter[2].equalsIgnoreCase("set")))
            {
                if (Util.isDouble(value))
                {
                    double Exp = Double.parseDouble(value);
                    Exp = Exp < 0 ? 0 : Exp;
                    if (myPet.getExperience().getExp() > Exp)
                    {
                        myPet.getSkills().reset();
                        myPet.getExperience().reset();
                        myPet.getExperience().addExp(Exp);
                        sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] set " + Exp + "exp. Pet is now level " + myPet.getExperience().getLevel() + ".");
                    }
                    else
                    {
                        myPet.getExperience().addExp(Exp - myPet.getExperience().getExp());
                        sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] set exp to " + Exp + "exp");
                    }
                }
            }
            else if (parameter.length >= 3 && parameter[2].equalsIgnoreCase("add"))
            {
                if (Util.isDouble(value))
                {
                    double Exp = Double.parseDouble(value);
                    Exp = Exp < 0 ? 0 : Exp;
                    myPet.getExperience().addExp(Exp);
                    sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] added " + Exp + "exp.");
                }
            }
            else if (parameter.length >= 3 && parameter[2].equalsIgnoreCase("remove"))
            {
                if (Util.isDouble(value))
                {
                    double Exp = Double.parseDouble(value);
                    Exp = Exp < 0 ? 0 : Exp;
                    Exp = Exp <= myPet.getExperience().getExp() ? Exp : myPet.getExperience().getExp();
                    if (Exp <= myPet.getExperience().getCurrentExp())
                    {
                        myPet.getExperience().removeExp(Exp);
                        sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] removed " + value + "exp.");
                    }
                    else
                    {
                        Exp = myPet.getExperience().getExp() - Exp;
                        myPet.getSkills().reset();
                        myPet.getExperience().reset();
                        myPet.getExperience().addExp(Exp);
                        sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] removed " + Exp + "exp. Pet is now level " + myPet.getExperience().getLevel() + ".");
                    }
                }
            }
        }
        else if (option.equalsIgnoreCase("respawn") && parameter.length >= 1)
        {
            Player petOwner = Bukkit.getServer().getPlayer(parameter[0]);

            if (petOwner == null || !petOwner.isOnline())
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Locales.getString("Message.No.PlayerOnline", lang));
                return true;
            }
            else if (!MyPetList.hasMyPet(petOwner))
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Util.formatText(Locales.getString("Message.No.UserHavePet", lang), petOwner.getName()));
                return true;
            }
            MyPet myPet = MyPetList.getMyPet(petOwner);
            if (parameter.length >= 2 && parameter[1].equalsIgnoreCase("show"))
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] respawn time: " + myPet.getRespawnTime() + "sec.");
            }
            else if (myPet.getStatus() == PetState.Dead)
            {
                if (parameter.length >= 2 && Util.isInt(parameter[1]))
                {
                    int respawnTime = Integer.parseInt(parameter[1]);
                    if (respawnTime >= 0)
                    {
                        myPet.setRespawnTime(respawnTime);
                    }
                }
                else
                {
                    myPet.setRespawnTime(0);
                }
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] set respawn time to: " + myPet.getRespawnTime() + "sec.");
            }
            else
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] pet is not dead!");
            }
        }
        else if (option.equalsIgnoreCase("reload"))
        {
            MyPetPlugin.getPlugin().reloadConfig();
            Configuration.config = MyPetPlugin.getPlugin().getConfig();
            Configuration.loadConfiguration();
            DebugLogger.info("Config reloaded.");
            sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] config (config.yml) reloaded!");
        }
        else if (option.equalsIgnoreCase("build"))
        {
            DebugLogger.info("MyPet-" + MyPetVersion.getMyPetVersion() + "-b#" + MyPetVersion.getMyPetBuild());
            sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] MyPet-" + MyPetVersion.getMyPetVersion() + "-b#" + MyPetVersion.getMyPetBuild());
        }
        else if (option.equalsIgnoreCase("reloadskills"))
        {
            SkillTreeMobType.clearMobTypes();
            String[] petTypes = new String[MyPetType.values().length];
            for (int i = 0 ; i < MyPetType.values().length ; i++)
            {
                petTypes[i] = MyPetType.values()[i].getTypeName();
            }
            for (MyPet myPet : MyPetList.getAllActiveMyPets())
            {
                myPet.getSkills().reset();
            }

            SkillTreeMobType.clearMobTypes();
            SkillTreeLoaderNBT.getSkilltreeLoader().loadSkillTrees(MyPetPlugin.getPlugin().getDataFolder().getPath() + File.separator + "skilltrees", petTypes);
            SkillTreeLoaderYAML.getSkilltreeLoader().loadSkillTrees(MyPetPlugin.getPlugin().getDataFolder().getPath() + File.separator + "skilltrees", petTypes);
            SkillTreeLoaderJSON.getSkilltreeLoader().loadSkillTrees(MyPetPlugin.getPlugin().getDataFolder().getPath() + File.separator + "skilltrees", petTypes);

            for (MyPetType mobType : MyPetType.values())
            {
                SkillTreeMobType skillTreeMobType = SkillTreeMobType.getMobTypeByName(mobType.getTypeName());
                SkillTreeLoader.addDefault(skillTreeMobType);
                SkillTreeLoader.manageInheritance(skillTreeMobType);
            }

            for (MyPet myPet : MyPetList.getAllActiveMyPets())
            {
                myPet.getSkills().reset();

                SkillTree skillTree = myPet.getSkillTree();
                if (skillTree != null)
                {
                    String skilltreeName = skillTree.getName();
                    if (SkillTreeMobType.hasMobType(myPet.getPetType().getTypeName()))
                    {
                        SkillTreeMobType mobType = SkillTreeMobType.getMobTypeByPetType(myPet.getPetType());

                        if (mobType.hasSkillTree(skilltreeName))
                        {
                            skillTree = mobType.getSkillTree(skilltreeName);
                        }
                        else
                        {
                            skillTree = null;
                        }
                    }
                    else
                    {
                        skillTree = null;
                    }
                }
                myPet.setSkilltree(skillTree);
                if (skillTree != null)
                {
                    sender.sendMessage(Util.formatText(Locales.getString("Message.Command.Skills.Show", myPet.getOwner()), myPet.getPetName(), (myPet.getSkillTree() == null ? "-" : myPet.getSkillTree().getDisplayName())));
                    for (ISkillInstance skill : myPet.getSkills().getSkills())
                    {
                        if (skill.isActive())
                        {
                            myPet.sendMessageToOwner("  " + ChatColor.GREEN + skill.getName() + ChatColor.RESET + " " + skill.getFormattedValue());
                        }
                    }
                }
            }
            for (InactiveMyPet myPet : MyPetList.getAllInactiveMyPets())
            {
                SkillTree skillTree = myPet.getSkillTree();
                if (skillTree != null)
                {
                    String skilltreeName = skillTree.getName();
                    if (SkillTreeMobType.getMobTypeByPetType(myPet.getPetType()) != null)
                    {
                        SkillTreeMobType mobType = SkillTreeMobType.getMobTypeByPetType(myPet.getPetType());

                        if (mobType.hasSkillTree(skilltreeName))
                        {
                            skillTree = mobType.getSkillTree(skilltreeName);
                        }
                        else
                        {
                            skillTree = null;
                        }
                    }
                    else
                    {
                        skillTree = null;
                    }
                }
                myPet.setSkillTree(skillTree);
            }
            sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] skilltrees reloaded!");
            DebugLogger.info("Skilltrees reloaded.");
        }
        else if (option.equalsIgnoreCase("skilltree"))
        {
            if (parameter.length < 2)
            {
                return false;
            }
            Player petOwner = Bukkit.getServer().getPlayer(parameter[0]);

            if (petOwner == null || !petOwner.isOnline())
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Locales.getString("Message.No.PlayerOnline", lang));
                return true;
            }
            else if (!MyPetList.hasMyPet(petOwner))
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Util.formatText(Locales.getString("Message.No.UserHavePet", lang), petOwner.getName()));
                return true;
            }
            MyPet myPet = MyPetList.getMyPet(petOwner);

            SkillTreeMobType skillTreeMobType = SkillTreeMobType.getMobTypeByName(myPet.getPetType().getTypeName());
            if (skillTreeMobType.hasSkillTree(parameter[1]))
            {
                SkillTree skillTree = skillTreeMobType.getSkillTree(parameter[1]);
                if (myPet.setSkilltree(skillTree))
                {
                    sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Util.formatText(Locales.getString("Message.Skilltree.SwitchedToFor", lang), petOwner.getName(), skillTree.getName()));
                }
                else
                {
                    sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Locales.getString("Message.Skilltree.NotSwitched", lang));
                }
            }
            else
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Util.formatText(Locales.getString("Message.Command.Skilltree.CantFindSkilltree", lang), parameter[2]));
            }
        }
        else if (option.equalsIgnoreCase("create"))
        {
            if (parameter.length < 2)
            {
                return false;
            }
            MyPetType myPetType = MyPetType.getMyPetTypeByName(parameter[1]);
            if (myPetType != null)
            {
                Player owner = Bukkit.getPlayer(parameter[0]);
                if (owner == null || !owner.isOnline())
                {
                    sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Locales.getString("Message.No.PlayerOnline", lang));
                    return true;
                }

                MyPetPlayer newOwner = MyPetPlayer.getMyPetPlayer(owner);
                if (!newOwner.hasMyPet())
                {
                    InactiveMyPet inactiveMyPet = new InactiveMyPet(newOwner);
                    inactiveMyPet.setPetType(myPetType);
                    inactiveMyPet.setPetName(Locales.getString("Name." + inactiveMyPet.getPetType().getTypeName(), inactiveMyPet.getOwner().getLanguage()));

                    CompoundTag compoundTag = inactiveMyPet.getInfo();
                    if (parameter.length > 2)
                    {
                        for (int i = 2 ; i < parameter.length ; i++)
                        {
                            if (parameter[i].equalsIgnoreCase("baby"))
                            {
                                compoundTag.getValue().put("Baby", new ByteTag("Baby", true));
                            }
                            else if (parameter[i].equalsIgnoreCase("fire"))
                            {
                                compoundTag.getValue().put("Fire", new ByteTag("Fire", true));
                            }
                            else if (parameter[i].equalsIgnoreCase("powered"))
                            {
                                compoundTag.getValue().put("Powered", new ByteTag("Powered", true));
                            }
                            else if (parameter[i].equalsIgnoreCase("saddle"))
                            {
                                compoundTag.getValue().put("Saddle", new ByteTag("Saddle", true));
                            }
                            else if (parameter[i].equalsIgnoreCase("sheared"))
                            {
                                compoundTag.getValue().put("Sheared", new ByteTag("Sheared", true));
                            }
                            else if (parameter[i].equalsIgnoreCase("wither"))
                            {
                                compoundTag.getValue().put("Wither", new ByteTag("Wither", true));
                            }
                            else if (parameter[i].equalsIgnoreCase("tamed"))
                            {
                                compoundTag.getValue().put("Tamed", new ByteTag("Tamed", true));
                            }
                            else if (parameter[i].equalsIgnoreCase("angry"))
                            {
                                compoundTag.getValue().put("Angry", new ByteTag("Angry", true));
                            }
                            else if (parameter[i].equalsIgnoreCase("villager"))
                            {
                                compoundTag.getValue().put("Villager", new ByteTag("Villager", true));
                            }
                            else if (parameter[i].equalsIgnoreCase("chest"))
                            {
                                compoundTag.getValue().put("Chest", new ByteTag("Chest", true));
                            }
                            else if (parameter[i].startsWith("size:"))
                            {
                                String size = parameter[i].replace("size:", "");
                                if (Util.isInt(size))
                                {
                                    compoundTag.getValue().put("Size", new IntTag("Size", Integer.parseInt(size)));
                                }
                            }
                            else if (parameter[i].startsWith("horse:"))
                            {
                                String horseTypeString = parameter[i].replace("horse:", "");
                                if (Util.isByte(horseTypeString))
                                {
                                    int horseType = Integer.parseInt(horseTypeString);
                                    horseType = Math.min(Math.max(0, horseType), 4);
                                    compoundTag.getValue().put("Type", new ByteTag("Type", (byte) horseType));
                                }
                            }
                            else if (parameter[i].startsWith("variant:"))
                            {
                                String variantString = parameter[i].replace("variant:", "");
                                if (Util.isInt(variantString))
                                {
                                    int variant = Integer.parseInt(variantString);
                                    variant = Math.min(Math.max(0, variant), 1030);
                                    compoundTag.getValue().put("Variant", new IntTag("Variant", variant));
                                }
                            }
                            else if (parameter[i].startsWith("cat:"))
                            {
                                String catTypeString = parameter[i].replace("cat:", "");
                                if (Util.isInt(catTypeString))
                                {
                                    int catType = Integer.parseInt(catTypeString);
                                    catType = Math.min(Math.max(0, catType), 3);
                                    compoundTag.getValue().put("CatType", new IntTag("CatType", catType));
                                }
                            }
                            else if (parameter[i].startsWith("profession:"))
                            {
                                String professionString = parameter[i].replace("profession:", "");
                                if (Util.isInt(professionString))
                                {
                                    int profession = Integer.parseInt(professionString);
                                    profession = Math.min(Math.max(0, profession), 5);
                                    compoundTag.getValue().put("Profession", new IntTag("Profession", profession));
                                }
                            }
                            else if (parameter[i].startsWith("color:"))
                            {
                                String colorString = parameter[i].replace("color:", "");
                                if (Util.isByte(colorString))
                                {
                                    byte color = Byte.parseByte(colorString);
                                    color = color > 15 ? 15 : color < 0 ? 0 : color;
                                    compoundTag.getValue().put("Color", new ByteTag("Color", color));
                                }
                            }
                            else if (parameter[i].startsWith("collar:"))
                            {
                                String colorString = parameter[i].replace("collar:", "");
                                if (Util.isByte(colorString))
                                {
                                    byte color = Byte.parseByte(colorString);
                                    color = color > 15 ? 15 : color < 0 ? 0 : color;
                                    compoundTag.getValue().put("CollarColor", new ByteTag("CollarColor", color));
                                }
                            }
                            else if (parameter[i].startsWith("block:"))
                            {
                                String blocks = parameter[i].replace("block:", "");
                                String[] blockInfo = blocks.split(":");
                                if (blockInfo.length >= 1 && Util.isInt(blockInfo[0]) && BukkitUtil.isValidMaterial(Integer.parseInt(blockInfo[0])))
                                {
                                    compoundTag.getValue().put("BlockID", new IntTag("BlockID", Integer.parseInt(blockInfo[0])));
                                }
                                if (blockInfo.length >= 2 && Util.isInt(blockInfo[1]))
                                {
                                    int blockData = Integer.parseInt(blockInfo[1]);
                                    blockData = Math.min(Math.max(0, blockData), 15);
                                    compoundTag.getValue().put("BlockData", new IntTag("BlockData", blockData));
                                }
                            }
                        }
                    }

                    MyPetList.addInactiveMyPet(inactiveMyPet);
                    MyPet myPet = MyPetList.setMyPetActive(inactiveMyPet);
                    myPet.createPet();

                    WorldGroup wg = WorldGroup.getGroup(owner.getWorld().getName());
                    myPet.setWorldGroup(wg.getName());
                    myPet.getOwner().setMyPetForWorldGroup(wg.getName(), myPet.getUUID());
                }
                else
                {
                    sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + newOwner.getName() + " has already an active MyPet!");
                }
            }
        }
        else if (option.equalsIgnoreCase("clone"))
        {
            if (parameter.length < 2)
            {
                return false;
            }

            Player oldOwner = Bukkit.getPlayer(parameter[0]);
            if (oldOwner == null || !oldOwner.isOnline())
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Locales.getString("Message.No.PlayerOnline", lang));
                return true;
            }
            Player newOwner = Bukkit.getPlayer(parameter[1]);
            if (newOwner == null || !newOwner.isOnline())
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Locales.getString("Message.No.PlayerOnline", lang));
                return true;
            }

            MyPetPlayer oldPetOwner = MyPetPlayer.getMyPetPlayer(oldOwner);
            MyPetPlayer newPetOwner = MyPetPlayer.getMyPetPlayer(newOwner);

            if (!oldPetOwner.hasMyPet())
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Util.formatText(Locales.getString("Message.No.UserHavePet", lang), oldOwner.getName()));
                return true;
            }
            if (newPetOwner.hasMyPet())
            {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + newOwner.getName() + " has already an active MyPet!");
                return true;
            }

            MyPet oldPet = oldPetOwner.getMyPet();
            InactiveMyPet newPet = new InactiveMyPet(newPetOwner);
            newPet.setPetName(oldPet.getPetName());
            newPet.setExp(oldPet.getExperience().getExp());
            newPet.setHealth(oldPet.getHealth());
            newPet.setHungerValue(oldPet.getHungerValue());
            newPet.setRespawnTime(oldPet.getRespawnTime());
            newPet.setInfo(oldPet.getExtendedInfo());
            newPet.setPetType(oldPet.getPetType());
            newPet.setSkillTree(oldPet.getSkillTree());
            newPet.setWorldGroup(oldPet.getWorldGroup());
            CompoundTag skillCompund = newPet.getSkills();
            for (ISkillInstance skill : oldPet.getSkills().getSkills())
            {
                if (skill instanceof ISkillStorage)
                {
                    ISkillStorage storageSkill = (ISkillStorage) skill;
                    CompoundTag s = storageSkill.save();
                    if (s != null)
                    {
                        skillCompund.getValue().put(skill.getName(), s);
                    }
                }
            }

            MyPetList.addInactiveMyPet(newPet);
            MyPetList.setMyPetActive(newPet);

            sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] MyPet owned by " + newOwner.getName() + " successfully cloned!");
        }
        else if (option.equalsIgnoreCase("remove"))
        {
            if (parameter.length >= 1)
            {
                Player player = Bukkit.getPlayer(parameter[0]);
                if (player == null || !player.isOnline())
                {
                    sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] " + Locales.getString("Message.No.PlayerOnline", lang));
                    return true;
                }
                if (MyPetPlayer.isMyPetPlayer(player))
                {
                    MyPetPlayer petOwner = MyPetPlayer.getMyPetPlayer(player);
                    if (petOwner.hasMyPet())
                    {
                        MyPet myPet = petOwner.getMyPet();

                        sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] You removed the MyPet of: " + ChatColor.YELLOW + petOwner.getName());

                        myPet.getOwner().setMyPetForWorldGroup(WorldGroup.getGroup(player.getWorld().getName()).getName(), null);
                        MyPetList.removeInactiveMyPet(MyPetList.setMyPetInactive(myPet.getOwner()));
                    }
                }
            }
        }
        else if (option.equalsIgnoreCase("cleanup"))
        {
            if (Util.isInt(parameter[0]))
            {
                int days = Integer.parseInt(parameter[0]);
                boolean deleteOld = days == -1;
                List<InactiveMyPet> deletionList = new ArrayList<InactiveMyPet>();
                for (InactiveMyPet inactiveMyPet : MyPetList.getAllInactiveMyPets())
                {
                    if (inactiveMyPet.getLastUsed() != -1 && !deleteOld)
                    {
                        if (TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - inactiveMyPet.getLastUsed()) > days)
                        {
                            deletionList.add(inactiveMyPet);
                        }
                    }
                    else if (inactiveMyPet.getLastUsed() == -1 && deleteOld)
                    {
                        deletionList.add(inactiveMyPet);
                    }
                }
                int deletedPetCount = deletionList.size();
                if (deletedPetCount > 0)
                {
                    sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] created backup -> " + MyPetPlugin.getPlugin().getBackupManager().createBackup());

                    for (InactiveMyPet inactiveMyPet : deletionList)
                    {
                        MyPetList.removeInactiveMyPet(inactiveMyPet);
                    }
                }
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet" + ChatColor.RESET + "] removed " + deletedPetCount + " MyPets.");
            }
        }
        else if (option.equalsIgnoreCase("test"))
        {

        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings)
    {
        if (!Permissions.has((Player) commandSender, "MyPet.admin", false))
        {
            return emptyList;
        }
        if (strings.length == 1)
        {
            return optionsList;
        }
        else if (strings.length >= 1)
        {
            if (strings[0].equalsIgnoreCase("name"))
            {
                if (strings.length == 2)
                {
                    return null;
                }
                if (strings.length > 2)
                {
                    return emptyList;
                }
            }
            else if (strings[0].equalsIgnoreCase("exp"))
            {
                if (strings.length == 2)
                {
                    return null;
                }
                else if (strings.length == 3)
                {
                    return emptyList;
                }
                else if (strings.length == 4)
                {
                    return addSetRemoveList;
                }
            }
            else if (strings[0].equalsIgnoreCase("respawn"))
            {
                if (strings.length == 2)
                {
                    return null;
                }
                if (strings.length == 3)
                {
                    return showList;
                }
            }
            else if (strings[0].equalsIgnoreCase("skilltree"))
            {
                if (strings.length == 2)
                {
                    return null;
                }
                if (strings.length == 3)
                {
                    Player player = Bukkit.getServer().getPlayer(strings[1]);
                    if (player == null || !player.isOnline())
                    {
                        return emptyList;
                    }
                    if (MyPetList.hasMyPet(player))
                    {
                        MyPet myPet = MyPetList.getMyPet(player);
                        SkillTreeMobType skillTreeMobType = SkillTreeMobType.getMobTypeByName(myPet.getPetType().getTypeName());

                        List<String> skilltreeList = new ArrayList<String>();
                        for (SkillTree skillTree : skillTreeMobType.getSkillTrees())
                        {
                            skilltreeList.add(skillTree.getName());
                        }
                        return skilltreeList;
                    }
                    return emptyList;
                }
            }
            else if (strings[0].equalsIgnoreCase("create"))
            {
                if (strings.length == 2)
                {
                    return null;
                }
                if (strings.length == 3)
                {
                    return petTypeList;
                }
                if (strings.length >= 4)
                {
                    if (petTypeOptionMap.containsKey(strings[2].toLowerCase()))
                    {
                        return petTypeOptionMap.get(strings[2].toLowerCase());
                    }
                    return emptyList;
                }
            }
            else if (strings[0].equalsIgnoreCase("clone"))
            {
                if (strings.length == 2)
                {
                    return null;
                }
                if (strings.length == 3)
                {
                    return null;
                }
            }
            else if (strings[0].equalsIgnoreCase("remove"))
            {
                if (strings.length == 2)
                {
                    return null;
                }
            }
        }
        return emptyList;
    }
}