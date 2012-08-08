/*
 * Copyright (C) 2011-2012 Keyle
 *
 * This file is part of MyPet
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
 * along with MyPet. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.entity.types.villager;

import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPetType;
import de.Keyle.MyPet.entity.types.ocelot.EntityMyOcelot;
import de.Keyle.MyPet.util.MyPetPlayer;
import net.minecraft.server.NBTTagCompound;

public class MyVillager extends MyPet
{
    private static int startHP = 10;

    int profession = 0;

    public MyVillager(MyPetPlayer Owner)
    {
        super(Owner);
        this.Name = "Villager";
    }

    public int getMaxHealth()
    {
        return startHP + (skillSystem.hasSkill("HP") ? skillSystem.getSkill("HP").getLevel() : 0);
    }

    public void setProfession(int color)
    {
        this.profession = color;
        if (Status == PetState.Here)
        {
            ((EntityMyOcelot) Pet.getHandle()).setCatType(color);
        }
    }

    public int getProfession()
    {
        return profession;
    }

    @Override
    public NBTTagCompound getExtendedInfo()
    {
        NBTTagCompound info = new NBTTagCompound("Info");
        info.setInt("Profession", profession);
        return info;
    }

    @Override
    public void setExtendedInfo(NBTTagCompound info)
    {
        setProfession(info.getInt("Profession"));
    }

    @Override
    public MyPetType getPetType()
    {
        return MyPetType.Villager;
    }

    @Override
    public String toString()
    {
        return "MyVillager{owner=" + getOwner().getName() + ", name=" + Name + ", exp=" + experience.getExp() + "/" + experience.getRequiredExp() + ", lv=" + experience.getLevel() + ", status=" + Status.name() + ", skilltree=" + skillTree.getName() + "}";
    }

    public static void setStartHP(int hp)
    {
        startHP = hp;
    }

    public static int getStartHP()
    {
        return startHP;
    }
}