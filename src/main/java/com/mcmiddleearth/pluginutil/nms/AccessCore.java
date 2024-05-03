package com.mcmiddleearth.pluginutil.nms;

import org.bukkit.util.Vector;

public class AccessCore {


    public static Object shiftBlockPosition(Object blockposition, int shiftX, int shiftY, int shiftZ) {
        Class[] argsClasses = new Class[]{int.class, int.class, int.class};
        return NMSUtil.invokeNMS("core.BaseBlockPosition", "c", argsClasses, blockposition,
                shiftX, shiftY, shiftZ);
    }

    public static Vector toVector(Object blockPosition) {
        return new Vector((int) NMSUtil.invokeNMS("core.BaseBlockPosition","u"/*"getX"*/,null,blockPosition),
                (int) NMSUtil.invokeNMS("core.BaseBlockPosition","v"/*"getY"*/,null,blockPosition),
                (int) NMSUtil.invokeNMS("core.BaseBlockPosition","w"/*getZ"*/,null,blockPosition));
    }

    public static Object toBlockPosition(Vector vector) {
        return NMSUtil.createNMSObject("core.BlockPosition",
                new Class[]{int.class,int.class,int.class},
                vector.getBlockX(),
                vector.getBlockY(),
                vector.getBlockZ());
    }

    public static Object getBlockPositionX(Object blockPosition) {
        return NMSUtil.invokeNMS("core.BaseBlockPosition", "u"/*"getX"*/, null, blockPosition);
    }

    public static Object getBlockPositionY(Object blockPosition) {
        return NMSUtil.invokeNMS("core.BaseBlockPosition", "v"/*"getY"*/, null, blockPosition);
    }

    public static Object getBlockPositionZ(Object blockPosition) {
        return NMSUtil.invokeNMS("core.BaseBlockPosition", "w"/*"getZ"*/, null, blockPosition);
    }
}
