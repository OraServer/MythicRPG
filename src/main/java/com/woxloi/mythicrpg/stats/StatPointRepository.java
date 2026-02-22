package com.woxloi.mythicrpg.stats;

import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.db.DatabaseManager;

import java.sql.*;
import java.util.UUID;

/**
 * StatPoint を MySQL に保存・読み込み
 */
public class StatPointRepository {

    public static void save(UUID uuid, StatPoint sp) {
        String sql = """
            INSERT INTO player_stat_points (uuid, free_points, str_points, vit_points, int_points, agi_points)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                free_points = VALUES(free_points),
                str_points  = VALUES(str_points),
                vit_points  = VALUES(vit_points),
                int_points  = VALUES(int_points),
                agi_points  = VALUES(agi_points)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, sp.getFreePoints());
            ps.setInt(3, sp.getStrPoints());
            ps.setInt(4, sp.getVitPoints());
            ps.setInt(5, sp.getIntPoints());
            ps.setInt(6, sp.getAgiPoints());
            ps.executeUpdate();
        } catch (SQLException e) {
            MythicLogger.error("ステータスポイント保存エラー: " + e.getMessage());
        }
    }

    public static void load(UUID uuid) {
        String sql = "SELECT * FROM player_stat_points WHERE uuid = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                StatPointManager.loadFromDb(
                        uuid,
                        rs.getInt("free_points"),
                        rs.getInt("str_points"),
                        rs.getInt("vit_points"),
                        rs.getInt("int_points"),
                        rs.getInt("agi_points")
                );
            }
        } catch (SQLException e) {
            MythicLogger.error("ステータスポイント読み込みエラー: " + e.getMessage());
        }
    }
}
