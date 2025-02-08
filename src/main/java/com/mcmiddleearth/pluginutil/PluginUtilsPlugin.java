/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil;

import com.mcmiddleearth.pluginutil.developer.Debugable;
import com.mcmiddleearth.pluginutil.developer.DevCommand;
import com.mcmiddleearth.pluginutil.developer.DevUtil;
import org.bukkit.plugin.java.JavaPlugin;


/**
 *
 * @author Eriol_Eandur
 */
public class PluginUtilsPlugin extends JavaPlugin implements Debugable {
        
    private static JavaPlugin instance;

    public static JavaPlugin getInstance() {
        return instance;
    }

    public final DevUtil devUtil = new DevUtil();

    @Override
    public void onEnable() {
        getCommand("dev").setExecutor(new DevCommand());
        instance = this;
    }

    @Override
    public DevUtil getDevUtil() {
        return devUtil;
    }
}
