package com.woxloi.mythicrpg.db;

import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.job.JobType;
import com.woxloi.mythicrpg.player.PlayerData;

import java.sql.*;
import java.util.UUID;

/**
 * PlayerData の MySQL CRUD 操作。
 * 非同期呼び出しを前提としているため、このクラス自体はスレッドセーフに書く。
 */
public class PlayerRepository {

    /* =====================
       ロード
     ===================== */

    /**
     * DBからプレイヤーデータを読み込む。
     * 存在しない場合は新規レコードを INSERT して返す。
     */
    public static PlayerData load(UUID uuid) {
        PlayerData data = new PlayerData(uuid);

        try (Connection conn = DatabaseManager.getConnection()) {

            // --- players ---
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT job, level, exp FROM players WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    // 既存プレイヤー
                    String jobStr = rs.getString("job");
                    if (jobStr != null) {
                        try { data.setJob(JobType.valueOf(jobStr)); } catch (IllegalArgumentException ignored) {}
                    }
                    data.setLevel(rs.getInt("level"));
                    data.setExp(rs.getDouble("exp"));

                } else {
                    // 新規プレイヤー → INSERT
                    insertPlayer(conn, uuid);
                    MythicLogger.debug("新規プレイヤー登録: " + uuid);
                }
            }

            // --- player_stats ---
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT max_hp, hp, max_mp, mp, max_sp, sp, attack FROM player_stats WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    data.setMaxHp(rs.getDouble("max_hp"));
                    data.setHp(rs.getDouble("hp"));
                    data.setMaxMp(rs.getDouble("max_mp"));
                    data.setMp(rs.getDouble("mp"));
                    data.setMaxSp(rs.getDouble("max_sp"));
                    data.setSp(rs.getDouble("sp"));
                    data.setAttack(rs.getDouble("attack"));
                }
            }

            // --- player_skills ---
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT skill_id FROM player_skills WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    data.unlockSkill(rs.getString("skill_id"));
                }
            }

        } catch (SQLException e) {
            MythicLogger.error("プレイヤーロードエラー (" + uuid + "): " + e.getMessage());
        }

        return data;
    }

    /* =====================
       セーブ
     ===================== */

    /**
     * プレイヤーデータをDBに保存する（UPSERT）。
     */
    public static void save(PlayerData data) {
        String uuid = data.getUuid().toString();

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // --- players ---
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO players (uuid, job, level, exp)
                        VALUES (?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            job = VALUES(job),
                            level = VALUES(level),
                            exp = VALUES(exp)
                        """)) {
                    ps.setString(1, uuid);
                    ps.setString(2, data.hasJob() ? data.getJob().name() : null);
                    ps.setInt(3, data.getLevel());
                    ps.setDouble(4, data.getExp());
                    ps.executeUpdate();
                }

                // --- player_stats ---
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO player_stats (uuid, max_hp, hp, max_mp, mp, max_sp, sp, attack)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            max_hp = VALUES(max_hp), hp = VALUES(hp),
                            max_mp = VALUES(max_mp), mp = VALUES(mp),
                            max_sp = VALUES(max_sp), sp = VALUES(sp),
                            attack = VALUES(attack)
                        """)) {
                    ps.setString(1, uuid);
                    ps.setDouble(2, data.getMaxHp());
                    ps.setDouble(3, data.getHp());
                    ps.setDouble(4, data.getMaxMp());
                    ps.setDouble(5, data.getMp());
                    ps.setDouble(6, data.getMaxSp());
                    ps.setDouble(7, data.getSp());
                    ps.setDouble(8, data.getAttack());
                    ps.executeUpdate();
                }

                // --- player_skills (差分INSERT) ---
                for (String skillId : data.getUnlockedSkills()) {
                    try (PreparedStatement ps = conn.prepareStatement("""
                            INSERT IGNORE INTO player_skills (uuid, skill_id) VALUES (?, ?)
                            """)) {
                        ps.setString(1, uuid);
                        ps.setString(2, skillId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                MythicLogger.debug("プレイヤーセーブ完了: " + uuid);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            MythicLogger.error("プレイヤーセーブエラー (" + uuid + "): " + e.getMessage());
        }
    }

    /* =====================
       内部ヘルパー
     ===================== */

    private static void insertPlayer(Connection conn, UUID uuid) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO players (uuid) VALUES (?)")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }
}
