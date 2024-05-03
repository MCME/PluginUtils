package com.mcmiddleearth.pluginutil.nms;

import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Optional;

public class AccessServer {

    public static void addFreshEntity(Object nmsWorld, Object entity) throws ClassNotFoundException {
        Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("world.entity.Entity"),
                CreatureSpawnEvent.SpawnReason.CUSTOM.getClass()};
        //boolean addFreshEntity(Entity, SpawnReason)
        NMSUtil.invokeNMS("server.level.WorldServer", "addFreshEntity"/*"addEntity"*/, argsClasses, nmsWorld, ((Optional) entity).get(),
                CreatureSpawnEvent.SpawnReason.CUSTOM);
    }
}
