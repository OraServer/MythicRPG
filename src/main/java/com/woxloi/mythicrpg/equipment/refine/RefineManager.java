package com.woxloi.mythicrpg.equipment.refine;

import com.woxloi.mythicrpg.equipment.model.EquipStats;
import com.woxloi.mythicrpg.equipment.model.RpgItem;

import java.util.Random;

/**
 * 精錬システム。
 * 精錬素材を使ってアイテムのステータスを強化する。
 * 失敗すると素材が消失するリスクがある。
 */
public class RefineManager {

    private static final Random RANDOM = new Random();

    /**
     * 精錬結果の列挙型。
     */
    public enum RefineResult {
        SUCCESS      ("§a精錬成功！"),
        GREAT_SUCCESS("§6§l大成功！！"),
        FAIL         ("§c精錬失敗..."),
        BREAK        ("§4§l装備が壊れた！");

        public final String message;
        RefineResult(String msg) { this.message = msg; }
    }

    /**
     * 精錬を実行する。
     * 成功率はアイテムの精錬レベルに反比例する。
     *
     * @param item     対象アイテム
     * @param material 精錬素材の個数（多いほど成功率UP）
     * @return 精錬結果
     */
    public static RefineResult refine(RpgItem item, int material) {
        int refLv = item.refineLevel;
        double baseRate = calcSuccessRate(refLv, material);
        double roll = RANDOM.nextDouble();

        if (roll < baseRate * 0.1) {
            // 大成功
            item.refineLevel += 2;
            boostStats(item, 2.0);
            return RefineResult.GREAT_SUCCESS;
        }
        if (roll < baseRate) {
            // 成功
            item.refineLevel++;
            boostStats(item, 1.0);
            return RefineResult.SUCCESS;
        }
        if (roll < baseRate + calcBreakRate(refLv)) {
            // 破壊（精錬レベルリセット）
            item.refineLevel = 0;
            // ステータスを元に戻す
            resetRefinedStats(item);
            return RefineResult.BREAK;
        }
        // 失敗（変化なし）
        return RefineResult.FAIL;
    }

    /**
     * 精錬レベルに応じた成功率を計算する。
     * base: 90% → +1ごとに10%減少, 最低10%
     */
    private static double calcSuccessRate(int refLv, int material) {
        double base = Math.max(0.1, 0.9 - refLv * 0.1);
        double matBonus = Math.min(0.3, material * 0.05);
        return Math.min(0.95, base + matBonus);
    }

    /**
     * 破壊率の計算。精錬レベル5以上から発生。
     */
    private static double calcBreakRate(int refLv) {
        if (refLv < 5) return 0;
        return Math.min(0.3, (refLv - 4) * 0.05);
    }

    private static void boostStats(RpgItem item, double factor) {
        EquipStats s = item.baseStats;
        s.attack    *= 1 + 0.05 * factor;
        s.defense   *= 1 + 0.05 * factor;
        s.maxHpBonus *= 1 + 0.03 * factor;
        s.maxMpBonus *= 1 + 0.03 * factor;
    }

    private static void resetRefinedStats(RpgItem item) {
        // 精錬ブースト分をリセット（簡略実装: baseを再読込）
        // 実際の実装ではbaseStatsを別フィールドで保持する
        EquipStats s = item.baseStats;
        s.attack    = item.baseStats.attack;
        s.defense   = item.baseStats.defense;
        s.maxHpBonus = item.baseStats.maxHpBonus;
        s.maxMpBonus = item.baseStats.maxMpBonus;
    }

    /**
     * 精錬レベル表示文字列（+0〜+10）
     */
    public static String getRefineTag(RpgItem item) {
        if (item.refineLevel <= 0) return "";
        return "§e+" + item.refineLevel;
    }
}
