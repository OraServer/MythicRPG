package com.woxloi.mythicrpg.equipment.set;

import com.woxloi.mythicrpg.equipment.model.EquipStats;

import java.util.ArrayList;
import java.util.List;

/**
 * 装備セット効果の定義。
 * 同一セットをN個装備すると発動するボーナスを管理する。
 */
public class SetEffect {

    /** セットID（例: "dragon_set"） */
    public final String setId;

    /** セット名（表示用） */
    public final String displayName;

    /** セット効果のティア（装備枚数→ボーナスのマッピング） */
    private final List<SetTier> tiers = new ArrayList<>();

    public SetEffect(String setId, String displayName) {
        this.setId       = setId;
        this.displayName = displayName;
    }

    public void addTier(SetTier tier) {
        tiers.add(tier);
    }

    /**
     * 現在の装備枚数に応じたステータスボーナスを返す。
     */
    public EquipStats getBonus(int pieceCount) {
        EquipStats total = new EquipStats();
        for (SetTier tier : tiers) {
            if (pieceCount >= tier.requiredPieces) {
                total = total.add(tier.bonus);
            }
        }
        return total;
    }

    /**
     * Lore表示用テキストを返す。
     */
    public List<String> getLoreLines(int currentPieces) {
        List<String> lines = new ArrayList<>();
        lines.add("§6セット効果: " + displayName);
        for (SetTier tier : tiers) {
            String check = currentPieces >= tier.requiredPieces ? "§a✓" : "§7○";
            lines.add(check + " §f(" + tier.requiredPieces + "セット) §7" + tier.description);
        }
        return lines;
    }

    public List<SetTier> getTiers() { return tiers; }

    // ──────────────────────────────────────────────────────────
    //  SetTier
    // ──────────────────────────────────────────────────────────

    public static class SetTier {
        public final int requiredPieces;
        public final EquipStats bonus;
        public final String description;

        public SetTier(int requiredPieces, EquipStats bonus, String description) {
            this.requiredPieces = requiredPieces;
            this.bonus          = bonus;
            this.description    = description;
        }
    }
}
