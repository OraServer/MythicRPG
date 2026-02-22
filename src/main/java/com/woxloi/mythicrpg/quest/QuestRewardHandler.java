package com.woxloi.mythicrpg.quest;

import com.woxloi.mythicrpg.level.LevelManager;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.entity.Player;

/**
 * QuestPlugin側でクエストが完了したときに呼ばれるハンドラ。
 *
 * QuestPluginの completeQuest() 内から
 * Bukkit.getPluginManager().callEvent(new QuestCompleteEvent(...)) を
 * 呼ぶことで連携するのが理想。
 * ここでは QuestCompleteListener 経由で受け取る設計。
 *
 * 報酬テーブル:
 *  - 基本EXP: 50 × クエスト難易度係数
 *  - デイリークエスト: ×1.5
 *  - ウィークリークエスト: ×3.0
 *  - チェーンクエスト完了: ボーナスEXP 200
 */
public class QuestRewardHandler {

    /** 通常クエスト完了時 */
    public static void onQuestComplete(Player player, String questId, String questName) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        int baseExp = calcBaseExp(questId);
        LevelManager.addExp(player, baseExp);

        player.sendMessage("§6§l[Quest→RPG] §eklエスト報酬: §f+" + baseExp + "EXP");
    }

    /** デイリークエスト完了時 */
    public static void onDailyQuestComplete(Player player, String questId) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        int baseExp = calcBaseExp(questId);
        int bonus = (int) (baseExp * 1.5);
        LevelManager.addExp(player, bonus);

        player.sendMessage("§a§l[デイリー] §e+" + bonus + "EXP §7(×1.5ボーナス)");
    }

    /** ウィークリークエスト完了時 */
    public static void onWeeklyQuestComplete(Player player, String questId) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        int baseExp = calcBaseExp(questId);
        int bonus = baseExp * 3;
        LevelManager.addExp(player, bonus);

        player.sendMessage("§6§l[ウィークリー] §e+" + bonus + "EXP §7(×3.0ボーナス)");
    }

    /** クエストチェーン完了ボーナス */
    public static void onChainQuestComplete(Player player, String chainId) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        int bonus = 200;
        LevelManager.addExp(player, bonus);

        player.sendMessage("§d§l[チェーン完了] §e+" + bonus + "EXP §7(チェーンボーナス)");
    }

    /** 民間クエスト完了時（依頼側/受注側両方） */
    public static void onPlayerQuestComplete(Player player, boolean isCreator) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        int exp = isCreator ? 10 : 30;  // 受注側の方が多め
        LevelManager.addExp(player, exp);
    }

    /** クエストIDからベースEXPを計算（命名規則で難易度推定） */
    private static int calcBaseExp(String questId) {
        if (questId == null) return 50;
        String lower = questId.toLowerCase();

        if (lower.contains("weekly"))  return 300;
        if (lower.contains("daily"))   return 80;
        if (lower.contains("boss"))    return 200;
        if (lower.contains("hard"))    return 150;
        if (lower.contains("chapter")) return 120;
        return 50;  // デフォルト
    }
}
