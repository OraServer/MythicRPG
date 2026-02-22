package com.woxloi.mythicrpg.ui.title;

import com.woxloi.mythicrpg.title.TitleCondition;
import com.woxloi.mythicrpg.title.TitleDefinition;
import com.woxloi.mythicrpg.title.TitleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 称号の表示名・条件文・進捗テキストを生成するユーティリティ。
 * TitleGUI・TitleDetailGUI・スコアボードから共通利用する。
 */
public class TitleRenderer {

    private TitleRenderer() {}

    /**
     * 称号のGUI loreを生成する。
     *
     * @param title      対象称号
     * @param uuid       プレイヤーUUID
     * @param isUnlocked 解放済みか
     * @param isActive   現在セットしているか
     */
    public static List<String> buildTitleLore(TitleDefinition title, UUID uuid,
                                               boolean isUnlocked, boolean isActive) {
        List<String> lore = new ArrayList<>();

        lore.add("§7" + title.getDescription());
        lore.add("");

        // 解放条件
        lore.add("§8--- 解放条件 ---");
        lore.add(buildConditionText(title));

        // 進捗バー
        if (!isUnlocked) {
            lore.add("");
            lore.add(buildProgressBar(title, uuid));
        }

        lore.add("");
        if (isActive) {
            lore.add("§6★ 現在設定中");
        } else if (isUnlocked) {
            lore.add("§aクリックで設定");
        } else {
            lore.add("§8未解放");
        }

        return lore;
    }

    /**
     * 条件テキストを生成する。
     */
    public static String buildConditionText(TitleDefinition title) {
        TitleCondition cond = title.getCondition();
        long threshold = title.getThreshold();

        return switch (cond) {
            case LEVEL          -> "§aレベル §e" + threshold + " §a達成";
            case MOB_KILL       -> "§cMob §e" + threshold + "体 §c討伐";
            case QUEST_COMPLETE -> "§bクエスト §e" + threshold + "回 §b完了";
            case JOB            -> "§6ジョブ選択";
            case MONEY          -> "§6所持金 §e" + String.format("%,d", threshold) + " §6達成";
            case CRAFT          -> "§aアイテム §e" + threshold + "個 §aクラフト";
            default             -> "§7特殊条件";
        };
    }

    /**
     * 進捗バーを生成する（最大20マス）。
     */
    public static String buildProgressBar(TitleDefinition title, UUID uuid) {
        TitleManager.TitleStats stats = TitleManager.getStatsPublic(uuid);
        if (stats == null) return "§8░░░░░░░░░░░░░░░░░░░░ §80%";

        long current = getCurrentValue(title, stats);
        long total   = title.getThreshold();
        if (total <= 0) return "";

        double ratio = Math.min(1.0, (double) current / total);
        int filled   = (int)(ratio * 20);
        int empty    = 20 - filled;

        String bar = "§a" + "█".repeat(filled) + "§8" + "░".repeat(empty);
        int pct = (int)(ratio * 100);
        return bar + " §f" + current + "§8/§f" + total + " §7(" + pct + "%)";
    }

    /**
     * 称号の色付きタグを返す（スコアボード・チャット表示用）。
     */
    public static String getFormattedTag(TitleDefinition title) {
        return title.getDisplayTag();
    }

    private static long getCurrentValue(TitleDefinition title, TitleManager.TitleStats stats) {
        return switch (title.getCondition()) {
            case LEVEL          -> stats.level;
            case MOB_KILL       -> stats.mobKills;
            case QUEST_COMPLETE -> stats.questCompletes;
            case CRAFT          -> stats.craftCount;
            default             -> 0L;
        };
    }
}
