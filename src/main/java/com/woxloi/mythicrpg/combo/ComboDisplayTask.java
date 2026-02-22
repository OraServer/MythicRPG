package com.woxloi.mythicrpg.combo;

import com.woxloi.mythicrpg.MythicRPG;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * 毎tick（20tick/s）にコンボ数をアクションバーに表示するタスク。
 * コンボが0の場合は何も表示しない。
 */
public class ComboDisplayTask extends BukkitRunnable {

    private static BukkitTask task;

    public static void start() {
        if (task != null) return;
        task = new ComboDisplayTask().runTaskTimer(MythicRPG.getInstance(), 0L, 4L);
    }

    public static void stop() {
        if (task != null) { task.cancel(); task = null; }
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            int combo = ComboManager.getComboCount(player);
            if (combo < 5) continue;  // 5コンボ未満は表示なし

            double mult = ComboManager.getDamageMultiplier(player);
            String color = combo >= 30 ? "§4§l" : combo >= 20 ? "§c" : combo >= 10 ? "§6" : "§e";
            String msg = color + combo + " COMBO! §f×" + String.format("%.2f", mult);

            player.sendActionBar(Component.text(msg));
        }
    }
}
