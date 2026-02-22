package com.woxloi.mythicrpg.stats;

/**
 * 振り分け可能なステータスタイプ
 */
public enum StatType {
    STR("§c筋力(STR)",  "攻撃力 +5/pt"),
    VIT("§a体力(VIT)",  "最大HP +20/pt"),
    INT("§9知力(INT)",  "最大MP +15/pt"),
    AGI("§e俊敏(AGI)",  "最大SP +10/pt");

    private final String displayName;
    private final String description;

    StatType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
