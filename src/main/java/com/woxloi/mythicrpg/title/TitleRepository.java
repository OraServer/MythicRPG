package com.woxloi.mythicrpg.title;

import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.db.DatabaseManager;

import java.sql.*;
import java.util.*;

/**
 * 称号の解放状態・統計をMySQLに保存/読み込み
 */
public class TitleRepository {

    // ─── 解放称号の保存 ───
    public static void saveUnlocked(UUID uuid, Set<TitleDefinition> unlocked, TitleDefinition active) {
        String deleteSQL = "DELETE FROM player_titles WHERE uuid = ?";
        String insertSQL = "INSERT INTO player_titles (uuid, title_id) VALUES (?, ?)";
        String activeSql = "INSERT INTO player_active_title (uuid, title_id) VALUES (?, ?) " +
                           "ON DUPLICATE KEY UPDATE title_id = VALUES(title_id)";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement del = conn.prepareStatement(deleteSQL)) {
                del.setString(1, uuid.toString());
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(insertSQL)) {
                for (TitleDefinition t : unlocked) {
                    ins.setString(1, uuid.toString());
                    ins.setString(2, t.name());
                    ins.addBatch();
                }
                ins.executeBatch();
            }
            if (active != null) {
                try (PreparedStatement act = conn.prepareStatement(activeSql)) {
                    act.setString(1, uuid.toString());
                    act.setString(2, active.name());
                    act.executeUpdate();
                }
            }
        } catch (SQLException e) {
            MythicLogger.error("称号保存エラー: " + e.getMessage());
        }
    }

    // ─── 統計保存 ───
    public static void saveStats(UUID uuid, TitleManager.TitleStats stats) {
        String sql = """
            INSERT INTO player_title_stats (uuid, mob_kills, quest_completes, craft_count)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                mob_kills       = VALUES(mob_kills),
                quest_completes = VALUES(quest_completes),
                craft_count     = VALUES(craft_count)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setLong(2, stats.mobKills);
            ps.setLong(3, stats.questCompletes);
            ps.setLong(4, stats.craftCount);
            ps.executeUpdate();
        } catch (SQLException e) {
            MythicLogger.error("称号統計保存エラー: " + e.getMessage());
        }
    }

    // ─── 読み込み ───
    public static Set<String> loadUnlocked(UUID uuid) {
        Set<String> result = new HashSet<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT title_id FROM player_titles WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(rs.getString("title_id"));
        } catch (SQLException e) {
            MythicLogger.error("称号読み込みエラー: " + e.getMessage());
        }
        return result;
    }

    public static String loadActive(UUID uuid) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT title_id FROM player_active_title WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("title_id");
        } catch (SQLException e) {
            MythicLogger.error("アクティブ称号読み込みエラー: " + e.getMessage());
        }
        return null;
    }
}
