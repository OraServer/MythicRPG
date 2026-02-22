package com.woxloi.mythicrpg.player;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.db.PlayerRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * オンメモリキャッシュ + 非同期MySQL保存。
 *
 *  load → 非同期でDB読み込み → メインスレッドでキャッシュに登録
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
            // DBから非同期読み込み
            PlayerData data = PlayerRepository.load(uuid);

            // メインスレッドでキャッシュ登録
            Bukkit.getScheduler().runTask(MythicRPG.getInstance(), () -> {
                dataMap.put(uuid, data);
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
