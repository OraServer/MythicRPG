package com.woxloi.mythicrpg.equipment.model;

/** 装備のレアリティ。色コードとステータス倍率を持つ。 */
public enum EquipRarity {
    COMMON    ("§7普通",    "§7", 1.0),
    UNCOMMON  ("§a上質",    "§a", 1.2),
    RARE      ("§9希少",    "§9", 1.5),
    EPIC      ("§5叙事詩",  "§5", 2.0),
    LEGENDARY ("§6§l伝説",  "§6", 3.0),
    MYTHIC    ("§d§l神話",  "§d", 5.0);

    public final String displayName;
    public final String color;
    public final double statMultiplier;

    EquipRarity(String displayName, String color, double statMultiplier) {
        this.displayName    = displayName;
        this.color          = color;
        this.statMultiplier = statMultiplier;
    }
}
