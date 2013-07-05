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

@SkillName("HP")
@SkillProperties(
        parameterNames = {"hp_double", "addset_hp"},
        parameterTypes = {NBTdatatypes.Double, NBTdatatypes.String},
        parameterDefaultValues = {"1.0", "add"})
public class HPInfo extends MyPetSkillTreeSkill implements ISkillInfo, Serializable
{
    private static String defaultHTML = null;

    protected double hpIncrease = 0;

    public HPInfo(boolean addedByInheritance)
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
        if (getProperties().getValue().containsKey("hp"))
        {
            int hp = ((IntTag) getProperties().getValue().get("hp")).getValue();
            getProperties().getValue().remove("hp");
            DoubleTag doubleTag = new DoubleTag("hp_double", hp);
            getProperties().getValue().put("hp_double", doubleTag);
        }
        if (getProperties().getValue().containsKey("hp_double"))
        {
            double hp = ((DoubleTag) getProperties().getValue().get("hp_double")).getValue();
            html = html.replace("\"hp_double\" value=\"0.0\"", "\"hp_double\" value=\"" + hp + "\"");
            if (getProperties().getValue().containsKey("addset_hp"))
            {
                if (((StringTag) getProperties().getValue().get("addset_hp")).getValue().equals("set"))
                {
                    html = html.replace("name=\"addset_hp\" value=\"add\" checked", "name=\"addset_hp\" value=\"add\"");
                    html = html.replace("name=\"addset_hp\" value=\"set\"", "name=\"addset_hp\" value=\"set\" checked");
                }
            }
        }
        return html;
    }

    public ISkillInfo cloneSkill()
    {
        HPInfo newSkill = new HPInfo(this.isAddedByInheritance());
        newSkill.setProperties(getProperties());
        return newSkill;
    }
}
