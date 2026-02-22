package com.woxloi.mythicrpg.player;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.job.JobSelectGUI;
import com.woxloi.mythicrpg.ui.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerDataManager.load(event.getPlayer());
        ScoreboardManager.init(event.getPlayer());

        Bukkit.getScheduler().runTaskLater(
                MythicRPG.getInstance(),
                () -> {
                    PlayerData data = PlayerDataManager.get(event.getPlayer());
                    if (data != null && !data.hasJob()) {
                        JobSelectGUI.open(event.getPlayer());
                    }
                },
                2L
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PlayerDataManager.save(event.getPlayer());
        ScoreboardManager.remove(event.getPlayer());
    }
}
