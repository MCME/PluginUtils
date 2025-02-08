package com.mcmiddleearth.pluginutil.nms;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;

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
}
