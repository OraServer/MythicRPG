package com.woxloi.mythicrpg.combo;

/** コンボの種類。発動条件と効果を定義する。 */
public enum ComboType {
    BASIC   ("通常コンボ",     "§f"),
    CRITICAL("クリティカル",   "§e"),
    ELEMENT ("属性コンボ",    "§b"),
    SKILL   ("スキルコンボ",  "§d"),
    FINISHER("フィニッシャー", "§c§l");

    public final String displayName;
    public final String color;

    ComboType(String displayName, String color) {
        this.displayName = displayName;
        this.color        = color;
    }
}
