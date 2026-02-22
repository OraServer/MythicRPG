package com.woxloi.mythicrpg.element;

/**
 * 属性システムの属性種別。
 * 各属性は相性テーブルを持ち、攻撃時に倍率補正がかかる。
 *
 * 相性表（行=攻撃属性 / 列=防御属性）:
 *           FIRE  WATER WIND  EARTH LIGHT DARK  NONE
 * FIRE       1.0   0.5   1.5   1.0   1.0   1.0   1.0
 * WATER      1.5   1.0   0.5   1.0   1.0   1.0   1.0
 * WIND       1.0   1.5   1.0   0.5   1.0   1.0   1.0
 * EARTH      1.0   1.0   1.5   1.0   1.0   1.0   1.0
 * LIGHT      1.0   1.0   1.0   1.0   1.0   2.0   1.0
 * DARK       1.0   1.0   1.0   1.0   2.0   1.0   1.0
 * NONE       1.0   1.0   1.0   1.0   1.0   1.0   1.0
 */
public enum ElementType {

    FIRE ("§c炎",   "§c🔥", "§c"),
    WATER("§b水",   "§b💧", "§b"),
    WIND ("§a風",   "§a🍃", "§a"),
    EARTH("§6土",   "§6⛰", "§6"),
    LIGHT("§e光",   "§e✦",  "§e"),
    DARK ("§5闇",   "§5☾",  "§5"),
    NONE ("§7無",   "§7◆",  "§7");

    private final String displayName;
    private final String icon;
    private final String color;

    /** 相性倍率テーブル [attackerIndex][defenderIndex] */
    private static final double[][] AFFINITY = {
        // vs: FIRE  WATER  WIND  EARTH LIGHT  DARK  NONE
        {        1.0,  0.5,  1.5,  1.0,  1.0,  1.0,  1.0 }, // FIRE
        {        1.5,  1.0,  0.5,  1.0,  1.0,  1.0,  1.0 }, // WATER
        {        1.0,  1.5,  1.0,  0.5,  1.0,  1.0,  1.0 }, // WIND
        {        1.0,  1.0,  1.5,  1.0,  1.0,  1.0,  1.0 }, // EARTH
        {        1.0,  1.0,  1.0,  1.0,  1.0,  2.0,  1.0 }, // LIGHT
        {        1.0,  1.0,  1.0,  1.0,  2.0,  1.0,  1.0 }, // DARK
        {        1.0,  1.0,  1.0,  1.0,  1.0,  1.0,  1.0 }, // NONE
    };

    ElementType(String displayName, String icon, String color) {
        this.displayName = displayName;
        this.icon        = icon;
        this.color       = color;
    }

    /** この属性で攻撃したときの相性倍率を返す */
    public double getAffinityMultiplier(ElementType defender) {
        return AFFINITY[this.ordinal()][defender.ordinal()];
    }

    /** 2.0倍＝弱点、0.5倍＝耐性の表示テキスト */
    public String getAffinityText(ElementType defender) {
        double mult = getAffinityMultiplier(defender);
        if (mult >= 2.0) return "§c弱点";
        if (mult <= 0.5) return "§b耐性";
        return "§7等倍";
    }

    public String getDisplayName() { return displayName; }
    public String getIcon()        { return icon; }
    public String getColor()       { return color; }

    /** 略称（GUI lore用） */
    public String getTagged() { return color + "[" + displayName + "§r" + color + "]"; }
}
