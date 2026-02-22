package com.woxloi.mythicrpg.equipment.db;

import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.db.DatabaseManager;
import com.woxloi.mythicrpg.equipment.EquipmentRegistry;
import com.woxloi.mythicrpg.equipment.model.EquipSlot;
import com.woxloi.mythicrpg.equipment.model.RpgItem;

import java.sql.*;
import java.util.EnumMap;
import java.util.UUID;

/**
 * player_equipment テーブルへのCRUD。
 * TableInitializerでテーブル作成済みを前提とする。
 */
public class EquipmentRepository {

    /* =====================
       ロード
     ===================== */
    public static EnumMap<EquipSlot, RpgItem> load(UUID uuid) {
        EnumMap<EquipSlot, RpgItem> result = new EnumMap<>(EquipSlot.class);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT slot, item_id, enhance_level FROM player_equipment WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    EquipSlot slot   = EquipSlot.valueOf(rs.getString("slot"));
                    String    itemId = rs.getString("item_id");
                    int       enh    = rs.getInt("enhance_level");

                    RpgItem item = EquipmentRegistry.get(itemId);
                    if (item != null) {
                        item.enhanceLevel = enh;
                        result.put(slot, item);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (SQLException e) {
            MythicLogger.error("装備ロードエラー (" + uuid + "): " + e.getMessage());
        }
        return result;
    }

    /* =====================
       スロット単体セーブ（UPSERT）
     ===================== */
    public static void saveSlot(UUID uuid, EquipSlot slot, RpgItem item) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                     INSERT INTO player_equipment (uuid, slot, item_id, enhance_level)
                     VALUES (?, ?, ?, ?)
                     ON DUPLICATE KEY UPDATE item_id = VALUES(item_id), enhance_level = VALUES(enhance_level)
                     """)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, slot.name());
            ps.setString(3, item.id);
            ps.setInt(4, item.enhanceLevel);
            ps.executeUpdate();
        } catch (SQLException e) {
            MythicLogger.error("装備スロット保存エラー: " + e.getMessage());
        }
    }

    /* =====================
       スロット削除
     ===================== */
    public static void deleteSlot(UUID uuid, EquipSlot slot) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM player_equipment WHERE uuid = ? AND slot = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, slot.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            MythicLogger.error("装備スロット削除エラー: " + e.getMessage());
        }
    }

    /* =====================
       全スロット保存
     ===================== */
    public static void saveAll(UUID uuid, EnumMap<EquipSlot, RpgItem> equips) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // まず既存を全削除してから再INSERT
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM player_equipment WHERE uuid = ?")) {
                    del.setString(1, uuid.toString());
                    del.executeUpdate();
                }
                for (var entry : equips.entrySet()) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO player_equipment (uuid, slot, item_id, enhance_level) VALUES (?, ?, ?, ?)")) {
                        ins.setString(1, uuid.toString());
                        ins.setString(2, entry.getKey().name());
                        ins.setString(3, entry.getValue().id);
                        ins.setInt(4, entry.getValue().enhanceLevel);
                        ins.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            MythicLogger.error("全装備保存エラー (" + uuid + "): " + e.getMessage());
        }
    }
}
