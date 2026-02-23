package com.woxloi.mythicrpg.dungeon;

import com.woxloi.mythicrpg.MythicRPG;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * ダンジョン中のイベントを処理する。
 *
 * - プレイヤー死亡 → セッションから除外
 * - プレイヤーログアウト → セッションから除外
 * - MythicMob死亡 → Mob全滅判定 → フロアクリア or ダンジョンクリア
 */
public class DungeonListener implements Listener {

    /** ダンジョンセッションIDのPDCキー */
    private static final NamespacedKey SESSION_KEY =
            new NamespacedKey(MythicRPG.getInstance(), DungeonMobSpawner.META_SESSION_ID);

    // ────────────────────────────────────────────────
    //  プレイヤーイベント
    // ────────────────────────────────────────────────

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

    // ────────────────────────────────────────────────
    //  MythicMob死亡 → Mob全滅クリア判定
    // ────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        // MobエンティティのPDCからセッションIDを取得
        org.bukkit.entity.Entity entity = event.getMob().getEntity().getBukkitEntity();
        String sessionId = entity.getPersistentDataContainer()
                .get(SESSION_KEY, PersistentDataType.STRING);

        if (sessionId == null) return; // ダンジョン外のMob

        UUID mobUuid = entity.getUniqueId();
        DungeonManager.onDungeonMobDied(sessionId, mobUuid);
    }
}
