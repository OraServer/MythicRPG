package com.woxloi.mythicrpg;

import com.woxloi.mythicrpg.core.PluginBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

public class MythicRPG extends JavaPlugin {

    private static MythicRPG instance;

    public static MythicRPG getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        new PluginBootstrap(this).enable();
    }

    @Override
    public void onDisable() {
        new PluginBootstrap(this).disable();
    }
}
