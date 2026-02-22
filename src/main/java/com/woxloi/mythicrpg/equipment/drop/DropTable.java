package com.woxloi.mythicrpg.equipment.drop;

import com.woxloi.mythicrpg.equipment.model.EquipRarity;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.random.RandomItemGenerator;

import java.util.*;

/**
 * 装備ドロップテーブル。
 * Mobの種類や難易度に応じてドロップするアイテムを抽選する。
 */
public class DropTable {

    /** テーブルID */
    private final String id;

    /** ドロップエントリのリスト */
    private final List<DropEntry> entries = new ArrayList<>();

    /** ドロップが発生するかの基礎確率 (0.0〜1.0) */
    private double dropChance;

    public DropTable(String id, double dropChance) {
        this.id         = id;
        this.dropChance = dropChance;
    }

    public String getId() { return id; }
    public double getDropChance() { return dropChance; }

    /** エントリを追加する */
    public void addEntry(DropEntry entry) {
        entries.add(entry);
    }

    /**
     * ドロップ抽選を行い、ドロップするRpgItemのリストを返す。
     * ドロップなしの場合は空リストを返す。
     */
    public List<RpgItem> roll(Random random) {
        List<RpgItem> result = new ArrayList<>();
        if (random.nextDouble() > dropChance) return result;

        // 重み付き抽選
        double totalWeight = entries.stream().mapToDouble(e -> e.weight).sum();
        double rand = random.nextDouble() * totalWeight;
        double cumulative = 0;

        for (DropEntry entry : entries) {
            cumulative += entry.weight;
            if (rand <= cumulative) {
                RpgItem item = entry.generateItem(random);
                if (item != null) result.add(item);
                break;
            }
        }
        return result;
    }
}
