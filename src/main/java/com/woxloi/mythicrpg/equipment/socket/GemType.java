package com.woxloi.mythicrpg.equipment.socket;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * 宝石（ソケット素材）の種類。
 * 各宝石を装備のソケットに装着することでステータスを強化できる。
 */
public enum GemType {

    RUBY       ("ルビー",         ChatColor.RED,          Material.REDSTONE,           0, 5, 0, 0, 0),
    SAPPHIRE   ("サファイア",     ChatColor.BLUE,         Material.LAPIS_LAZULI,       0, 0, 5, 0, 0),
    EMERALD    ("エメラルド",     ChatColor.GREEN,        Material.EMERALD,            0, 0, 0, 5, 0),
    DIAMOND    ("ダイヤ",         ChatColor.AQUA,         Material.DIAMOND,            10,0, 0, 0, 0),
    TOPAZ      ("トパーズ",       ChatColor.YELLOW,       Material.GOLD_INGOT,         0, 3, 3, 0, 0),
    AMETHYST   ("アメジスト",     ChatColor.LIGHT_PURPLE, Material.AMETHYST_SHARD,     0, 0, 0, 0, 5),
    ONYX       ("オニキス",       ChatColor.DARK_GRAY,    Material.COAL,               0, 8, 0, 0, 0),
    OPAL       ("オパール",       ChatColor.WHITE,        Material.QUARTZ,             5, 0, 5, 0, 0);

    public final String displayName;
    public final ChatColor color;
    public final Material material;

    /** 付与するステータスボーナス */
    public final int bonusMaxHp;
    public final int bonusAtk;
    public final int bonusMp;
    public final int bonusDef;
    public final int bonusSp;

    GemType(String displayName, ChatColor color, Material material,
            int bonusMaxHp, int bonusAtk, int bonusMp, int bonusDef, int bonusSp) {
        this.displayName  = displayName;
        this.color        = color;
        this.material     = material;
        this.bonusMaxHp   = bonusMaxHp;
        this.bonusAtk     = bonusAtk;
        this.bonusMp      = bonusMp;
        this.bonusDef     = bonusDef;
        this.bonusSp      = bonusSp;
    }

    public String getColoredName() {
        return color + displayName;
    }
}
