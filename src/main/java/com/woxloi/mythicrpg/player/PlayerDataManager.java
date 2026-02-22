package com.woxloi.mythicrpg.player;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.combat.CombatListener;
import com.woxloi.mythicrpg.db.PlayerRepository;
import com.woxloi.mythicrpg.equipment.EquipmentManager;
import com.woxloi.mythicrpg.equipment.db.EquipmentRepository;
import com.woxloi.mythicrpg.equipment.model.EquipSlot;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.stats.StatPoint;
import com.woxloi.mythicrpg.stats.StatPointManager;
import com.woxloi.mythicrpg.ui.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * オンメモリキャッシュ + 非同期MySQL保存。
 *
 *  load → 非同期でDB読み込み → メインスレッドでキャッシュ登録・装備適用・スコアボード初期化
 *  save → 非同期でDB書き込み → キャッシュから削除
 */
public class PlayerDataManager {

    private static final Map<UUID, PlayerData> dataMap = new HashMap<>();

    /* =====================
       取得
     ===================== */
    public static PlayerData get(Player player) {
        return dataMap.get(player.getUniqueId());
    }

    public static PlayerData get(UUID uuid) {
        return dataMap.get(uuid);
    }

    /* =====================
       ロード（非同期）
     ===================== */
    public static void load(Player player) {
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(MythicRPG.getInstance(), () -> {
            // DBから非同期読み込み（プレイヤーデータ + 装備データ）
            PlayerData data               = PlayerRepository.load(uuid);
            EnumMap<EquipSlot, RpgItem> equips = EquipmentRepository.load(uuid);

            // メインスレッドでキャッシュ登録・装備適用
            Bukkit.getScheduler().runTask(MythicRPG.getInstance(), () -> {
                if (!player.isOnline()) return; // ロード中に切断した場合はスキップ

                dataMap.put(uuid, data);

                // 装備データをEquipmentManagerに登録
                EquipmentManager.loadPlayer(uuid, equips);

                // 装備ステータスをPlayerDataに反映
                EquipmentManager.applyStats(player);

                // Bukkit の max_health アトリビュートを PlayerData に合わせる
                CombatListener.applyMaxHealthAttribute(player, data);

                // スコアボード更新
                ScoreboardManager.update(player);
            });
        });
    }

    /* =====================
       セーブ（非同期）
     ===================== */
    public static void save(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = dataMap.remove(uuid);
        if (data == null) return;

        // 装備データもアンロード
        EquipmentManager.unloadPlayer(uuid);

        // キャッシュから削除後に非同期保存
        Bukkit.getScheduler().runTaskAsynchronously(MythicRPG.getInstance(), () -> {
            PlayerRepository.save(data);
        });
    }

    /**
     * サーバーシャットダウン時に全プレイヤーを同期保存する。
     * onDisable で呼ぶこと。
     */
    public static void saveAll() {
        for (PlayerData data : dataMap.values()) {
            PlayerRepository.save(data);   // シャットダウン時は同期保存
        }
        dataMap.clear();
    }
}
