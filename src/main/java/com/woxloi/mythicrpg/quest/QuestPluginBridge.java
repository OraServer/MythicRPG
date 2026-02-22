package com.woxloi.mythicrpg.quest;

import com.woxloi.mythicrpg.core.MythicLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * QuestPluginとの依存関係ブリッジ。
 *
 * QuestPluginはsoftdependとして扱う。
 * isAvailable() == true の時のみ連携機能を有効化する。
 *
 * QuestPluginのAPIで使う主なクラス:
 *  - com.woxloi.questplugin.manager.ActiveQuestManager
 *  - com.woxloi.questplugin.manager.QuestConfigManager
 *  - com.woxloi.questplugin.party.PartyManager
 *  - com.woxloi.questplugin.features.QuestChainManager
 */
public class QuestPluginBridge {

    private static boolean available = false;
    private static Plugin questPlugin = null;

    /** プラグイン起動時に呼ぶ */
    public static void init() {
        questPlugin = Bukkit.getPluginManager().getPlugin("QuestPlugin");
        available = (questPlugin != null && questPlugin.isEnabled());

        if (available) {
            MythicLogger.info("QuestPlugin連携を有効化しました");
        } else {
            MythicLogger.info("QuestPlugin未検出 - クエスト連携は無効");
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static Plugin getQuestPlugin() {
        return questPlugin;
    }

    /**
     * QuestPluginのActiveQuestManagerから、プレイヤーがクエスト中かを確認。
     * リフレクション経由で呼ぶことでコンパイル時依存を排除。
     */
    public static boolean isPlayerQuesting(org.bukkit.entity.Player player) {
        if (!available) return false;
        try {
            Class<?> aqm = Class.forName("com.woxloi.questplugin.manager.ActiveQuestManager");
            var method = aqm.getMethod("isQuesting", org.bukkit.entity.Player.class);
            // Kotlinのobjectはフィールド INSTANCE を持つ
            var instance = aqm.getField("INSTANCE").get(null);
            return (boolean) method.invoke(instance, player);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * プレイヤーが参加しているQuestPluginパーティーメンバーを取得。
     */
    @SuppressWarnings("unchecked")
    public static java.util.List<org.bukkit.entity.Player> getQuestPartyMembers(org.bukkit.entity.Player player) {
        if (!available) return java.util.List.of();
        try {
            Class<?> pm = Class.forName("com.woxloi.questplugin.party.PartyManager");
            var instance = pm.getField("INSTANCE").get(null);
            var method = pm.getMethod("getPartyMembers", org.bukkit.entity.Player.class);
            return (java.util.List<org.bukkit.entity.Player>) method.invoke(instance, player);
        } catch (Exception e) {
            return java.util.List.of(player);
        }
    }
}
