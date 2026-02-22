package com.woxloi.mythicrpg.title;

import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 称号の解放・選択・表示を管理する
 */
public class TitleManager {

    // UUID → 解放済み称号セット
    private static final Map<UUID, Set<TitleDefinition>> unlockedTitles = new HashMap<>();

    // UUID → 現在選択中の称号
    private static final Map<UUID, TitleDefinition> activeTitles = new HashMap<>();

    // UUID → 統計カウンター
    private static final Map<UUID, TitleStats> playerStats = new HashMap<>();

    // ─────────────────────────────────
    //  統計更新 → 称号解放チェック
    // ─────────────────────────────────

    public static void incrementMobKill(Player player) {
        TitleStats stats = getStats(player);
        stats.mobKills++;
        checkUnlock(player);
    }

    public static void incrementQuestComplete(Player player) {
        TitleStats stats = getStats(player);
        stats.questCompletes++;
        checkUnlock(player);
    }

    public static void incrementCraft(Player player) {
        TitleStats stats = getStats(player);
        stats.craftCount++;
        checkUnlock(player);
    }

    public static void onLevelChange(Player player) {
        checkUnlock(player);
    }

    public static void onJobSelect(Player player) {
        checkUnlock(player);
    }

    /** 称号解放チェック */
    public static void checkUnlock(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        TitleStats stats = getStats(player);
        Set<TitleDefinition> unlocked = unlockedTitles.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());

        for (TitleDefinition title : TitleDefinition.values()) {
            if (unlocked.contains(title)) continue;

            boolean met = switch (title.getCondition()) {
                case LEVEL          -> data.getLevel() >= title.getThreshold();
                case JOB            -> data.hasJob();
                case MOB_KILL       -> stats.mobKills >= title.getThreshold();
                case QUEST_COMPLETE -> stats.questCompletes >= title.getThreshold();
                case CRAFT          -> stats.craftCount >= title.getThreshold();
                case MONEY          -> false; // Vault連携は別途
            };

            if (met) {
                unlocked.add(title);
                player.sendMessage("§6§l★ 称号解放！ §r" + title.getDisplayTag()
                        + " §7- " + title.getDescription());
                player.sendMessage("§7/mrpg title で称号を選択できます");
            }
        }
    }

    // ─────────────────────────────────
    //  選択・取得
    // ─────────────────────────────────

    public static void setActiveTitle(Player player, TitleDefinition title) {
        activeTitles.put(player.getUniqueId(), title);
    }

    public static TitleDefinition getActiveTitle(UUID uuid) {
        return activeTitles.get(uuid);
    }

    public static String getDisplayTag(UUID uuid) {
        TitleDefinition title = activeTitles.get(uuid);
        return title == null ? "" : title.getDisplayTag() + " ";
    }

    public static Set<TitleDefinition> getUnlocked(Player player) {
        return unlockedTitles.getOrDefault(player.getUniqueId(), Collections.emptySet());
    }

    // ─────────────────────────────────
    //  永続化ヘルパー (DB連携は TitleRepository)
    // ─────────────────────────────────

    public static void loadFromDb(UUID uuid, Set<String> titleNames, String activeTitle) {
        Set<TitleDefinition> set = new HashSet<>();
        for (String name : titleNames) {
            try { set.add(TitleDefinition.valueOf(name)); } catch (Exception ignored) {}
        }
        unlockedTitles.put(uuid, set);

        if (activeTitle != null) {
            try { activeTitles.put(uuid, TitleDefinition.valueOf(activeTitle)); } catch (Exception ignored) {}
        }
    }

    private static TitleStats getStats(Player player) {
        return playerStats.computeIfAbsent(player.getUniqueId(), k -> new TitleStats());
    }

    public static TitleStats getStatsPublic(UUID uuid) {
        return playerStats.getOrDefault(uuid, new TitleStats());
    }

    // ─────────────────────────────────
    //  統計クラス
    // ─────────────────────────────────
    public static class TitleStats {
        public long mobKills       = 0;
        public long questCompletes = 0;
        public long craftCount     = 0;
        public long level           = 1;
    }
}
