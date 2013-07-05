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

package de.Keyle.MyPet.util;

import de.Keyle.MyPet.MyPetPlugin;
import de.Keyle.MyPet.entity.types.InactiveMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPet.PetState;
import de.Keyle.MyPet.entity.types.MyPetList;
import de.Keyle.MyPet.util.locale.MyPetLocales;
import de.Keyle.MyPet.util.logger.DebugLogger;
import de.Keyle.nbt.*;
import net.minecraft.server.v1_6_R1.EntityHuman;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;


import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class MyPetPlayer implements IScheduler, NBTStorage, Serializable
{
    private static List<MyPetPlayer> playerList = new ArrayList<MyPetPlayer>();

    private String playerName;
    private String lastLanguage = "en_US";

    private boolean donator = false;
    private boolean captureHelperMode = false;
    private boolean autoRespawn = false;
    private int autoRespawnMin = 1;
    private Map<String, UUID> petWorlds = new HashMap<String, UUID>();
    private CompoundTag extendedInfo = new CompoundTag("ExtendedInfo", new CompoundMap());

    private MyPetPlayer(String playerName)
    {
        this.playerName = playerName;
        checkForDonation();
    }

    public String getName()
    {
        return playerName;
    }

    public boolean hasCustomData()
    {
        if (autoRespawn || autoRespawnMin != 1)
        {
            return true;
        }
        else if (captureHelperMode)
        {
            return true;
        }
        else if (extendedInfo.getValue().size() > 0)
        {
            return true;
        }
        else if (petWorlds.size() > 0)
        {
            return true;
        }
        return false;
    }

    // Custom Data -----------------------------------------------------------------

    public void setAutoRespawnEnabled(boolean flag)
    {
        autoRespawn = flag;
    }

    public boolean hasAutoRespawnEnabled()
    {
        return autoRespawn;
    }

    public void setAutoRespawnMin(int value)
    {
        autoRespawnMin = value;
    }

    public int getAutoRespawnMin()
    {
        return autoRespawnMin;
    }

    public boolean isCaptureHelperActive()
    {
        return captureHelperMode;
    }

    public void setCaptureHelperActive(boolean captureHelperMode)
    {
        this.captureHelperMode = captureHelperMode;
    }

    public void setMyPetForWorldGroup(String worldGroup, UUID myPetUUID)
    {
        if (worldGroup == null || worldGroup.equals(""))
        {
            return;
        }
        if (myPetUUID == null)
        {
            petWorlds.remove(worldGroup);
        }
        else
        {
            petWorlds.put(worldGroup, myPetUUID);
        }
    }

    public UUID getMyPetForWorldGroup(String worldGroup)
    {
        return petWorlds.get(worldGroup);
    }

    public String getWorldGroupForMyPet(UUID petUUID)
    {
        for (String worldGroup : petWorlds.keySet())
        {
            if (petWorlds.get(worldGroup).equals(petUUID))
            {
                return worldGroup;
            }
        }
        return null;
    }

    public boolean hasMyPetInWorldGroup(String worldGroup)
    {
        return petWorlds.containsKey(worldGroup);
    }

    public boolean hasInactiveMyPetInWorldGroup(String worldGroup)
    {
        for (InactiveMyPet inactiveMyPet : getInactiveMyPets())
        {
            if (inactiveMyPet.getWorldGroup().equals(worldGroup))
            {
                return true;
            }
        }
        return false;
    }

    public void setExtendedInfo(CompoundTag compound)
    {
        if (extendedInfo.getValue().size() == 0)
        {
            extendedInfo = compound;
        }
    }

    public void addExtendedInfo(String key, Tag<?> tag)
    {
        extendedInfo.getValue().put(key, tag);
    }

    public Tag<?> getExtendedInfo(String key)
    {
        if (extendedInfo.getValue().containsKey(key))
        {
            return extendedInfo.getValue().get(key);
        }
        return null;
    }

    public CompoundTag getExtendedInfo()
    {
        return extendedInfo;
    }

    // -----------------------------------------------------------------------------

    public boolean isOnline()
    {
        return getPlayer() != null && getPlayer().isOnline();
    }

    public boolean isDonator()
    {
        return donator;
    }

    public void checkForDonation()
    {
        if (donator || !MyPetConfiguration.DONATOR_EFFECT)
        {
            return;
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(MyPetPlugin.getPlugin(), new Runnable()
        {
            public void run()
            {
                try
                {
                    // Check whether this player has donated for the MyPet project
                    // returns 1 for yes and 0 for no
                    // no data will be saved
                    String donation = MyPetUtil.readUrlContent("http://donation.keyle.de/donated.php?userid=" + playerName);
                    if (donation.equals("1"))
                    {
                        donator = true;
                        DebugLogger.info(playerName + " is a donator! Thanks " + playerName + " =)");
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }, 60L);
    }

    public String getLanguage()
    {
        if (isOnline())
        {
            lastLanguage = MyPetBukkitUtil.getPlayerLanguage(getPlayer());
        }
        return lastLanguage;
    }

    public boolean isMyPetAdmin()
    {
        return isOnline() && MyPetPermissions.has(getPlayer(), "MyPet.admin", false);
    }

    public boolean hasMyPet()
    {
        return MyPetList.hasMyPet(playerName);
    }

    public MyPet getMyPet()
    {
        return MyPetList.getMyPet(playerName);
    }

    public boolean hasInactiveMyPets()
    {
        return MyPetList.hasInactiveMyPets(playerName);
    }

    public InactiveMyPet getInactiveMyPet(UUID petUUID)
    {
        for (InactiveMyPet inactiveMyPet : MyPetList.getInactiveMyPets(playerName))
        {
            if (inactiveMyPet.getUUID().equals(petUUID))
            {
                return inactiveMyPet;
            }
        }
        return null;
    }

    public InactiveMyPet[] getInactiveMyPets()
    {
        return MyPetList.getInactiveMyPets(playerName);
    }

    public Player getPlayer()
    {
        return Bukkit.getServer().getPlayer(playerName);
    }

    public static MyPetPlayer getMyPetPlayer(String name)
    {
        for (MyPetPlayer myPetPlayer : playerList)
        {
            if (myPetPlayer.getName().equals(name))
            {
                return myPetPlayer;
            }
        }
        MyPetPlayer myPetPlayer = new MyPetPlayer(name);
        playerList.add(myPetPlayer);
        return myPetPlayer;
    }

    public static MyPetPlayer getMyPetPlayer(Player player)
    {
        return MyPetPlayer.getMyPetPlayer(player.getName());
    }

    public static boolean isMyPetPlayer(String name)
    {
        for (MyPetPlayer myPetPlayer : playerList)
        {
            if (myPetPlayer.getName().equals(name))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isMyPetPlayer(Player player)
    {
        for (MyPetPlayer myPetPlayer : playerList)
        {
            if (myPetPlayer.equals(player))
            {
                return true;
            }
        }
        return false;
    }

    public static MyPetPlayer[] getMyPetPlayers()
    {
        MyPetPlayer[] playerArray = new MyPetPlayer[playerList.size()];
        int playerCounter = 0;
        for (MyPetPlayer player : playerList)
        {
            playerArray[playerCounter++] = player;
        }
        return playerArray;
    }

    @Override
    public CompoundTag save()
    {
        CompoundTag playerNBT = new CompoundTag(getName(), new CompoundMap());

        playerNBT.getValue().put("Name", new StringTag("Name", getName()));
        playerNBT.getValue().put("AutoRespawn", new ByteTag("AutoRespawn", hasAutoRespawnEnabled()));
        playerNBT.getValue().put("AutoRespawnMin", new IntTag("AutoRespawnMin", getAutoRespawnMin()));
        playerNBT.getValue().put("AutoRespawnMin2", new IntTag("AutoRespawnMin2", getAutoRespawnMin()));
        playerNBT.getValue().put("ExtendedInfo", getExtendedInfo());
        playerNBT.getValue().put("CaptureMode", new ByteTag("CaptureMode", isCaptureHelperActive()));

        CompoundTag multiWorldCompound = new CompoundTag("MultiWorld", new CompoundMap());
        for (String worldGroupName : petWorlds.keySet())
        {
            multiWorldCompound.getValue().put(worldGroupName, new StringTag(worldGroupName, petWorlds.get(worldGroupName).toString()));
        }
        playerNBT.getValue().put("MultiWorld", multiWorldCompound);

        return playerNBT;
    }

    @Override
    public void load(CompoundTag myplayerNBT)
    {
        if (myplayerNBT.getValue().containsKey("AutoRespawn"))
        {
            setAutoRespawnEnabled(((ByteTag) myplayerNBT.getValue().get("AutoRespawn")).getBooleanValue());
        }
        if (myplayerNBT.getValue().containsKey("AutoRespawnMin"))
        {
            setAutoRespawnMin(((IntTag) myplayerNBT.getValue().get("AutoRespawnMin")).getValue());
        }
        if (myplayerNBT.getValue().containsKey("CaptureMode"))
        {
            if (myplayerNBT.getValue().get("CaptureMode").getType() == TagType.TAG_STRING)
            {
                if (!((StringTag) myplayerNBT.getValue().get("CaptureMode")).getValue().equals("Deactivated"))
                {
                    setCaptureHelperActive(true);
                }
            }
            else if (myplayerNBT.getValue().get("CaptureMode").getType() == TagType.TAG_BYTE)
            {
                setCaptureHelperActive(((ByteTag) myplayerNBT.getValue().get("CaptureMode")).getBooleanValue());
            }
        }
        if (myplayerNBT.getValue().containsKey("LastActiveMyPetUUID"))
        {
            String lastActive = ((StringTag) myplayerNBT.getValue().get("LastActiveMyPetUUID")).getValue();
            if (!lastActive.equalsIgnoreCase(""))
            {
                UUID lastActiveUUID = UUID.fromString(lastActive);
                World newWorld = Bukkit.getServer().getWorlds().get(0);
                MyPetWorldGroup lastActiveGroup = MyPetWorldGroup.getGroup(newWorld.getName());
                this.setMyPetForWorldGroup(lastActiveGroup.getName(), lastActiveUUID);
            }
        }
        if (myplayerNBT.getValue().containsKey("ExtendedInfo"))
        {
            setExtendedInfo((CompoundTag) myplayerNBT.getValue().get("ExtendedInfo"));
        }
        if (myplayerNBT.getValue().containsKey("MultiWorld"))
        {
            CompoundMap map = ((CompoundTag) myplayerNBT.getValue().get("MultiWorld")).getValue();
            for (String worldGroupName : map.keySet())
            {
                String petUUID = ((StringTag) map.get(worldGroupName)).getValue();
                setMyPetForWorldGroup(worldGroupName, UUID.fromString(petUUID));
            }
        }
    }

    public void schedule()
    {
        if (!isOnline())
        {
            return;
        }
        if (hasMyPet())
        {
            MyPet myPet = getMyPet();
            if (myPet.getStatus() == PetState.Here)
            {
                if (myPet.getLocation().getWorld() != this.getPlayer().getLocation().getWorld() || myPet.getLocation().distance(this.getPlayer().getLocation()) > 75)
                {
                    if (!myPet.getCraftPet().canMove())
                    {
                        myPet.removePet(true);
                        myPet.sendMessageToOwner(MyPetBukkitUtil.setColors(MyPetLocales.getString("Message.Despawn", getLanguage())).replace("%petname%", myPet.getPetName()));
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (obj instanceof Player)
        {
            Player player = (Player) obj;
            return playerName.equals(player.getName());
        }
        else if (obj instanceof OfflinePlayer)
        {
            return ((OfflinePlayer) obj).getName().equals(playerName);
        }
        else if (obj instanceof EntityHuman)
        {
            EntityHuman entityHuman = (EntityHuman) obj;
            return playerName.equals(entityHuman.getName());
        }
        else if (obj instanceof AnimalTamer)
        {
            return ((AnimalTamer) obj).getName().equals(playerName);
        }
        else if (obj instanceof MyPetPlayer)
        {
            return this == obj;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "MyPetPlayer{name=" + playerName + "}";
    }
}