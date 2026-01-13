package com.woxloi.mythicrpg.player;

import com.woxloi.mythicrpg.job.JobSelectGUI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerDataManager.load(event.getPlayer());

        // 1tick遅らせてGUI表示
        Bukkit.getScheduler().runTaskLater(
                com.woxloi.mythicrpg.MythicRPG.getInstance(),
                () -> {
                    if (!PlayerDataManager.get(event.getPlayer()).hasJob()) {
                        JobSelectGUI.open(event.getPlayer());
                    }
                },
                1L
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PlayerDataManager.save(event.getPlayer());
    }
}
