package com.woxloi.mythicrpg.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * QuestPluginがクエストを完了したときにBukkitイベントとして発火するカスタムイベント。
 *
 * QuestPlugin側での発火方法（Kotlin）:
 * ```kotlin
 * val event = QuestCompleteEvent(player, quest.id, quest.name, questCategory)
 * Bukkit.getPluginManager().callEvent(event)
 * ```
 *
 * MythicRPG側でこのイベントを @EventHandler で受け取って報酬付与する。
 */
public class QuestCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public enum QuestCategory {
        NORMAL,
        DAILY,
        WEEKLY,
        CHAIN,
        PLAYER_QUEST_CREATOR,   // 民間クエスト依頼主
        PLAYER_QUEST_ACCEPTOR   // 民間クエスト受注者
    }

    private final Player player;
    private final String questId;
    private final String questName;
    private final QuestCategory category;

    public QuestCompleteEvent(Player player, String questId, String questName, QuestCategory category) {
        this.player    = player;
        this.questId   = questId;
        this.questName = questName;
        this.category  = category;
    }

    public Player getPlayer()       { return player; }
    public String getQuestId()      { return questId; }
    public String getQuestName()    { return questName; }
    public QuestCategory getCategory() { return category; }

    @NotNull
    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
