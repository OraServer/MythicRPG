package com.woxloi.mythicrpg.core;

import com.woxloi.mythicrpg.MythicRPG;
import org.bukkit.Bukkit;

public class MythicLogger {

    private static final String PREFIX = "[MythicRPG] ";

    public static void info(String msg) {
        Bukkit.getLogger().info(PREFIX + msg);
    }

    public static void warn(String msg) {
        Bukkit.getLogger().warning(PREFIX + msg);
    }

    public static void error(String msg) {
        Bukkit.getLogger().severe(PREFIX + msg);
    }

    public static void debug(String msg) {
        if (MythicRPG.getInstance().getConfig().getBoolean("debug")) {
            Bukkit.getLogger().info(PREFIX + "§7[DEBUG] " + msg);
        }
    }
}
