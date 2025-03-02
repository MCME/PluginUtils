package com.mcmiddleearth.pluginutil.nms;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class AccessServer {

    @SuppressWarnings("unchecked")
    public static void addFreshEntity(Object nmsWorld, Object entity) throws ClassNotFoundException {
        ((ServerLevel)nmsWorld).addFreshEntity((Entity) entity,CreatureSpawnEvent.SpawnReason.CUSTOM);
        /*Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("world.entity.Entity"),
                CreatureSpawnEvent.SpawnReason.CUSTOM.getClass()};
        //boolean addFreshEntity(Entity, SpawnReason)
        NMSUtil.invokeNMS("server.level.WorldServer", "addFreshEntity"/*"addEntity", argsClasses, nmsWorld, ((Optional) entity).get(),
                CreatureSpawnEvent.SpawnReason.CUSTOM);*/
    }

    public static void calcLight(Chunk chunk, List<Vector> positions) {
        ServerLevel nmsServer = ((CraftWorld)chunk.getWorld()).getHandle();
        LevelLightEngine nmsLightEngine = nmsServer.getLightEngine();
        for(Vector position: positions) {
            BlockPos nmsBlockPosition = new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ());
            nmsLightEngine.checkBlock(nmsBlockPosition);
        }
    }

    public static void calcLight(Location loc) {
        ServerLevel nmsServer = ((CraftWorld)loc.getWorld()).getHandle();
        LevelLightEngine nmsLightEngine = nmsServer.getLightEngine();
        BlockPos nmsBlockPosition = new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        nmsLightEngine.checkBlock(nmsBlockPosition);
    }

}
