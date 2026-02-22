package com.woxloi.mythicrpg.db;

import com.woxloi.mythicrpg.core.MythicLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 起動時にDBテーブルが存在しなければ自動作成する。
 *
 * テーブル構成:
 *   players           - プレイヤー基本情報 (UUID, job, level, exp)
 *   player_stats      - ステータス (hp, mp, sp, attack)
 *   player_skills     - 解放済みスキル一覧
 *   player_equipment  - 装備スロット別データ
 *   player_stat_points- ステータスポイント振り分け
 *   player_titles     - 解放済み称号・アクティブ称号
 */
public class TableInitializer {

    public static void createTables() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // players テーブル
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid        VARCHAR(36)  NOT NULL PRIMARY KEY,
                    job         VARCHAR(20)  DEFAULT NULL,
                    level       INT          NOT NULL DEFAULT 1,
                    exp         DOUBLE       NOT NULL DEFAULT 0,
                    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                             ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // player_stats テーブル
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_stats (
                    uuid        VARCHAR(36)  NOT NULL PRIMARY KEY,
                    max_hp      DOUBLE       NOT NULL DEFAULT 0,
                    hp          DOUBLE       NOT NULL DEFAULT 0,
                    max_mp      DOUBLE       NOT NULL DEFAULT 0,
                    mp          DOUBLE       NOT NULL DEFAULT 0,
                    max_sp      DOUBLE       NOT NULL DEFAULT 0,
                    sp          DOUBLE       NOT NULL DEFAULT 0,
                    attack      DOUBLE       NOT NULL DEFAULT 0,
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // player_skills テーブル
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_skills (
                    uuid        VARCHAR(36)  NOT NULL,
                    skill_id    VARCHAR(64)  NOT NULL,
                    PRIMARY KEY (uuid, skill_id),
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // player_equipment テーブル（装備スロット保存）
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_equipment (
                    uuid            VARCHAR(36)  NOT NULL,
                    slot            VARCHAR(20)  NOT NULL,
                    item_id         VARCHAR(64)  NOT NULL,
                    enhance_level   INT          NOT NULL DEFAULT 0,
                    PRIMARY KEY (uuid, slot),
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // player_stat_points テーブル（ステータスポイント振り分け）
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_stat_points (
                    uuid        VARCHAR(36)  NOT NULL PRIMARY KEY,
                    str_pts     INT          NOT NULL DEFAULT 0,
                    vit_pts     INT          NOT NULL DEFAULT 0,
                    int_pts     INT          NOT NULL DEFAULT 0,
                    agi_pts     INT          NOT NULL DEFAULT 0,
                    free_pts    INT          NOT NULL DEFAULT 0,
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // player_titles テーブル（称号保存）
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_titles (
                    uuid            VARCHAR(36)  NOT NULL,
                    title_name      VARCHAR(64)  NOT NULL,
                    is_active       BOOLEAN      NOT NULL DEFAULT FALSE,
                    PRIMARY KEY (uuid, title_name),
                    FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            MythicLogger.info("DBテーブルの確認・作成完了 (6テーブル)");

        } catch (SQLException e) {
            MythicLogger.error("テーブル作成中にエラー: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
