package com.mcmiddleearth.pluginutil.nms;

import net.minecraft.nbt.*;

import java.io.*;

public class AccessNBT {
    public static void writeNBTToStream(Object nbt, DataOutput out) throws ClassNotFoundException, IOException {
        NbtIo.writeCompressed((CompoundTag) nbt, (OutputStream) out);

       /* Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("nbt.NBTTagCompound"), DataOutput.class};

//Logger.getGlobal().info("saving nbt TileEntity: "+nbt.toString());
        NMSUtil.invokeNMS("nbt.NBTCompressedStreamTools","a",argsClasses,null,nbt, out);*/
    }

    public static Object readNBTFromStream(DataInput in) throws ClassNotFoundException, IOException {
        return NbtIo.readCompressed((InputStream) in, new NbtAccounter(10000,100));
        /*Class[] argsClasses = new Class[]{DataInput.class, NMSUtil.getNMSClass("nbt.NBTReadLimiter")};
        return NMSUtil.invokeNMS("nbt.NBTCompressedStreamTools", "a", argsClasses, null,
                in, NMSUtil.getNMSField("nbt.NBTReadLimiter", "a", null));*/
    }

    public static void setString(Object nbt, String key, Object value) {
        ((CompoundTag)nbt).putString(key, (String) value);
        /*NMSUtil.invokeNMS("nbt.NBTTagCompound","a",
                new Class[]{String.class, String.class} , nbt,key, value);*/
    }

    public static void setInt(Object nbt, String key, Object value) {
        ((CompoundTag)nbt).putInt(key, (Integer) value);
        /*NMSUtil.invokeNMS("nbt.NBTTagCompound", "a"/*setInt*, new Class[]{String.class, int.class},
                nbt, key, value);*/

    }

    public static void setNBTBase(Object nbt, String key, Object value) throws ClassNotFoundException {
        ((CompoundTag)nbt).put(key, (Tag) value);
        /*NMSUtil.invokeNMS("nbt.NBTTagCompound", "a"/*"set"*,
                new Class[]{String.class, NMSUtil.getNMSClass("nbt.NBTBase")},
                nbt, key, value);*/
   }
    public static boolean hasKey(Object tag, String key) throws ClassNotFoundException {
        return ((CompoundTag)tag).contains(key);
        /*if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
            return (boolean) NMSUtil.invokeNMS("nbt.NBTTagCompound","e",
                    new Class[]{String.class},tag,key);
        }
        return false;*/
    }

    public static Object getCompound(Object tag, String key) throws ClassNotFoundException {
        return ((CompoundTag)tag).getCompound(key);
        /*if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
            Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","p"/*getCompound*,
                    new Class[]{String.class},tag,key);
            return NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(result)?result:null;
        }
        return null;*/
    }

    /*public static Object getTagList(Object tag, String key) throws ClassNotFoundException {
        return ((CompoundTag)tag).get
        if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
            Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","c"/*get*,
                    new Class[]{String.class},tag,key);
            return NMSUtil.getNMSClass("nbt.NBTTagList").isInstance(result)?result:null;
        }
        return null;
    }*/

    public static Object getNBTBaseList(Object nbt, String key) {
        return ((CompoundTag)nbt).getList(key, 6);
        /*return NMSUtil.invokeNMS("nbt.NBTTagCompound", "c"/*"getList"*,
                new Class[]{String.class, int.class},
                nbt, key, 6); // 6 = content type double > NBTBase*/
    }

    public static Object getFloatList(Object nbt, String key) {
        return ((CompoundTag)nbt).getList(key, 5);
        /*return NMSUtil.invokeNMS("nbt.NBTTagCompound", "c"/*"getList"*,
                new Class[]{String.class, int.class},
                nbt, key, 5); // 5= content type float > NBTBase;*/
    }

    public static float getFloatFromList(Object list, int index) {
        return ((ListTag)list).getFloat(index);
        /*return (float) NMSUtil.invokeNMS("nbt.NBTTagList", "i",
                new Class[]{int.class}, list, index);*/
    }

    public static double getDoubleFromList(Object list, int index) {
        return ((ListTag)list).getDouble(index);
        /*Class[] argsClassesC = new Class[]{int.class};
        return (double) NMSUtil.invokeNMS("nbt.NBTTagList", "h",
                argsClassesC, list, index);*/
    }

    public static Integer getInt(Object tag, String key) throws ClassNotFoundException {
        return ((CompoundTag)tag).getInt(key);
        /*if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
            Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","h"/*getInt*,
                    new Class[]{String.class},tag,key);
            return result instanceof Integer?(Integer)result:null;
        }
        return null;*/
    }

