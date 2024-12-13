package com.mcmiddleearth.pluginutil.nms;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Optional;

public class AccessServer {

    @SuppressWarnings("unchecked")
    public static void addFreshEntity(Object nmsWorld, Object entity) throws ClassNotFoundException {
        ((ServerLevel)nmsWorld).addFreshEntity(((Optional<Entity>) entity).get(),CreatureSpawnEvent.SpawnReason.CUSTOM);
        /*Class[] argsClasses = new Class[]{NMSUtil.getNMSClass("world.entity.Entity"),
                CreatureSpawnEvent.SpawnReason.CUSTOM.getClass()};
        //boolean addFreshEntity(Entity, SpawnReason)
        NMSUtil.invokeNMS("server.level.WorldServer", "addFreshEntity"/*"addEntity", argsClasses, nmsWorld, ((Optional) entity).get(),
                CreatureSpawnEvent.SpawnReason.CUSTOM);*/
    }
}
