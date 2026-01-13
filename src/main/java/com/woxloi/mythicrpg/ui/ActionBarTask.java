package com.woxloi.mythicrpg.ui;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarTask extends BukkitRunnable {

    private static ActionBarTask task;

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            PlayerData data = PlayerDataManager.get(player);
            if (data == null) return;

            ActionBarManager.update(player, data);
        });
    }

    public static void start() {
        if (task != null) return;

        task = new ActionBarTask();
        task.runTaskTimer(
                MythicRPG.getInstance(),
                0L,
                10L
        );
    }

    public static void stop() {
        if (task == null) return;

        task.cancel();
        task = null;
    }
}
