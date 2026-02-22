package com.woxloi.mythicrpg.pvp;

import com.woxloi.mythicrpg.MythicRPG;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * PvPのキル数・デス数・レーティング（ELO風）を管理する。
 * 上位ランキングをリアルタイム表示できる。
 */
public class PvpRankingManager implements Listener {

    public static class PvpStats {
        public int kills;
        public int deaths;
        public int rating;
        public String playerName;

        public PvpStats(String playerName) {
            this.playerName = playerName;
            this.kills  = 0;
            this.deaths = 0;
            this.rating = 1000; // 初期レーティング
        }

        public double getKDR() {
            return deaths == 0 ? kills : (double) kills / deaths;
        }

        public String format() {
            return "§e" + kills + "§7K §c" + deaths + "§7D §b"
                    + String.format("%.2f", getKDR()) + "§7KDR §6" + rating + "§7pt";
        }
    }

    /** UUID → PvPStats */
    private static final Map<UUID, PvpStats> stats = new ConcurrentHashMap<>();

    // ─── イベント処理 ────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // PvPゾーン内での死亡かチェック
        if (!PvpZoneManager.isInPvpZone(victim.getLocation())) return;
        if (killer == null || killer.equals(victim)) return;

        // デス更新
        PvpStats victimStats = getOrCreate(victim);
        victimStats.deaths++;

        // キル更新
        PvpStats killerStats = getOrCreate(killer);
        killerStats.kills++;

        // ELO風レーティング更新
        updateRating(killerStats, victimStats);

        // キルメッセージ
        MythicRPG.playerPrefixMsg(killer,
            "§c⚔ " + victim.getName() + " §7を倒しました！ "
            + "§6" + killerStats.kills + "K §7(§6+" + calcRatingDelta(killerStats, victimStats) + "pt§7)");
    }

    // ─── レーティング計算 (ELO風) ────────────────

    private static int calcRatingDelta(PvpStats winner, PvpStats loser) {
        double expected = 1.0 / (1.0 + Math.pow(10.0, (loser.rating - winner.rating) / 400.0));
        return (int)(32 * (1.0 - expected));
    }

    private static void updateRating(PvpStats winner, PvpStats loser) {
        int delta = calcRatingDelta(winner, loser);
        winner.rating += delta;
        loser.rating   = Math.max(0, loser.rating - delta);
    }

    // ─── ランキング表示 ──────────────────────────

    /**
     * レーティング順の上位n件を返す。
     */
    public static List<Map.Entry<UUID, PvpStats>> getTopByRating(int n) {
        return stats.entrySet().stream()
                .sorted((a, b) -> b.getValue().rating - a.getValue().rating)
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * キル数順の上位n件を返す。
     */
    public static List<Map.Entry<UUID, PvpStats>> getTopByKills(int n) {
        return stats.entrySet().stream()
                .sorted((a, b) -> b.getValue().kills - a.getValue().kills)
                .limit(n)
                .collect(Collectors.toList());
    }

    /** ランキングをプレイヤーに送信 */
    public static void sendRanking(Player player) {
        MythicRPG.playerPrefixMsg(player, "§6§l=== PvPランキング TOP5 ===");
        List<Map.Entry<UUID, PvpStats>> top = getTopByRating(5);
        if (top.isEmpty()) {
            MythicRPG.playerPrefixMsg(player, "§7まだデータがありません");
            return;
        }
        for (int i = 0; i < top.size(); i++) {
            PvpStats s = top.get(i).getValue();
            String rank = switch (i) {
                case 0 -> "§6#1";
                case 1 -> "§7#2";
                case 2 -> "§c#3";
                default -> "§8#" + (i + 1);
            };
            MythicRPG.playerPrefixMsg(player, rank + " §f" + s.playerName + " " + s.format());
        }
    }

    // ─── ユーティリティ ──────────────────────────

    public static PvpStats getOrCreate(Player player) {
        return stats.computeIfAbsent(player.getUniqueId(),
                k -> new PvpStats(player.getName()));
    }

    public static PvpStats getStats(UUID uuid) { return stats.get(uuid); }

    public static Map<UUID, PvpStats> getAllStats() { return Collections.unmodifiableMap(stats); }
}
