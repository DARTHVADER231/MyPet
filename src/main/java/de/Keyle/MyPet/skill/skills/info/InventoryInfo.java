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
import de.Keyle.nbt.ByteTag;
import de.Keyle.nbt.IntTag;

import java.io.InputStream;
import java.io.Serializable;

@SkillName("Inventory")
@SkillProperties(parameterNames = {"add", "drop"},
        parameterTypes = {NBTdatatypes.Int, NBTdatatypes.Boolean},
        parameterDefaultValues = {"1", "false"})
public class InventoryInfo extends MyPetSkillTreeSkill implements ISkillInfo, Serializable
{
    private static String defaultHTML = null;

    protected int rows = 0;
    protected boolean dropOnDeath = false;

    public InventoryInfo(boolean addedByInheritance)
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
        if (getProperties().getValue().containsKey("add"))
        {
            int add = ((IntTag) getProperties().getValue().get("add")).getValue();
            html = html.replace("value=\"0\"", "value=\"" + add + "\"");
        }
        if (getProperties().getValue().containsKey("drop"))
        {
            if (!((ByteTag) getProperties().getValue().get("drop")).getBooleanValue())
            {
                html = html.replace("name=\"drop\" checked", "name=\"drop\"");
            }
        }
        return html;
    }

    public ISkillInfo cloneSkill()
    {
        InventoryInfo newSkill = new InventoryInfo(this.isAddedByInheritance());
        newSkill.setProperties(getProperties());
        return newSkill;
    }
}
