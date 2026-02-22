package com.woxloi.mythicrpg.quest;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * QuestPlugin の PartyManager と MythicRPG の情報を同期するユーティリティ。
 *
 * MythicRPGは独自パーティーシステムを持たず、
 * QuestPluginのPartyManagerを参照する設計。
 *
 * 主な用途:
 * - EXPをパーティー内で分配する
 * - スキルのAOE対象をパーティーメンバーで共有する
 */
public class QuestPartySync {

    /**
     * パーティーメンバーを取得（QuestPlugin連携時）。
     * QuestPlugin未導入の場合はプレイヤー自身のみのリストを返す。
     */
    public static List<Player> getPartyMembers(Player player) {
        if (!QuestPluginBridge.isAvailable()) return List.of(player);
        List<Player> members = QuestPluginBridge.getQuestPartyMembers(player);
        return members.isEmpty() ? List.of(player) : members;
    }

    /**
     * EXPをパーティー全員に分配する。
     * 分配率: リーダー 60%、メンバー 40% × 人数割り
     */
    public static void distributeExp(Player source, int totalExp,
                                     com.woxloi.mythicrpg.level.LevelManager levelManager) {
        List<Player> members = getPartyMembers(source);

        if (members.size() <= 1) {
            // ソロ → 全額
            com.woxloi.mythicrpg.level.LevelManager.addExp(source, totalExp);
            return;
        }

        // パーティー全員に均等分配（ボーナス10%を上乗せ）
        int bonus = (int) (totalExp * 1.1);
        int share = bonus / members.size();

        for (Player member : members) {
            com.woxloi.mythicrpg.level.LevelManager.addExp(member, share);
            if (!member.equals(source)) {
                member.sendMessage("§7[パーティーEXP] §e+" + share + "EXP");
            }
        }
    }

    /** パーティーに所属しているか */
    public static boolean isInParty(Player player) {
        return getPartyMembers(player).size() > 1;
    }
}
