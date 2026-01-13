package com.woxloi.mythicrpg.ui;

import com.woxloi.mythicrpg.MythicRPG;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreboardTask extends BukkitRunnable {

    private static ScoreboardTask task;

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(ScoreboardManager::update);
    }

    public static void start() {
        if (task != null) return;

        task = new ScoreboardTask();
        task.runTaskTimer(
                MythicRPG.getInstance(),
                20L,
                40L
        );
    }

    public static void stop() {
        if (task == null) return;

        task.cancel();
        task = null;
    }
}
