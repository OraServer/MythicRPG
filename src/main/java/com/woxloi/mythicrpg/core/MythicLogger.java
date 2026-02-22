package com.woxloi.mythicrpg.core;

import com.woxloi.mythicrpg.MythicRPG;
import org.bukkit.Bukkit;

public class MythicLogger {

    public static void info(String msg) {
        Bukkit.getLogger().info("[MythicRPG] " + msg);
    }

    public static void warn(String msg) {
        Bukkit.getLogger().warning("[MythicRPG] " + msg);
    }

    public static void error(String msg) {
        Bukkit.getLogger().severe("[MythicRPG] " + msg);
    }

    public static void debug(String msg) {
        if (MythicRPG.getInstance().getConfig().getBoolean("debug")) {
            Bukkit.getLogger().info("[MythicRPG] [DEBUG] " + msg);
        }
    }
}
