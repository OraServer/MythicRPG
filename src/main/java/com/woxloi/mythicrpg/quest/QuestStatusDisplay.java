package com.woxloi.mythicrpg.quest;

import org.bukkit.entity.Player;

/**
 * QuestPluginの進行状況をMythicRPGのScoreboardに追記するユーティリティ。
 * ScoreboardTaskから呼ばれる。
 */
public class QuestStatusDisplay {

    /**
     * プレイヤーのアクティブクエスト情報を取得して文字列で返す。
     * QuestPlugin未導入なら空文字を返す。
     */
    public static String getActiveQuestLine(Player player) {
        if (!QuestPluginBridge.isAvailable()) return "";

        try {
            Class<?> aqm = Class.forName("com.woxloi.questplugin.manager.ActiveQuestManager");
            var instance = aqm.getField("INSTANCE").get(null);

            boolean questing = (boolean) aqm.getMethod("isQuesting", Player.class)
                    .invoke(instance, player);
            if (!questing) return "";

            // QuestData取得
            Object questData = aqm.getMethod("getQuest", Player.class).invoke(instance, player);
            if (questData == null) return "";

            String questName = (String) questData.getClass().getMethod("getName").invoke(questData);

            // PlayerQuestData取得（progress）
            Object playerData = aqm.getMethod("getPlayerData", java.util.UUID.class)
                    .invoke(instance, player.getUniqueId());
            int progress = 0;
            int amount   = 0;
            if (playerData != null) {
                progress = (int) playerData.getClass().getField("progress").get(playerData);
                amount   = (int) questData.getClass().getMethod("getAmount").invoke(questData);
            }

            return "§6[Quest] §f" + questName + " §7" + progress + "/" + amount;

        } catch (Exception e) {
            return "";
        }
    }
}
