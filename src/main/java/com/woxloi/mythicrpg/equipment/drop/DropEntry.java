package com.woxloi.mythicrpg.equipment.drop;

import com.woxloi.mythicrpg.equipment.model.EquipRarity;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.random.RandomItemGenerator;

import java.util.Random;

/**
 * ドロップテーブルの1エントリ。
 * 固定アイテムIDまたはランダム生成パラメータを持つ。
 */
public class DropEntry {

    /** 重み（大きいほど出やすい） */
    public final double weight;

    /** 固定アイテムID（null = ランダム生成） */
    public final String fixedItemId;

    /** ランダム生成する場合のレアリティ（null = ランダム選択） */
    public final EquipRarity rarityOverride;

    /**
     * 固定アイテムIDのエントリ。
     */
    public DropEntry(double weight, String fixedItemId) {
        this.weight        = weight;
        this.fixedItemId   = fixedItemId;
        this.rarityOverride = null;
    }

    /**
     * ランダム生成エントリ。
     */
    public DropEntry(double weight, EquipRarity rarityOverride) {
        this.weight         = weight;
        this.fixedItemId    = null;
        this.rarityOverride = rarityOverride;
    }

    /**
     * アイテムを生成する。
     */
    public RpgItem generateItem(Random random) {
        if (fixedItemId != null) {
            // EquipmentRegistryから取得
            return com.woxloi.mythicrpg.equipment.EquipmentRegistry.get(fixedItemId);
        }
        // ランダム生成
        return RandomItemGenerator.generate(rarityOverride, random);
    }
}
