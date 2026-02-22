package com.woxloi.mythicrpg.equipment.model;

/**
 * RPG装備スロット定義。
 * BukkitのEquipmentSlotと区別するため EquipSlot という名前にしている。
 *
 * MAIN_HAND / OFF_HAND はアーティファクトシステム用エイリアス。
 */
public enum EquipSlot {
    WEAPON    ("武器",    "⚔"),
    MAIN_HAND ("武器",    "⚔"),   // WEAPONのエイリアス
    OFFHAND   ("副武器",  "🛡"),
    OFF_HAND  ("副武器",  "🛡"),   // OFFHANDのエイリアス
    HELMET    ("兜",      "⛑"),
    CHESTPLATE("胸当て",  "🎽"),
    LEGGINGS  ("脚当て",  "👖"),
    BOOTS     ("靴",      "👟"),
    RING_L    ("左指輪",  "💍"),
    RING_R    ("右指輪",  "💍"),
    NECKLACE  ("首飾り",  "📿"),
    RELIC     ("遺物",    "🔮");

    public final String displayName;
    public final String icon;

    EquipSlot(String displayName, String icon) {
        this.displayName = displayName;
        this.icon        = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon()        { return icon; }
}
