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

package de.Keyle.MyPet.skill.skills.info;

import de.Keyle.MyPet.skill.MyPetSkillTreeSkill;
import de.Keyle.MyPet.skill.SkillName;
import de.Keyle.MyPet.skill.SkillProperties;
import de.Keyle.MyPet.skill.SkillProperties.NBTdatatypes;
import de.Keyle.MyPet.util.MyPetUtil;
import org.spout.nbt.DoubleTag;
import org.spout.nbt.IntTag;
import org.spout.nbt.StringTag;

import java.io.InputStream;
import java.io.Serializable;

@SkillName("Ranged")
@SkillProperties(
        parameterNames = {"damage_double", "addset_damage", "projectile"},
        parameterTypes = {NBTdatatypes.Double, NBTdatatypes.String, NBTdatatypes.String},
        parameterDefaultValues = {"1.0", "add", "Arrow"})
public class RangedInfo extends MyPetSkillTreeSkill implements ISkillInfo, Serializable
{
    private static String defaultHTML = null;

    protected double damage = 0;

    public enum Projectiles
    {
        Arrow, Snowball, LargeFireball, SmallFireball, WitherSkull
    }

    protected Projectiles selectedProjectile = Projectiles.Arrow;

    public RangedInfo(boolean addedByInheritance)
    {
        super(addedByInheritance);
    }

    public String getHtml()
    {
        if (defaultHTML == null)
        {
            InputStream htmlStream = getClass().getClassLoader().getResourceAsStream("html/skills/" + getName() + ".html");
            if (htmlStream == null)
            {
                htmlStream = this.getClass().getClassLoader().getResourceAsStream("html/skills/_default.html");
                if (htmlStream == null)
                {
                    return "NoSkillPropertieViewNotFoundError";
                }
            }
            defaultHTML = MyPetUtil.convertStreamToString(htmlStream).replace("#Skillname#", getName());
        }

        String html = defaultHTML;
        if (getProperties().getValue().containsKey("damage"))
        {
            int damage = ((IntTag) getProperties().getValue().get("damage")).getValue();
            getProperties().getValue().remove("damage");
            DoubleTag doubleTag = new DoubleTag("damage_double", damage);
            getProperties().getValue().put("damage_double", doubleTag);
        }
        if (getProperties().getValue().containsKey("damage_double"))
        {
            int damage = ((IntTag) getProperties().getValue().get("damage_double")).getValue();
            html = html.replace("value=\"0.0\"", "value=\"" + damage + "\"");
            if (getProperties().getValue().containsKey("addset_damage"))
            {
                if (((StringTag) getProperties().getValue().get("addset_damage")).getValue().equals("set"))
                {
                    html = html.replace("name=\"addset_damage\" value=\"add\" checked", "name=\"addset_damage\" value=\"add\"");
                    html = html.replace("name=\"addset_damage\" value=\"set\"", "name=\"addset_damage\" value=\"set\" checked");
                }
            }
        }
        if (getProperties().getValue().containsKey("projectile"))
        {
            String projectile = ((StringTag) getProperties().getValue().get("projectile")).getValue();
            if (projectile.equalsIgnoreCase("Arrow"))
            {
                html = html.replace("<option value=\"Arrow\">Arrow</option>", "<option value=\"Arrow\" selected>Arrow</option>");
            }
            else if (projectile.equalsIgnoreCase("Snowball"))
            {
                html = html.replace("<option value=\"Snowball\">Snowball</option>", "<option value=\"Snowball\" selected>Snowball</option>");
            }
            else if (projectile.equalsIgnoreCase("LargeFireball"))
            {
                html = html.replace("<option value=\"LargeFireball\">Large Fireball</option>", "<option value=\"LargeFireball\" selected>Large Fireball</option>");
            }
            else if (projectile.equalsIgnoreCase("Snowball"))
            {
                html = html.replace("<option value=\"SmallFireball\">Small Fireball</option>", "<option value=\"SmallFireball\" selected>Small Fireball</option>");
            }
            else if (projectile.equalsIgnoreCase("Snowball"))
            {
                html = html.replace("<option value=\"WitherSkull\">Wither Skull</option>", "<option value=\"WitherSkull\" selected>Wither Skull</option>");
            }
        }
        return html;
    }

    public ISkillInfo cloneSkill()
    {
        RangedInfo newSkill = new RangedInfo(this.isAddedByInheritance());
        newSkill.setProperties(getProperties());
        return newSkill;
    }
}
