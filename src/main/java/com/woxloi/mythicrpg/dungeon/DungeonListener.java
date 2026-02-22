package com.woxloi.mythicrpg.dungeon;

import com.woxloi.mythicrpg.MythicRPG;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * ダンジョン中のプレイヤー死亡・切断・ワールド移動を検知して
 * セッションから除外・失敗処理を行う。
 */
public class DungeonListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!DungeonManager.isInDungeon(player)) return;

        MythicRPG.playerPrefixMsg(player, "§c戦闘不能… ダンジョンから退出しました");
        DungeonManager.onPlayerDeath(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!DungeonManager.isInDungeon(player)) return;

        DungeonManager.leave(player);
    }
}
