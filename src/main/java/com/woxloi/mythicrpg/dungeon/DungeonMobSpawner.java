package com.woxloi.mythicrpg.dungeon;

import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.element.ElementManager;
import com.woxloi.mythicrpg.element.ElementType;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * ダンジョンフロアのMythicMobsスポーンを担当する。
 *
 * スポーン後に属性タグ（ElementManager）とセッションタグを付与し、
 * 全滅検知用のUUIDセットをDungeonSessionに登録する。
 */
public class DungeonMobSpawner {

    /** セッションIDをMobエンティティに紐付けるNBTキー文字列 */
    public static final String META_SESSION_ID = "mrpg_dungeon_session";

    private DungeonMobSpawner() {}

    /**
     * 指定フロアのMobを全員スポーンさせ、
     * セッションのアクティブMob UUIDセットに登録する。
     *
     * @param session   対象セッション
     * @param floor     フロア番号
     * @param center    スポーン基準座標（プレイヤー位置）
     */
    public static void spawnFloor(DungeonSession session, int floor, Location center) {
        DungeonDefinition def = session.getDefinition();
        List<String> mobIds = def.getMobsForFloor(floor);

        boolean isBossFloor = mobIds.isEmpty() && !def.getBossId().isBlank();
        if (isBossFloor) {
            spawnBoss(session, center);
            return;
        }

        List<int[]> offsets = def.getSpawnOffsets();
        Set<UUID> spawnedUuids = new HashSet<>();

        for (int i = 0; i < mobIds.size(); i++) {
            String mobId = mobIds.get(i);
            int[] offset = offsets.get(i % offsets.size());

            Location spawnLoc = center.clone().add(offset[0], offset[1], offset[2]);
            UUID uuid = spawnMythicMob(mobId, spawnLoc, session.getSessionId(), def.getMobElement());
            if (uuid != null) spawnedUuids.add(uuid);
        }

        session.setActiveMobUuids(spawnedUuids);
        MythicLogger.debug("ダンジョン[" + def.getId() + "] F" + floor
                + " : " + spawnedUuids.size() + "体スポーン");
    }

    private static void spawnBoss(DungeonSession session, Location center) {
        DungeonDefinition def = session.getDefinition();
        UUID uuid = spawnMythicMob(def.getBossId(), center, session.getSessionId(), def.getBossElement());
        Set<UUID> set = new HashSet<>();
        if (uuid != null) set.add(uuid);
        session.setActiveMobUuids(set);

        // 参加者にボス出現を通知
        session.broadcastToParticipants("§c§l⚔ ボス出現！ §r§e" + def.getBossId());
    }

    /**
     * MythicMobsでMobをスポーンさせ、属性とセッションIDを付与する。
     * @return スポーンしたEntityのUUID（失敗時null）
     */
    private static UUID spawnMythicMob(String mythicId, Location loc,
                                        String sessionId, ElementType element) {
        try {
            ActiveMob am = MythicBukkit.inst().getMobManager().spawnMob(mythicId, loc);
            if (am == null) { MythicLogger.warn("スポーン失敗: " + mythicId); return null; }

            Entity entity = am.getEntity().getBukkitEntity();

            // 属性付与（ElementManager経由でPDCに書き込む）
            if (element != ElementType.NONE) {
                ElementManager.setMobElement(entity, element);
            }

            // セッションIDタグ付与（Mob全滅検知用）
            entity.getPersistentDataContainer().set(
                com.woxloi.mythicrpg.MythicRPG.getInstance()
                    .key(META_SESSION_ID),
                org.bukkit.persistence.PersistentDataType.STRING,
                sessionId
            );

            return entity.getUniqueId();
        } catch (Exception e) {
            MythicLogger.warn("MythicMob スポーンエラー [" + mythicId + "]: " + e.getMessage());
            return null;
        }
    }
}
