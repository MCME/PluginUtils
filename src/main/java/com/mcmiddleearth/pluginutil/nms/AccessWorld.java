package com.mcmiddleearth.pluginutil.nms;

public class AccessWorld {

    public static Object createEntity(Object nmsWorld, Object nbt) throws ClassNotFoundException {
        Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("nbt.NBTTagCompound"),
                NMSUtil.getNMSClass("world.level.World")};
        //static Optional<Entity> a(NBTTagCompound, World)
        return NMSUtil.invokeNMS("world.entity.EntityTypes", "a", argsClasses, null, nbt, nmsWorld);
    }

    public static Object getEntityId(Object nmsEntity) {
        return NMSUtil.invokeNMS("world.entity.Entity","bp",null, nmsEntity);
    }

    public static Object writeEntityNBT(Object nmsEntity, Object nbt) {
        return NMSUtil.invokeNMS("world.entity.Entity","f",null, nmsEntity,nbt);
    }

    public static Object getTileEntityBlockPosition(Object nbt) throws ClassNotFoundException {
        return NMSUtil.invokeNMS("world.level.block.entity.TileEntity","c",
                new Class[]{NMSUtil.getNMSClass("nbt.NBTTagCompound")},null, nbt);
    }

    public static Object getChunkAtWorldCoords(Object nmsWorld, Object blockPosition) {
        return NMSUtil.invokeNMS("world.level.World", "l"/*"getChunkAtWorldCoords"*/,
                new Class[]{blockPosition.getClass()}, nmsWorld, blockPosition);
    }

    public static Object getBlockState(Object chunk, Object blockPosition) {
        return NMSUtil.invokeNMS("world.level.chunk.Chunk", "a_"/*"getType"*/,
                new Class[]{blockPosition.getClass()}, chunk, blockPosition);
    }

    public static Object createTileEntity(Object blockPosition, Object iBlockState, Object nbt) throws ClassNotFoundException {
        Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("core.BlockPosition"),
                NMSUtil.getNMSClass("world.level.block.state.IBlockData"),
                NMSUtil.getNMSClass("nbt.NBTTagCompound")};
        return NMSUtil.invokeNMS("world.level.block.entity.TileEntity", "a"/*"create"*/,
                argsClasses, null, blockPosition, iBlockState, nbt/*,nmsWorld*/);
    }

    public static void setTileEntity(Object chunk, Object entity) throws ClassNotFoundException {
        NMSUtil.invokeNMS("world.level.chunk.Chunk", "a"/*"setTileEntity"*/,
                new Class[]{NMSUtil.getNMSClass("world.level.block.entity.TileEntity")},
                chunk, entity);

    }
}
