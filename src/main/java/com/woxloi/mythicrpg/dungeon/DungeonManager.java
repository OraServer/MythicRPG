package com.woxloi.mythicrpg.dungeon;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.model.RpgItemSerializer;
import com.woxloi.mythicrpg.equipment.random.RandomItemGenerator;
import com.woxloi.mythicrpg.level.LevelManager;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ダンジョンの全セッションを管理する。
 *
 * YAMLロードは DungeonLoader が担当。
 * MythicMobsスポーンは DungeonMobSpawner が担当。
 * Mob全滅クリア判定は DungeonListener (MythicMobDeathEvent) から通知される。
 */
public class DungeonManager {

    /** 定義済みダンジョン一覧 (dungeons.yml から読み込み) */
    private static Map<String, DungeonDefinition> definitions = new LinkedHashMap<>();

    /** アクティブセッション: sessionId → session */
    private static final Map<String, DungeonSession> sessions = new ConcurrentHashMap<>();

    /** プレイヤーが参加中のセッションID: UUID → sessionId */
    private static final Map<UUID, String> playerSessionMap = new ConcurrentHashMap<>();

    /** タイマータスク: sessionId → task */
    private static final Map<String, BukkitTask> timers = new ConcurrentHashMap<>();

    private DungeonManager() {}

    // ─── 初期化 ─────────────────────────────────────────
    public static void load() {
        definitions = DungeonLoader.load();
    }

    // ─── 入場 ────────────────────────────────────────────
    public static String enter(Player player, String dungeonId) {
        DungeonDefinition def = definitions.get(dungeonId);
        if (def == null) return "存在しないダンジョンです";

        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return "プレイヤーデータが読み込まれていません";
        if (data.getLevel() < def.getRequiredLevel())
            return "必要Lv " + def.getRequiredLevel() + " に達していません（現在Lv" + data.getLevel() + "）";
        if (playerSessionMap.containsKey(player.getUniqueId()))
            return "すでにダンジョンに参加中です";

        DungeonSession session = new DungeonSession(def, List.of(player));
        sessions.put(session.getSessionId(), session);
        playerSessionMap.put(player.getUniqueId(), session.getSessionId());

        MythicRPG.playerPrefixMsg(player, "§a" + def.getDisplayName() + " §7に入場しました！");
        MythicRPG.playerPrefixMsg(player, "§71F / " + def.getFloorCount()
                + "F  §7制限時間: §e" + (def.getTimeLimitSeconds() / 60) + "分");
        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 0.8f, 0.8f);

