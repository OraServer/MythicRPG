package com.woxloi.mythicrpg.player;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private static final Map<UUID, PlayerData> dataMap = new HashMap<>();

    public static PlayerData get(Player player) {
        return dataMap.get(player.getUniqueId());
    }

    public static void load(Player player) {
        UUID uuid = player.getUniqueId();

        // 今は新規作成のみ（保存は後）
        PlayerData data = new PlayerData(uuid);
        dataMap.put(uuid, data);
    }

    public static void save(Player player) {
        UUID uuid = player.getUniqueId();

        PlayerData data = dataMap.get(uuid);
        if (data == null) return;

        // STEP3以降でYAML/DB保存を書く
        dataMap.remove(uuid);
    }
}
