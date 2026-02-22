package com.woxloi.mythicrpg.db.migration;

import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.db.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * バフ/デバフ・称号・ステータスポイント・装備強化用テーブル追加マイグレーション
 */
public class MigrationV2 {

    public static void run() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // ─── 称号テーブル ───
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_titles (
                    uuid      VARCHAR(36)  NOT NULL,
                    title_id  VARCHAR(64)  NOT NULL,
                    PRIMARY KEY (uuid, title_id),
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_active_title (
                    uuid      VARCHAR(36)  NOT NULL PRIMARY KEY,
                    title_id  VARCHAR(64)  NOT NULL,
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_title_stats (
                    uuid            VARCHAR(36) NOT NULL PRIMARY KEY,
                    mob_kills       BIGINT      NOT NULL DEFAULT 0,
                    quest_completes BIGINT      NOT NULL DEFAULT 0,
                    craft_count     BIGINT      NOT NULL DEFAULT 0,
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // ─── ステータスポイントテーブル ───
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_stat_points (
                    uuid        VARCHAR(36) NOT NULL PRIMARY KEY,
                    free_points INT         NOT NULL DEFAULT 0,
                    str_points  INT         NOT NULL DEFAULT 0,
                    vit_points  INT         NOT NULL DEFAULT 0,
                    int_points  INT         NOT NULL DEFAULT 0,
                    agi_points  INT         NOT NULL DEFAULT 0,
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // ─── 装備テーブル ───
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_equipment (
                    uuid       VARCHAR(36) NOT NULL,
                    slot       VARCHAR(20) NOT NULL,
                    item_json  MEDIUMTEXT  NOT NULL,
                    PRIMARY KEY (uuid, slot),
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // ─── コンボ統計テーブル ───
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_combo_stats (
                    uuid          VARCHAR(36) NOT NULL PRIMARY KEY,
                    max_combo     INT         NOT NULL DEFAULT 0,
                    total_combos  BIGINT      NOT NULL DEFAULT 0,
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            MythicLogger.info("マイグレーションV2完了: 称号・ステータスポイント・装備テーブルを追加");

        } catch (SQLException e) {
            MythicLogger.error("マイグレーションV2エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
