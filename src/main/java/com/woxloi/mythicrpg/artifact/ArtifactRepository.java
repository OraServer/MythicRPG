package com.woxloi.mythicrpg.artifact;

import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * アーティファクト関連DB操作。
 *
 * テーブル: player_artifact_log
 *   player_uuid  VARCHAR(36)
 *   piece_id     VARCHAR(64)
 *   acquired_at  BIGINT
 *
 * アーティファクト取得履歴を記録する。
 * セットボーナス自体はオンメモリで管理し、装備スキャンで毎回再計算するため
 * DBには「どのピースを所持しているか」を記録する。
 *
 * ※ 実際の装備情報はバニラのインベントリをそのまま利用するため、
 *    NBTタグ (artifact_id) がついたアイテムがあれば有効とみなす。
 */
public class ArtifactRepository {

    // ─────────────────────────────────────────────
    //  DDL
    // ─────────────────────────────────────────────

    /** テーブルが存在しなければ作成 */
    public static void createTable() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS player_artifact_log (
                    player_uuid  VARCHAR(36) NOT NULL,
                    piece_id     VARCHAR(64) NOT NULL,
                    acquired_at  BIGINT      NOT NULL,
                    PRIMARY KEY (player_uuid, piece_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
             """)) {
            stmt.executeUpdate();
            MythicLogger.debug("player_artifact_log テーブル確認完了");
        } catch (Exception e) {
            MythicLogger.error("artifact テーブル作成失敗: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  書き込み
    // ─────────────────────────────────────────────

    /**
     * アーティファクトピース取得を記録する。
     * 既に記録済みであれば無視 (INSERT IGNORE)。
     */
    public static void logAcquired(UUID uuid, String pieceId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                INSERT IGNORE INTO player_artifact_log
                    (player_uuid, piece_id, acquired_at)
                VALUES (?, ?, ?)
             """)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, pieceId);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (Exception e) {
            MythicLogger.error("artifact ログ保存失敗: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  読み込み
    // ─────────────────────────────────────────────

    /**
     * プレイヤーが過去に取得したピースID一覧を返す。
     */
    public static Set<String> loadAcquired(UUID uuid) {
        Set<String> result = new LinkedHashSet<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT piece_id FROM player_artifact_log
                WHERE player_uuid = ?
                ORDER BY acquired_at ASC
             """)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(rs.getString("piece_id"));
            }
        } catch (Exception e) {
            MythicLogger.error("artifact ログ読み込み失敗: " + e.getMessage());
        }
        return result;
    }

    /**
     * プレイヤーが取得済みのセット別ピース数を返す。
     * key: ArtifactType, value: 取得済みピース数
     */
    public static Map<ArtifactType, Integer> loadAcquiredCounts(UUID uuid) {
        Set<String> pieceIds = loadAcquired(uuid);
        Map<ArtifactType, Integer> counts = new EnumMap<>(ArtifactType.class);
        for (String id : pieceIds) {
            ArtifactPiece p = ArtifactRegistry.get(id);
            if (p != null) counts.merge(p.getSetType(), 1, Integer::sum);
        }
        return counts;
    }

    /**
     * プレイヤーの全ログを削除（管理者用）。
     */
    public static void clearLog(UUID uuid) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM player_artifact_log WHERE player_uuid = ?")) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (Exception e) {
            MythicLogger.error("artifact ログ削除失敗: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  統計
    // ─────────────────────────────────────────────

    /**
     * 全プレイヤー中でピース取得数が最も多いセットTop3を返す。
     * (サーバー全体のアーティファクト普及度統計用)
     */
    public static List<Map.Entry<String, Integer>> topSets() {
        Map<String, Integer> totals = new LinkedHashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT piece_id, COUNT(*) AS cnt
                FROM player_artifact_log
                GROUP BY piece_id
                ORDER BY cnt DESC
                LIMIT 20
             """);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                totals.put(rs.getString("piece_id"), rs.getInt("cnt"));
            }
        } catch (Exception e) {
            MythicLogger.error("artifact 統計取得失敗: " + e.getMessage());
        }
        return new ArrayList<>(totals.entrySet());
    }
}
