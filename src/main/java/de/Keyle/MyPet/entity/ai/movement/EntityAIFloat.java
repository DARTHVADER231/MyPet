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

package de.Keyle.MyPet.entity.ai.movement;

import de.Keyle.MyPet.entity.types.EntityMyPet;
import net.minecraft.server.v1_5_R1.EntityLiving;
import net.minecraft.server.v1_5_R1.PathfinderGoal;

public class EntityAIFloat extends PathfinderGoal
{
    private EntityLiving entityMyPet;

    public EntityAIFloat(EntityMyPet entityMyPet)
    {
        this.entityMyPet = entityMyPet;
        entityMyPet.getNavigation().e(true);
    }

    public boolean a()
    {
        return entityMyPet.world.getMaterial((int) entityMyPet.locX, (int) entityMyPet.locY, (int) entityMyPet.locZ).isLiquid();
    }

    public void e()
    {
        if (entityMyPet.aE().nextFloat() < 0.9D)
        {
            entityMyPet.motY += 0.05D;
        }
    }
}
