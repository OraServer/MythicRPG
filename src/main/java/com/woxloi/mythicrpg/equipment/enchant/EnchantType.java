package com.woxloi.mythicrpg.equipment.enchant;

/**
 * 装備エンチャント（付与効果）の種別定義。
 * 各エンチャントはランク1〜5を持ち、ランクに応じて効果倍率が変動する。
 */
public enum EnchantType {

    /** 攻撃力強化 */
    POWER("力強化", "攻撃力を+{value}%増加"),

    /** 防御力強化 */
    FORTIFY("要塞", "防御力を+{value}%増加"),

    /** クリティカル率上昇 */
    PRECISION("精密", "クリティカル率を+{value}%増加"),

    /** 魔法攻撃力強化 */
    ARCANE("秘術", "魔法攻撃力を+{value}%増加"),

    /** 最大HP強化 */
    VITALITY("生命力", "最大HPを+{value}%増加"),

    /** 最大MP強化 */
    WISDOM("叡智", "最大MPを+{value}%増加"),

    /** 移動速度上昇 */
    SWIFTNESS("俊足", "移動速度を+{value}%増加"),

    /** 経験値ボーナス */
    EXPERIENCE("経験の恵み", "獲得経験値を+{value}%増加"),

    /** ドロップ率ボーナス */
    FORTUNE("幸運", "アイテムドロップ率を+{value}%増加"),

    /** クールダウン短縮 */
    ALACRITY("迅速", "スキルCTを{value}%短縮");

    /** エンチャント表示名 */
    private final String displayName;

    /** 効果説明テンプレート（{value} が実数値に置換される） */
    private final String descTemplate;

    EnchantType(String displayName, String descTemplate) {
        this.displayName  = displayName;
        this.descTemplate = descTemplate;
    }

    public String getDisplayName() { return displayName; }

    /**
     * ランクに応じた効果量を返す（1〜5段階）。
     */
    public double getValueForRank(int rank) {
        // ランク1=5%, 2=10%, ..., 5=25%
        return rank * 5.0;
    }

    /**
     * ランク付きの説明文を生成。
     */
    public String getDescription(int rank) {
        double value = getValueForRank(rank);
        String valueStr = (value == (int) value) ? String.valueOf((int) value) : String.valueOf(value);
        return descTemplate.replace("{value}", valueStr);
    }

    /**
     * GUIでの色付き表示名（ランク込み）。
     */
    public String getColoredName(int rank) {
        String rankStr = switch (rank) {
            case 1 -> "§7I";
            case 2 -> "§aII";
            case 3 -> "§bIII";
            case 4 -> "§dIV";
            case 5 -> "§6V";
            default -> "§fI";
        };
        return "§e" + displayName + " " + rankStr;
    }
}
