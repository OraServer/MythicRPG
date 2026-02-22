package com.woxloi.mythicrpg.combo;

import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.db.DatabaseManager;

import java.sql.*;
import java.util.UUID;

/**
 * コンボ最大値などの統計をDBに保存
 */
public class ComboRepository {

    public static void updateMaxCombo(UUID uuid, int combo) {
        String sql = """
            INSERT INTO player_combo_stats (uuid, max_combo, total_combos)
            VALUES (?, ?, 1)
            ON DUPLICATE KEY UPDATE
                max_combo    = GREATEST(max_combo, VALUES(max_combo)),
                total_combos = total_combos + 1
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, combo);
            ps.executeUpdate();
        } catch (SQLException e) {
            MythicLogger.error("コンボ統計保存エラー: " + e.getMessage());
        }
    }

    public static int getMaxCombo(UUID uuid) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT max_combo FROM player_combo_stats WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("max_combo");
        } catch (SQLException e) {
            MythicLogger.error("コンボ統計読み込みエラー: " + e.getMessage());
        }
        return 0;
    }
}