        startTimer(session);
        spawnCurrentFloor(session, player);
        return null;
    }

    // ─── Mob全滅クリア（DungeonListenerから呼ぶ） ────────
    /**
     * Mob死亡時に呼ばれる。全滅したらフロア進行。
     * @param sessionId 死んだMobが持つセッションID
     * @param mobUuid   死んだMobのUUID
     */
    public static void onDungeonMobDied(String sessionId, java.util.UUID mobUuid) {
        DungeonSession session = sessions.get(sessionId);
        if (session == null || session.isFinished()) return;

        boolean allDead = session.onMobDied(mobUuid);
        if (!allDead) return;

        // 全滅！
        if (session.getState() == DungeonSession.State.BOSS_FLOOR) {
            completeDungeon(session);
        } else {
            // 次のフロアへ
            session.broadcastToParticipants("§a§l✔ フロアクリア！ §7次の階へ…");
            session.advanceFloor();

            // 少し間を置いてスポーン
            Bukkit.getScheduler().runTaskLater(MythicRPG.getInstance(), () -> {
                Player representative = getRepresentativePlayer(session);
                if (representative != null) spawnCurrentFloor(session, representative);
            }, 40L); // 2秒後
        }
    }

    // ─── フロアスポーン ───────────────────────────────────
    private static void spawnCurrentFloor(DungeonSession session, Player basePlayer) {
        DungeonMobSpawner.spawnFloor(session, session.getCurrentFloor(), basePlayer.getLocation());

        boolean isBoss = session.getActiveMobUuids().size() <= 1
                && !session.getDefinition().getBossId().isBlank()
                && session.getState() == DungeonSession.State.BOSS_FLOOR;

        session.broadcastToParticipants(
            session.getProgressDisplay() + " §7フロア開始！ §e" + session.getActiveMobUuids().size() + "体 §7出現");
        if (isBoss) {
            session.broadcastToParticipants("§c§l⚔ ボス戦開始！ §r§e" + session.getDefinition().getBossId());
        }
    }

    private static Player getRepresentativePlayer(DungeonSession session) {
        for (UUID uuid : session.getParticipants()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) return p;
        }
        return null;
    }

    // ─── ダンジョンクリア ─────────────────────────────────
    private static void completeDungeon(DungeonSession session) {
        session.complete();
        stopTimer(session.getSessionId());

        session.broadcastToParticipants("§6§l★ ダンジョンクリア！ ★");
        long elapsed = (System.currentTimeMillis() - session.getStartTimeMs()) / 1000;
        session.broadcastToParticipants("§7クリア時間: §e" + elapsed / 60 + "分" + elapsed % 60 + "秒");

        for (UUID uuid : session.getParticipants()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            giveReward(p, session.getDefinition());
            playerSessionMap.remove(uuid);
        }
        sessions.remove(session.getSessionId());
    }

    // ─── 退場 ────────────────────────────────────────────
    public static void leave(Player player) {
        String sid = playerSessionMap.remove(player.getUniqueId());
        if (sid == null) return;
        DungeonSession session = sessions.get(sid);
        if (session != null) {
            session.removeParticipant(player.getUniqueId());
            if (session.isEmpty()) {
                session.fail();
                stopTimer(sid);
                sessions.remove(sid);
            }
        }
        MythicRPG.playerPrefixMsg(player, "§cダンジョンから退出しました");
    }

    public static void onPlayerDeath(Player player) { leave(player); }

    // ─── 報酬 ─────────────────────────────────────────────
    private static void giveReward(Player player, DungeonDefinition def) {
        RpgItem reward = RandomItemGenerator.generate(def.getRewardRarity(), new Random());
        if (reward != null) {
            ItemStack is = RpgItemSerializer.toItemStack(reward);
            player.getInventory().addItem(is);
            MythicRPG.playerPrefixMsg(player, "§6報酬: " + reward.rarity.color + reward.displayName);
        }
        LevelManager.addExp(player, def.getExpReward());
        MythicRPG.playerPrefixMsg(player, "§a経験値 +" + def.getExpReward() + " 獲得！");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    // ─── タイマー ─────────────────────────────────────────
    private static void startTimer(DungeonSession session) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(MythicRPG.getInstance(), () -> {
            if (session.isFinished()) { stopTimer(session.getSessionId()); return; }
            boolean timeout = session.tickTimer();
            if (timeout) {
                session.fail();
                session.broadcastToParticipants("§c§l時間切れ！ ダンジョン失敗…");
                new ArrayList<>(session.getParticipants()).forEach(uuid -> {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) leave(p);
                });
                stopTimer(session.getSessionId());
                sessions.remove(session.getSessionId());
            } else if (session.getRemainingSeconds() % 60 == 0 || session.getRemainingSeconds() <= 30) {
                session.broadcastToParticipants("§e残り時間: " + session.getRemainingTimeDisplay());
            }
        }, 20L, 20L);
        timers.put(session.getSessionId(), task);
    }

    private static void stopTimer(String sessionId) {
        BukkitTask task = timers.remove(sessionId);
        if (task != null) task.cancel();
    }

    // ─── ユーティリティ ───────────────────────────────────
    public static DungeonSession getSession(Player player) {
        String sid = playerSessionMap.get(player.getUniqueId());
        return sid != null ? sessions.get(sid) : null;
    }

    public static DungeonSession getSessionById(String sessionId) {
        return sessions.get(sessionId);
    }

    public static boolean isInDungeon(Player player) {
        return playerSessionMap.containsKey(player.getUniqueId());
    }

    public static Collection<DungeonDefinition> getAllDefinitions() {
        return definitions.values();
    }
}
