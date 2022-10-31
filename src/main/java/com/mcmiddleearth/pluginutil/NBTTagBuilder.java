/*
 * Copyright (C) 2017 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.pluginutil;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Eriol_Eandur
 */
public class NBTTagBuilder {
    
    private Object tag;
    
    public NBTTagBuilder() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
        reset();
    }
    
    public final NBTTagBuilder reset() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
        tag = NMSUtil.getNMSClass("nbt.NBTTagCompound").getConstructor().newInstance();
        return this;
    }
    
    public NBTTagBuilder setString(String key, String value) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        tag.getClass().getMethod("a", String.class, String.class)
                      .invoke(tag, key, value);
        return this;
    }
    
    public NBTTagBuilder setInt(String key, int value) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        tag.getClass().getMethod("a", String.class, int.class)
                      .invoke(tag, key, value);
        return this;
    }
    
    public NBTTagBuilder setShort(String key, short value) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        tag.getClass().getMethod("a", String.class, short.class)
                      .invoke(tag, key, value);
        return this;
    }
    
    public NBTTagBuilder setByte(String key, byte value) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        tag.getClass().getMethod("a", String.class, byte.class)
                      .invoke(tag, key, value);
        return this;
    }
    
    public NBTTagBuilder setBoolean(String key, boolean value) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        tag.getClass().getMethod("a", String.class, boolean.class)
                      .invoke(tag, key, value);
        return this;
    }
    
    public NBTTagBuilder setFloat(String key, float value) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        tag.getClass().getMethod("a", String.class, float.class)
                      .invoke(tag, key, value);
        return this;
    }
    
    public NBTTagBuilder setTag(String key, Object newTag) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
        tag.getClass().getMethod("a", String.class, NMSUtil.getNMSClass("nbt.NBTBase"))
                      .invoke(tag, key, newTag);
        return this;
    }
    
    public NBTTagBuilder setTagList(String key, Object... tags) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        Object list = NMSUtil.getNMSClass("nbt.NBTTagList").getConstructor().newInstance();
        int i = 0;
        for(Object newTag: tags) {
            list.getClass().getMethod("b", int.class, NMSUtil.getNMSClass("nbt.NBTBase")).invoke(list, i, newTag);
            i++;
        }
        setTag(key, list);
        return this;
    }
    
    public NBTTagBuilder setFloatList(String key, Float... elements) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        Object list = NMSUtil.getNMSClass("nbt.NBTTagList").getConstructor().newInstance();
        int i = 0;
        for(Float element: elements) {
            Object newTag = NMSUtil.getNMSClass("nbt.NBTTagFloat").getConstructor(float.class)
                                                              .newInstance(element);
            list.getClass().getMethod("b"/*add*/, int.class, NMSUtil.getNMSClass("nbt.NBTBase")).invoke(list, i, newTag);
            i++;
        }
        setTag(key, list);
        return this;
    }
    
    public Object getTag() {
        return tag;
    }
    
    public short getShort(String key) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return (short) tag.getClass().getMethod("g"/*getShort*/, String.class).invoke(tag, key);
    }
    
    @Override
    public String toString() {
        return tag.toString();
    }
    
}
