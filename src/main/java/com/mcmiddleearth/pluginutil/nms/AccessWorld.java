package com.mcmiddleearth.pluginutil.nms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;

public class AccessWorld {

    public static Object createEntity(Object nmsWorld, Object nbt) throws ClassNotFoundException {
        // EntityType.loadEntityRecursive no longer takes a CompoundTag; wrap it in a ValueInput
        // (the read counterpart of the TagValueOutput used in writeEntityNBT).
        HolderLookup.Provider provider = ((CraftServer) Bukkit.getServer()).getServer().registries().compositeAccess();
        ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, provider, (CompoundTag) nbt);
        return EntityType.loadEntityRecursive(input, (ServerLevel) nmsWorld, EntitySpawnReason.LOAD, EntityProcessor.NOP);
    }

    public static Object getEntityType(Object nmsEntity) {
        String[] descriptionId = ((Entity)nmsEntity).getType().getDescriptionId().split("\\.");
        return descriptionId[descriptionId.length-1];
    }

    public static Object writeEntityNBT(Object nmsEntity, Object nbt) {
        // Entity.saveWithoutId now takes a ValueOutput (returns void) instead of a CompoundTag.
        // Wrap our CompoundTag so the entity data still lands in it, then return the populated tag.
        HolderLookup.Provider provider = ((CraftServer) Bukkit.getServer()).getServer().registries().compositeAccess();
        TagValueOutput out = TagValueOutput.createWrappingWithContext(ProblemReporter.DISCARDING, provider, (CompoundTag) nbt);
        ((Entity) nmsEntity).saveWithoutId(out);
        return nbt;
    }

    public static Object getTileEntityBlockPosition(Object nbt) throws ClassNotFoundException {
        // BlockEntity.getPosFromTag now requires a ChunkPos we don't have here; the tile-entity NBT
        // still stores absolute x/y/z, so read them directly.
        CompoundTag tag = (CompoundTag) nbt;
        return new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0));
    }

    public static Object getChunkAtWorldCoords(Object nmsWorld, Object blockPosition) {
        return ((ServerLevel)nmsWorld).getChunk((BlockPos) blockPosition);
    }

    public static Object getBlockState(Object chunk, Object blockPosition) {
        return ((LevelChunk)chunk).getBlockState((BlockPos) blockPosition);
    }

    public static Object createTileEntity(Object blockPosition, Object iBlockState, Object nbt) throws ClassNotFoundException {
        HolderLookup.Provider provider = ((CraftServer)Bukkit.getServer()).getServer().registries().compositeAccess();
        return BlockEntity.loadStatic((BlockPos) blockPosition, (BlockState) iBlockState, (CompoundTag) nbt, provider);
    }

    public static void setTileEntity(Object chunk, Object entity) throws ClassNotFoundException {
        ((LevelChunk)chunk).setBlockEntity((BlockEntity) entity);
    }

 }
