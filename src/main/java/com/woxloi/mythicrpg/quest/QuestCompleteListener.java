package com.woxloi.mythicrpg.quest;

import com.woxloi.mythicrpg.title.TitleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * QuestCompleteEvent を受け取るリスナー。
 *
 * QuestPlugin が QuestCompleteEvent を発火 → このリスナーがキャッチ →
 * MythicRPGのEXP付与・称号チェックなど実行。
 */
public class QuestCompleteListener implements Listener {

    @EventHandler
    public void onQuestComplete(QuestCompleteEvent e) {
        Player player = e.getPlayer();
        String questId = e.getQuestId();
        String questName = e.getQuestName();

        switch (e.getCategory()) {
            case NORMAL -> QuestRewardHandler.onQuestComplete(player, questId, questName);
            case DAILY  -> QuestRewardHandler.onDailyQuestComplete(player, questId);
            case WEEKLY -> QuestRewardHandler.onWeeklyQuestComplete(player, questId);
            case CHAIN  -> QuestRewardHandler.onChainQuestComplete(player, questId);
            case PLAYER_QUEST_CREATOR  -> QuestRewardHandler.onPlayerQuestComplete(player, true);
            case PLAYER_QUEST_ACCEPTOR -> QuestRewardHandler.onPlayerQuestComplete(player, false);
        }

        // 称号チェック（クエスト完了数など）
        TitleManager.checkUnlock(player);
    }
}
