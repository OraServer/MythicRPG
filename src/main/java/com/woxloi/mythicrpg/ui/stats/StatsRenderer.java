package com.woxloi.mythicrpg.ui.stats;

import com.woxloi.mythicrpg.equipment.EquipmentManager;
import com.woxloi.mythicrpg.equipment.model.EquipStats;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.title.TitleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * プレイヤーステータスのlore・テキスト生成を一元管理するユーティリティ。
 * StatDetailGUI, ProfileGUI, ActionBarTaskなどから共通して利用する。
 */
public class StatsRenderer {

    private StatsRenderer() {}

    /**
     * ステータス一覧lore（装備・バフ込み全ステータス）を生成する。
     */
    public static List<String> buildFullStatsLore(PlayerData data, org.bukkit.entity.Player player) {
        EquipStats equip = EquipmentManager.getTotalStats(player);
        List<String> lore = new ArrayList<>();

        lore.add("§8─────────────────");
        lore.add("§e§l基本ステータス");
        lore.add("§aHp  §f" + fmt(data.getHp())   + " §8/ §f" + fmt(data.getMaxHp()));
        lore.add("§bMP  §f" + fmt(data.getMp())   + " §8/ §f" + fmt(data.getMaxMp()));
        lore.add("§6SP  §f" + fmt(data.getSp())   + " §8/ §f" + fmt(data.getMaxSp()));
        lore.add("");
        lore.add("§e§l戦闘ステータス");
        lore.add("§c攻撃力  §f" + fmt(data.getAttack() + equip.attack));
        lore.add("§7防御力  §f" + fmt(equip.defense));
        lore.add("§d魔 力   §f" + fmt(equip.magicPower));
        lore.add("§e会心率  §f" + pct(equip.critRate));
        lore.add("§e会心倍率 §f+" + pct(equip.critDamage));
        lore.add("§a移動速度 §f+" + String.format("%.2f", equip.speed));
        lore.add("");

        // 装備ボーナス内訳
        if (equip.attack > 0 || equip.defense > 0 || equip.maxHpBonus > 0) {
            lore.add("§8─── 装備ボーナス ───");
            if (equip.attack    > 0) lore.add("§c  +ATK §f" + fmt(equip.attack));
            if (equip.defense   > 0) lore.add("§7  +DEF §f" + fmt(equip.defense));
            if (equip.maxHpBonus> 0) lore.add("§a  +MaxHP §f" + fmt(equip.maxHpBonus));
            if (equip.maxMpBonus> 0) lore.add("§b  +MaxMP §f" + fmt(equip.maxMpBonus));
        }

        return lore;
    }

    /**
     * アクションバー用のコンパクトなステータス文字列を生成する。
     */
    public static String buildActionBarText(PlayerData data) {
        return "§aHP §c" + (int)data.getHp() + "§8/§c" + (int)data.getMaxHp()
             + "  §bMP §3" + (int)data.getMp() + "§8/§3" + (int)data.getMaxMp()
             + "  §6SP §e" + (int)data.getSp() + "§8/§e" + (int)data.getMaxSp();
    }

    /**
     * スコアボード用の短縮ステータス文字列を生成する。
     */
    public static List<String> buildScoreboardLines(PlayerData data, UUID uuid) {
        List<String> lines = new ArrayList<>();
        String titleTag = TitleManager.getDisplayTag(uuid);

        lines.add("§8" + "─".repeat(14));
        if (titleTag != null) lines.add(titleTag);
        lines.add("§aLv §e" + data.getLevel());
        lines.add("§cHP §f" + (int)data.getHp() + "/" + (int)data.getMaxHp());
        lines.add("§bMP §f" + (int)data.getMp() + "/" + (int)data.getMaxMp());
        lines.add("§6SP §f" + (int)data.getSp() + "/" + (int)data.getMaxSp());
        lines.add("§8" + "─".repeat(14));
        return lines;
    }

    // ─── ヘルパー ─────────────────────────────────

    private static String fmt(double v) { return String.valueOf((int)v); }

    private static String pct(double v) {
        return String.format("%.1f%%", v * 100);
    }
}
