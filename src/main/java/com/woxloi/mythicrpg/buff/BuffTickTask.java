package com.woxloi.mythicrpg.buff;

import com.woxloi.mythicrpg.MythicRPG;
import org.bukkit.scheduler.BukkitTask;

/**
 * BuffManagerの毎秒Tick処理を定期実行するタスク
 */
public class BuffTickTask {

    private static BukkitTask task;

    public static void start() {
        if (task != null) return;
        task = MythicRPG.getInstance().getServer().getScheduler()
                .runTaskTimer(MythicRPG.getInstance(), BuffManager::tickAll, 20L, 20L);
    }

    public static void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
