package com.mcmiddleearth.pluginutil.nms;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;

public class AccessCraftBukkit {

    public static Object getWorldServer(World world) {
        return NMSUtil.invokeCraftBukkit("CraftWorld", "getHandle", null, world);
    }

    public static Object getSnapshotNBT(BlockState state) {
        return NMSUtil.invokeCraftBukkit("block.CraftBlockEntityState","getSnapshotNBT",
                null, state);
    }

    public static Object getNMSEntity(Entity entity) {
        return NMSUtil.invokeCraftBukkit("entity.CraftEntity", "getHandle",
                null, entity);
    }
}
