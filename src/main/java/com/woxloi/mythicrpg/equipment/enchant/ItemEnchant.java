package com.woxloi.mythicrpg.equipment.enchant;

/**
 * RpgItemに付与されたエンチャント一件。
 * 種別とランク(1〜5)のペアで構成される。
 */
public class ItemEnchant {

    private final EnchantType type;
    private final int rank;

    public ItemEnchant(EnchantType type, int rank) {
        if (rank < 1 || rank > 5) throw new IllegalArgumentException("Rank must be 1-5, got: " + rank);
        this.type = type;
        this.rank = rank;
    }

    public EnchantType getType() { return type; }

    public int getRank() { return rank; }

    public double getValue() { return type.getValueForRank(rank); }

    public String getDescription() { return type.getDescription(rank); }

    public String getColoredName() { return type.getColoredName(rank); }

    /**
     * シリアライズ用文字列 (例: "POWER:3")
     */
    public String serialize() {
        return type.name() + ":" + rank;
    }

    /**
     * デシリアライズ
     */
    public static ItemEnchant deserialize(String s) {
        String[] parts = s.split(":");
        if (parts.length != 2) throw new IllegalArgumentException("Invalid enchant format: " + s);
        EnchantType type = EnchantType.valueOf(parts[0]);
        int rank = Integer.parseInt(parts[1]);
        return new ItemEnchant(type, rank);
    }

    @Override
    public String toString() {
        return "ItemEnchant{" + type.name() + ", rank=" + rank + "}";
    }
}