    public static Short getShort(Object tag, String key) throws ClassNotFoundException {
        return ((CompoundTag)tag).getShort(key);
        /*if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
            Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","g"/*getShort*,
                    new Class[]{String.class},tag,key);
            return result instanceof Short?(Short)result:null;
        }
        return null;*/
    }

    public static Long getLong(Object tag, String key) throws ClassNotFoundException {
        return ((CompoundTag)tag).getLong(key);
        /*if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
            Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","i"/*getLong*,
                    new Class[]{String.class},tag,key);
            return result instanceof Long?(Long)result:null;
        }
        return null;*/
    }

    public static Byte getByte(Object tag, String key) throws ClassNotFoundException {
        return ((CompoundTag)tag).getByte(key);
        /*if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
            Object result = NMSUtil.invokeNMS("nbt.NBTTagCompound", "f"/*getByte*,
                    new Class[]{String.class}, tag, key);
            return result instanceof Byte ? (Byte) result : null;
        }
        return null;*/
    }

    public static String getString(Object tag, String key) throws ClassNotFoundException {
        return ((CompoundTag)tag).getString(key);
        /*if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
            Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","l"/*getString*,
                    new Class[]{String.class},tag,key);
            return result instanceof String?(String)result:null;
        }
        return null;*/
    }

    public static Float getFloat(Object tag, String key) throws ClassNotFoundException {
        return ((CompoundTag)tag).getFloat(key);
        /*if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
            Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","j"/*getFloat*,
                    new Class[]{String.class},tag,key);
            return result instanceof Float?(Float)result:null;
        }
        return null;*/
    }

    public static Double getDouble(Object tag, String key) throws ClassNotFoundException {
        return ((CompoundTag)tag).getDouble(key);
        /*if(NMSUtil.getNMSClass("nbt.NBTTagCompound").isInstance(tag)) {
            Object result =  NMSUtil.invokeNMS("nbt.NBTTagCompound","k"/*getDouble*,
                    new Class[]{String.class},tag,key);
            return result instanceof Double?(Double)result:null;
        }
        return null;*/
    }

    public static String asString(Object tag) throws ClassNotFoundException {
        return ((Tag)tag).toString();
        /*if(NMSUtil.getNMSClass("nbt.NBTBase").isInstance(tag)) {
            return (String) NMSUtil.invokeNMS("nbt.NBTBase","toString",new Class[]{},tag);
        }
        return "";*/
    }

    public static Object createNBTTagLong(long value) {
        return LongTag.valueOf(value);
        //return NMSUtil.invokeNMS("nbt.NBTTagLong","a",new Class[]{long.class},null,value);
    }

    public static Object createNBTTagInt(int value) {
        return IntTag.valueOf(value);
        //return NMSUtil.invokeNMS("nbt.NBTTagInt","a",new Class[]{int.class},null,value);
    }

    public static Object createNBTTagByte(byte value) {
        return ByteTag.valueOf(value);
        //return NMSUtil.invokeNMS("nbt.NBTTagByte","a",new Class[]{byte.class},null,value);
    }

    public static Object createNBTTagFloat(float value) {
        return FloatTag.valueOf(value);
        //return NMSUtil.invokeNMS("nbt.NBTTagFloat","a",new Class[]{float.class},null,value);
    }

    public static Object createNBTTagDouble(double value) {
        return DoubleTag.valueOf(value);
        //return NMSUtil.invokeNMS("nbt.NBTTagDouble","a",new Class[]{double.class},null,value);
    }

    public static Object createNBTCompound() {
        return new CompoundTag();
        //return NMSUtil.createNMSObject("nbt.NBTTagCompound",null);
    }

    public static Object createNBTTagIntArray(int[] values) {
        return new IntArrayTag(values);
        /*return NMSUtil.createNMSObject("nbt.NBTTagIntArray",new Class[]{int[].class}, (Object) values);*/
    }

    public static void setNBTBaseInList(Object list, int index, Object nbtBase) throws ClassNotFoundException {
        ((ListTag)list).setTag(index, (Tag) nbtBase);
        /*Class[] argsClassesA = new Class[]{int.class,
                NMSUtil.getNMSClass("nbt.NBTBase")};
        NMSUtil.invokeNMS("nbt.NBTTagList", "a", argsClassesA, list,
                index, nbtBase);*/
    }

    public static int size(Object list) {
        return ((ListTag)list).size();
    }
}
