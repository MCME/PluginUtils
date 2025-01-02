package com.mcmiddleearth.pluginutil.nms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;

public class AccessWorld {

    public static Object createEntity(Object nmsWorld, Object nbt) throws ClassNotFoundException {
        return EntityType.loadEntityRecursive((CompoundTag) nbt, (ServerLevel) nmsWorld, EntitySpawnReason.LOAD, entity -> entity);
        /*Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("nbt.NBTTagCompound"),
                NMSUtil.getNMSClass("world.level.World")};
        //static Optional<Entity> a(NBTTagCompound, World)
        return NMSUtil.invokeNMS("world.entity.EntityTypes", "a", argsClasses, null, nbt, nmsWorld);*/
    }

    public static Object getEntityType(Object nmsEntity) {
        String[] descriptionId = ((Entity)nmsEntity).getType().getDescriptionId().split("\\.");
        return descriptionId[descriptionId.length-1];
        //return NMSUtil.invokeNMS("world.entity.Entity","bp",null, nmsEntity);
    }

    public static Object writeEntityNBT(Object nmsEntity, Object nbt) {
        return ((Entity) nmsEntity).saveWithoutId((CompoundTag) nbt);
        //return nmsEntity;
        //return NMSUtil.invokeNMS("world.entity.Entity","f",null, nmsEntity,nbt);
    }

    public static Object getTileEntityBlockPosition(Object nbt) throws ClassNotFoundException {
        return BlockEntity.getPosFromTag((CompoundTag) nbt);
        /*return NMSUtil.invokeNMS("world.level.block.entity.TileEntity","c",
                new Class[]{NMSUtil.getNMSClass("nbt.NBTTagCompound")},null, nbt);*/
    }

    public static Object getChunkAtWorldCoords(Object nmsWorld, Object blockPosition) {
        return ((ServerLevel)nmsWorld).getChunk((BlockPos) blockPosition);
        /*return NMSUtil.invokeNMS("world.level.World", "l"/*"getChunkAtWorldCoords"*,
                new Class[]{blockPosition.getClass()}, nmsWorld, blockPosition);*/
    }

    public static Object getBlockState(Object chunk, Object blockPosition) {
        return ((LevelChunk)chunk).getBlockState((BlockPos) blockPosition);
        /*return NMSUtil.invokeNMS("world.level.chunk.Chunk", "a_"/*"getType"*,
                new Class[]{blockPosition.getClass()}, chunk, blockPosition);*/
    }

    public static Object createTileEntity(Object blockPosition, Object iBlockState, Object nbt) throws ClassNotFoundException {
        HolderLookup.Provider provider = ((CraftServer)Bukkit.getServer()).getServer().registries().compositeAccess();
        return BlockEntity.loadStatic((BlockPos) blockPosition, (BlockState) iBlockState, (CompoundTag) nbt, provider);
        /*Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("core.BlockPosition"),
                NMSUtil.getNMSClass("world.level.block.state.IBlockData"),
                NMSUtil.getNMSClass("nbt.NBTTagCompound")};
        return NMSUtil.invokeNMS("world.level.block.entity.TileEntity", "a"/*"create"*,
                argsClasses, null, blockPosition, iBlockState, nbt/*,nmsWorld*);*/
    }

    public static void setTileEntity(Object chunk, Object entity) throws ClassNotFoundException {
        ((LevelChunk)chunk).setBlockEntity((BlockEntity) entity);
        /*NMSUtil.invokeNMS("world.level.chunk.Chunk", "a"/*"setTileEntity"*,
                new Class[]{NMSUtil.getNMSClass("world.level.block.entity.TileEntity")},
                chunk, entity);*/

    }
}
