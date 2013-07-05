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

package de.Keyle.MyPet.skill.skills.implementation;

import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.skill.ISkillActive;
import de.Keyle.MyPet.skill.ISkillStorage;
import de.Keyle.MyPet.skill.skills.implementation.inventory.MyPetCustomInventory;
import de.Keyle.MyPet.skill.skills.info.ISkillInfo;
import de.Keyle.MyPet.skill.skills.info.InventoryInfo;
import de.Keyle.MyPet.util.MyPetBukkitUtil;
import de.Keyle.MyPet.util.MyPetPermissions;
import de.Keyle.MyPet.util.locale.MyPetLocales;
import de.Keyle.MyPet.util.support.*;
import net.minecraft.server.v1_6_R1.EntityPlayer;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_6_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import de.Keyle.nbt.ByteTag;
import de.Keyle.nbt.CompoundMap;
import de.Keyle.nbt.CompoundTag;
import de.Keyle.nbt.IntTag;

public class Inventory extends InventoryInfo implements ISkillInstance, ISkillStorage, ISkillActive
{
    public MyPetCustomInventory inv = new MyPetCustomInventory("Pet's Inventory", 0);
    public static boolean OPEN_IN_CREATIVEMODE = true;
    public static boolean DROP_WHEN_OWNER_DIES = true;
    private MyPet myPet;

    public Inventory(boolean addedByInheritance)
    {
        super(addedByInheritance);
    }

    public void setMyPet(MyPet myPet)
    {
        this.myPet = myPet;
        inv.setName(myPet.getPetName());
    }

    public MyPet getMyPet()
    {
        return null;
    }

    public void upgrade(ISkillInfo upgrade, boolean quiet)
    {
        if (upgrade instanceof InventoryInfo)
        {
            if (upgrade.getProperties().getValue().containsKey("add"))
            {
                rows += ((IntTag) upgrade.getProperties().getValue().get("add")).getValue();
                if (rows > 6)
                {
                    rows = 6;
                }
                inv.setSize(rows * 9);
                if (!quiet)
                {
                    myPet.sendMessageToOwner(MyPetBukkitUtil.setColors(MyPetLocales.getString("Message.Skill.Inventory.Upgrade", myPet.getOwner())).replace("%petname%", myPet.getPetName()).replace("%size%", "" + inv.getSize()));
                }
            }
            if (upgrade.getProperties().getValue().containsKey("drop"))
            {
                dropOnDeath = ((ByteTag) upgrade.getProperties().getValue().get("drop")).getBooleanValue();
            }
        }
    }

    public String getFormattedValue()
    {
        return rows + " " + MyPetLocales.getString("Name.Rows", myPet.getOwner());
    }

    public void reset()
    {
        rows = 0;
        inv.setSize(0);
    }

    public boolean activate()
    {
        if (rows > 0)
        {
            if (myPet.getOwner().getPlayer().getGameMode() == GameMode.CREATIVE && !OPEN_IN_CREATIVEMODE && !MyPetPermissions.has(myPet.getOwner().getPlayer(), "MyPet.admin", false))
            {
                myPet.sendMessageToOwner(MyPetBukkitUtil.setColors(MyPetLocales.getString("Message.Skill.Inventory.Creative", myPet.getOwner())));
                return false;
            }
            if (MobArena.isInMobArena(myPet.getOwner()) ||
                    Minigames.isInMinigame(myPet.getOwner()) ||
                    BattleArena.isInBattleArena(myPet.getOwner()) ||
                    PvPArena.isInPvPArena(myPet.getOwner()) ||
                    MyHungerGames.isInHungerGames(myPet.getOwner()) ||
                    SurvivalGames.isInSurvivalGames(myPet.getOwner()))
            {
                myPet.sendMessageToOwner(MyPetBukkitUtil.setColors(MyPetLocales.getString("Message.NotAllowedHere", myPet.getOwner())).replace("%petname%", myPet.getPetName()));
                return false;
            }
            if (!myPet.getLocation().getBlock().isLiquid())
            {
                inv.setName(myPet.getPetName());
                openInventory(myPet.getOwner().getPlayer());
                return true;
            }
            else
            {
                myPet.sendMessageToOwner(MyPetBukkitUtil.setColors(MyPetLocales.getString("Message.Skill.Inventory.Swimming", myPet.getOwner()).replace("%petname%", myPet.getPetName())));
                return false;
            }
        }
        else
        {
            myPet.sendMessageToOwner(MyPetBukkitUtil.setColors(MyPetLocales.getString("Message.Skill.Inventory.NotAvailable", myPet.getOwner())).replace("%petname%", myPet.getPetName()));
            return false;
        }
    }

    public void openInventory(Player p)
    {
        EntityPlayer eh = ((CraftPlayer) p).getHandle();
        eh.openContainer(inv);
    }

    public void closeInventory()
    {
        inv.close();
    }

    public void load(CompoundTag compound)
    {
        inv.load(compound);
    }

    public CompoundTag save()
    {
        CompoundTag nbtTagCompound = new CompoundTag(getName(), new CompoundMap());
        inv.save(nbtTagCompound);
        return nbtTagCompound;
    }

    public boolean isActive()
    {
        return rows > 0;
    }

    public boolean dropOnDeath()
    {
        return dropOnDeath;
    }

    @Override
    public ISkillInstance cloneSkill()
    {
        Inventory newSkill = new Inventory(this.isAddedByInheritance());
        newSkill.setProperties(getProperties());
        return newSkill;
    }
}