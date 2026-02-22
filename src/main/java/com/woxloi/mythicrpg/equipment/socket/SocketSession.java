package com.woxloi.mythicrpg.equipment.socket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SocketGUIでプレイヤーが選択中の宝石を一時保存するセッションクラス。
 */
public class SocketSession {

    private static final Map<UUID, GemType> selectedGems = new HashMap<>();

    public static void setSelectedGem(UUID uuid, GemType gem) {
        selectedGems.put(uuid, gem);
    }

    public static GemType getSelectedGem(UUID uuid) {
        return selectedGems.get(uuid);
    }

    public static void clearSelectedGem(UUID uuid) {
        selectedGems.remove(uuid);
    }

    public static void clearAll() {
        selectedGems.clear();
    }
}
