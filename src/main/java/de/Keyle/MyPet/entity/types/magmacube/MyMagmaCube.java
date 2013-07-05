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

package de.Keyle.MyPet.entity.types.magmacube;

import de.Keyle.MyPet.entity.MyPetInfo;
import de.Keyle.MyPet.entity.types.IMyPetSlimeSize;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPetType;
import de.Keyle.MyPet.util.MyPetPlayer;
import org.bukkit.ChatColor;
import de.Keyle.nbt.CompoundTag;
import de.Keyle.nbt.IntTag;

import static org.bukkit.Material.REDSTONE;

@MyPetInfo(food = {REDSTONE})
public class MyMagmaCube extends MyPet implements IMyPetSlimeSize
{
    protected int size = 1;

    public MyMagmaCube(MyPetPlayer petOwner)
    {
        super(petOwner);
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int value)
    {
        if (status == PetState.Here)
        {
            ((EntityMyMagmaCube) getCraftPet().getHandle()).setSize(value);
        }
        this.size = value;
    }

    @Override
    public CompoundTag getExtendedInfo()
    {
        CompoundTag info = super.getExtendedInfo();
        info.getValue().put("Size", new IntTag("Size", getSize()));
        return info;
    }

    @Override
    public void setExtendedInfo(CompoundTag info)
    {
        if (info.getValue().containsKey("Size"))
        {
            setSize(((IntTag) info.getValue().get("Size")).getValue());
        }
    }

    @Override
    public MyPetType getPetType()
    {
        return MyPetType.MagmaCube;
    }

    @Override
    public String toString()
    {
        return "MyMagmaCube{owner=" + getOwner().getName() + ", name=" + ChatColor.stripColor(petName) + ", exp=" + experience.getExp() + "/" + experience.getRequiredExp() + ", lv=" + experience.getLevel() + ", status=" + status.name() + ", skilltree=" + (skillTree != null ? skillTree.getName() : "-") + ", size=" + getSize() + "}";
    }
}