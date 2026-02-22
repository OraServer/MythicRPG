package com.woxloi.mythicrpg.equipment.transfer;

import com.woxloi.mythicrpg.equipment.model.EquipSlot;
import com.woxloi.mythicrpg.equipment.model.EquipStats;
import com.woxloi.mythicrpg.equipment.model.RpgItem;

/**
 * ステータス継承システム。
 * 高レアリティ装備から低レアリティ装備に一部ステータスを引き継ぐ。
 * 元の装備は消滅し、対象装備にステータスが付与される。
 */
public class TransferManager {

    /** 継承できるステータスの割合 */
    private static final double TRANSFER_RATE = 0.3;

    /**
     * sourceのステータスをtargetに一部継承する。
     * sourceは消滅する（呼び出し元で削除すること）。
     *
     * @param source 継承元（消滅）
     * @param target 継承先
     * @return エラーメッセージ（null = 成功）
     */
    public static String transfer(RpgItem source, RpgItem target) {
        if (source.slot != target.slot) {
            return "異なるスロットのアイテムにはステータスを引き継げません";
        }
        if (source.rarity.ordinal() <= target.rarity.ordinal()) {
            return "継承元は継承先より高レアリティである必要があります";
        }
        if (source.id.equals(target.id)) {
            return "同じアイテムには継承できません";
        }

        // ステータスを計算して付与
        EquipStats sourceStats = source.getEffectiveStats();
        target.baseStats.attack    += sourceStats.attack    * TRANSFER_RATE;
        target.baseStats.defense   += sourceStats.defense   * TRANSFER_RATE;
        target.baseStats.maxHpBonus += sourceStats.maxHpBonus * TRANSFER_RATE;
        target.baseStats.maxMpBonus += sourceStats.maxMpBonus * TRANSFER_RATE;
        target.baseStats.magicPower += sourceStats.magicPower * TRANSFER_RATE;

        // 強化レベルの一部継承（最大+3まで）
        int enhanceInherit = Math.min(3, source.enhanceLevel / 2);
        target.enhanceLevel = Math.min(target.maxEnhance, target.enhanceLevel + enhanceInherit);

        return null; // 成功
    }

    /**
     * 継承プレビューとして、どれだけステータスが増えるかを返す。
     */
    public static EquipStats previewTransfer(RpgItem source, RpgItem target) {
        if (source == null || target == null) return new EquipStats();
        EquipStats sourceStats = source.getEffectiveStats();
        EquipStats preview = new EquipStats();
        preview.attack    = sourceStats.attack    * TRANSFER_RATE;
        preview.defense   = sourceStats.defense   * TRANSFER_RATE;
        preview.maxHpBonus = sourceStats.maxHpBonus * TRANSFER_RATE;
        preview.maxMpBonus = sourceStats.maxMpBonus * TRANSFER_RATE;
        preview.magicPower = sourceStats.magicPower * TRANSFER_RATE;
        return preview;
    }
}
