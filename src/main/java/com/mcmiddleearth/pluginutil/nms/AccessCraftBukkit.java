package com.mcmiddleearth.pluginutil.nms;

import com.mcmiddleearth.pluginutil.PluginUtilsPlugin;
import com.mcmiddleearth.pluginutil.developer.Debugable;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.logging.Logger;

public class AccessCraftBukkit {

    public static Object getWorldServer(World world) {
        return ((CraftWorld)world).getHandle();
        //return NMSUtil.invokeCraftBukkit("CraftWorld", "getHandle", null, world);
    }

    @SuppressWarnings("rawtypes")
    public static Object getSnapshotNBT(BlockState state) {
        return ((CraftBlockEntityState)state).getSnapshotNBT();
        /*/return NMSUtil.invokeCraftBukkit("block.CraftBlockEntityState","getSnapshotNBT",
                null, state);*/
    }

    public static Object getNMSEntity(Entity entity) {
        return ((CraftEntity)entity).getHandle();
        /*return NMSUtil.invokeCraftBukkit("entity.CraftEntity", "getHandle",
                null, entity);*/
    }

    public static void setPlayerFirstPlayed(Player player, long firstPlayed) {
        ((CraftPlayer)player).setFirstPlayed(firstPlayed);
    }

}
