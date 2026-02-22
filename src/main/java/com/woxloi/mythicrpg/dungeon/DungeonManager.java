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
 * - 入場判定・セッション生成
 * - フロア進行
 * - タイマータスク
 * - 報酬配布
 */
public class DungeonManager {

    /** 定義済みダンジョン一覧 */
    private static final Map<String, DungeonDefinition> definitions = new LinkedHashMap<>();

    /** アクティブセッション: sessionId → session */
    private static final Map<String, DungeonSession> sessions = new ConcurrentHashMap<>();

    /** プレイヤーが参加中のセッションID: UUID → sessionId */
    private static final Map<UUID, String> playerSessionMap = new ConcurrentHashMap<>();

    /** タイマータスク: sessionId → task */
    private static final Map<String, BukkitTask> timers = new ConcurrentHashMap<>();

    static {
        loadDefaultDungeons();
    }

    private static void loadDefaultDungeons() {
        definitions.put("goblin_cave", new DungeonDefinition(
            "goblin_cave", "§2ゴブリンの洞窟", "初心者向けの洞窟ダンジョン",
            1, 4, 5, 600,
            com.woxloi.mythicrpg.equipment.model.EquipRarity.UNCOMMON,
            List.of("GoblinWarrior", "GoblinArcher"), "GoblinKing"
        ));
        definitions.put("dark_forest", new DungeonDefinition(
            "dark_forest", "§5闇の森", "中級者向けの呪われた森",
            20, 4, 8, 900,
            com.woxloi.mythicrpg.equipment.model.EquipRarity.RARE,
            List.of("DarkWolf", "ShadowSpirit"), "ForestDemon"
        ));
        definitions.put("dragon_lair", new DungeonDefinition(
            "dragon_lair", "§4龍の巣窟", "上級者向けのドラゴンダンジョン",
            50, 6, 10, 1200,
            com.woxloi.mythicrpg.equipment.model.EquipRarity.EPIC,
            List.of("DragonHatchling", "DragonGuard"), "AncientDragon"
        ));
    }

    private DungeonManager() {}

    // ─── 入場 ────────────────────────────────────

    /**
     * プレイヤーをダンジョンに入場させる。
     * @return null=成功, String=失敗理由
     */
    public static String enter(Player player, String dungeonId) {
        DungeonDefinition def = definitions.get(dungeonId);
        if (def == null) return "存在しないダンジョンです";

        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return "プレイヤーデータが読み込まれていません";
        if (data.getLevel() < def.getRequiredLevel())
            return "必要Lv " + def.getRequiredLevel() + " に達していません（現在Lv" + data.getLevel() + "）";

        if (playerSessionMap.containsKey(player.getUniqueId()))
            return "すでにダンジョンに参加中です";

        // 新規セッション作成
        DungeonSession session = new DungeonSession(def, List.of(player));
        session.advanceFloor(); // 1Fからスタート
        sessions.put(session.getSessionId(), session);
        playerSessionMap.put(player.getUniqueId(), session.getSessionId());

        startTimer(session);

        MythicRPG.playerPrefixMsg(player, "§a" + def.getDisplayName() + " §7に入場しました！");
        MythicRPG.playerPrefixMsg(player, "§71F / " + def.getFloorCount() + "F  §7制限時間: §e"
                + (def.getTimeLimitSeconds() / 60) + "分");
        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 0.8f, 0.8f);
        return null;
    }

    // ─── フロア進行 ──────────────────────────────

    /** フロアクリアを通知し次の階へ進める */
    public static void completeFloor(Player player) {
        DungeonSession session = getSession(player);
        if (session == null) return;

        if (session.getState() == DungeonSession.State.BOSS_FLOOR) {
            completeDungeon(session);
            return;
        }

        session.advanceFloor();
        broadcastToSession(session, "§a§l" + session.getCurrentFloor() + "F クリア！ §7次のフロアへ…");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }

    /** ダンジョンクリア処理 */
    private static void completeDungeon(DungeonSession session) {
        session.complete();
        stopTimer(session.getSessionId());

        broadcastToSession(session, "§6§l★ ダンジョンクリア！ ★");
        long elapsed = (System.currentTimeMillis() - session.getStartTimeMs()) / 1000;
        broadcastToSession(session, "§7クリア時間: §e" + elapsed / 60 + "分" + elapsed % 60 + "秒");

        // 報酬配布
        for (UUID uuid : session.getParticipants()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            giveReward(p, session.getDefinition());
            playerSessionMap.remove(uuid);
        }
        sessions.remove(session.getSessionId());
    }

    // ─── 退場・失敗 ──────────────────────────────

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

    public static void onPlayerDeath(Player player) {
        leave(player);
    }

    // ─── 報酬 ────────────────────────────────────

    private static void giveReward(Player player, DungeonDefinition def) {
        // ランダム装備をドロップ
        RpgItem reward = RandomItemGenerator.generate(def.getRewardRarity(), new Random());
        if (reward != null) {
            ItemStack is = RpgItemSerializer.toItemStack(reward);
            player.getInventory().addItem(is);
            MythicRPG.playerPrefixMsg(player, "§6報酬: " + reward.rarity.color + reward.displayName);
        }

        // EXPボーナス
        double expBonus = def.getFloorCount() * def.getRequiredLevel() * 5.0;
        LevelManager.addExp(player, expBonus);
        MythicRPG.playerPrefixMsg(player, "§a経験値 +" + (int)expBonus + " 獲得！");

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    // ─── タイマー ────────────────────────────────

    private static void startTimer(DungeonSession session) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(MythicRPG.getInstance(), () -> {
            if (session.isFinished()) {
                stopTimer(session.getSessionId());
                return;
            }
            boolean timeout = session.tickTimer();
            if (timeout) {
                session.fail();
                broadcastToSession(session, "§c§l時間切れ！ ダンジョン失敗…");
                // 参加者を全員退出
                new ArrayList<>(session.getParticipants()).forEach(uuid -> {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) leave(p);
                });
                stopTimer(session.getSessionId());
                sessions.remove(session.getSessionId());
            } else if (session.getRemainingSeconds() % 60 == 0 || session.getRemainingSeconds() <= 30) {
                broadcastToSession(session, "§e残り時間: " + session.getRemainingTimeDisplay());
            }
        }, 20L, 20L);
        timers.put(session.getSessionId(), task);
    }

    private static void stopTimer(String sessionId) {
        BukkitTask task = timers.remove(sessionId);
        if (task != null) task.cancel();
    }

    // ─── ユーティリティ ──────────────────────────

    public static DungeonSession getSession(Player player) {
        String sid = playerSessionMap.get(player.getUniqueId());
        return sid != null ? sessions.get(sid) : null;
    }

    public static boolean isInDungeon(Player player) {
        return playerSessionMap.containsKey(player.getUniqueId());
    }

    public static Collection<DungeonDefinition> getAllDefinitions() {
        return definitions.values();
    }

    private static void broadcastToSession(DungeonSession session, String msg) {
        for (UUID uuid : session.getParticipants()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) MythicRPG.playerPrefixMsg(p, msg);
        }
    }
}
