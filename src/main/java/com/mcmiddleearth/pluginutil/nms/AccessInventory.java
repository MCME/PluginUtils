package com.mcmiddleearth.pluginutil.nms;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class AccessInventory {

    public static Object getItemNBT(ItemStack item) {
        // ItemStack.save(Provider) was removed; serialize via the item Codec + registry-aware NBT ops.
        HolderLookup.Provider provider = ((CraftServer) Bukkit.getServer()).getServer().registries().compositeAccess();
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        return net.minecraft.world.item.ItemStack.CODEC.encodeStart(ops, ((CraftItemStack) item).handle).getOrThrow();
    }

}
