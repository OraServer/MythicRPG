package com.woxloi.mythicrpg.ui;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class ResourceRegenTask extends BukkitRunnable {

    private static ResourceRegenTask task;

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            PlayerData data = PlayerDataManager.get(player);
            if (data == null) return;

            data.addMp(1);
            data.addSp(2);
        });
    }

    public static void start() {
        if (task != null) return;

        task = new ResourceRegenTask();
        task.runTaskTimer(
                MythicRPG.getInstance(),
                20L,
                20L
        );
    }

    public static void stop() {
        if (task == null) return;

        task.cancel();
        task = null;
    }
}
