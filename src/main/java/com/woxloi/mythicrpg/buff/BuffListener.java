package com.woxloi.mythicrpg.buff;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 死亡・ログアウト時にバフをクリアする
 */
public class BuffListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        BuffManager.clearAll(event.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        BuffManager.clearAll(event.getPlayer());
    }
}
