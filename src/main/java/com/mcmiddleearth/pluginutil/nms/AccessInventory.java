package com.mcmiddleearth.pluginutil.nms;

import net.minecraft.core.HolderLookup;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class AccessInventory {

    public static Object getItemNBT(ItemStack item) {
        HolderLookup.Provider provider = ((CraftServer) Bukkit.getServer()).getServer().registries().compositeAccess();
        return ((CraftItemStack)item).handle.save(provider);
    }


}
