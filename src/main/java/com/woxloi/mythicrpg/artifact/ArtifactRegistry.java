package com.woxloi.mythicrpg.artifact;

import com.woxloi.mythicrpg.equipment.model.EquipSlot;

import java.util.*;

/**
 * 全アーティファクトピースの定義レジストリ。
 *
 * キー: pieceId (例: "dragon_slayer_sword")
 * 値  : ArtifactPiece
 *
 * アイテム生成時・識別時にここを参照する。
 */
public class ArtifactRegistry {

    private static final Map<String, ArtifactPiece> registry = new LinkedHashMap<>();

    static {
        // ── DRAGON_SLAYER セット (6ピース) ──
        reg(ArtifactType.DRAGON_SLAYER, EquipSlot.MAIN_HAND,
                "dragon_slayer_sword",  "§4竜殺しの大剣",        80,  5,   0,   0);
        reg(ArtifactType.DRAGON_SLAYER, EquipSlot.OFF_HAND,
                "dragon_slayer_shield", "§4竜鱗の盾",             10, 30,  50,   0);
        reg(ArtifactType.DRAGON_SLAYER, EquipSlot.HELMET,
                "dragon_slayer_helm",   "§4竜頭の兜",              5, 15, 100,   0);
        reg(ArtifactType.DRAGON_SLAYER, EquipSlot.CHESTPLATE,
                "dragon_slayer_chest",  "§4竜鱗の鎧",             10, 25, 150,   0);
        reg(ArtifactType.DRAGON_SLAYER, EquipSlot.LEGGINGS,
                "dragon_slayer_legs",   "§4竜鱗の脚甲",            5, 20,  80,   0);
        reg(ArtifactType.DRAGON_SLAYER, EquipSlot.BOOTS,
                "dragon_slayer_boots",  "§4竜鱗の靴",              5, 10,  50,   0);

        // ── SHADOW_ASSASSIN セット (6ピース) ──
        reg(ArtifactType.SHADOW_ASSASSIN, EquipSlot.MAIN_HAND,
                "shadow_dagger",        "§8影の短剣",             70,  0,   0,   0);
        reg(ArtifactType.SHADOW_ASSASSIN, EquipSlot.OFF_HAND,
                "shadow_blade",         "§8影の刃",               60,  0,   0,   0);
        reg(ArtifactType.SHADOW_ASSASSIN, EquipSlot.HELMET,
                "shadow_hood",          "§8影のフード",             0,  8,  50,  20);
        reg(ArtifactType.SHADOW_ASSASSIN, EquipSlot.CHESTPLATE,
                "shadow_coat",          "§8影のコート",             5, 12,  80,  30);
        reg(ArtifactType.SHADOW_ASSASSIN, EquipSlot.LEGGINGS,
                "shadow_pants",         "§8影のズボン",             3,  8,  50,  20);
        reg(ArtifactType.SHADOW_ASSASSIN, EquipSlot.BOOTS,
                "shadow_boots",         "§8影のブーツ",             3,  5,  30,  10);

        // ── ARCANE_SCHOLAR セット (6ピース) ──
        reg(ArtifactType.ARCANE_SCHOLAR, EquipSlot.MAIN_HAND,
                "arcane_staff",         "§d秘術のスタッフ",        50,  0,   0, 100);
        reg(ArtifactType.ARCANE_SCHOLAR, EquipSlot.OFF_HAND,
                "arcane_tome",          "§d秘術の魔導書",           20,  0,   0, 150);
        reg(ArtifactType.ARCANE_SCHOLAR, EquipSlot.HELMET,
                "arcane_hat",           "§d秘術の帽子",              0,  5,  30, 120);
        reg(ArtifactType.ARCANE_SCHOLAR, EquipSlot.CHESTPLATE,
                "arcane_robe",          "§d秘術のローブ",            5,  8,  50, 200);
        reg(ArtifactType.ARCANE_SCHOLAR, EquipSlot.LEGGINGS,
                "arcane_skirt",         "§d秘術のスカート",           0,  5,  30, 100);
        reg(ArtifactType.ARCANE_SCHOLAR, EquipSlot.BOOTS,
                "arcane_shoes",         "§d秘術の靴",                0,  3,  20,  80);

        // ── IRON_FORTRESS セット (6ピース) ──
        reg(ArtifactType.IRON_FORTRESS, EquipSlot.MAIN_HAND,
                "fortress_sword",       "§7鉄壁の剣",             40, 10,  50,   0);
        reg(ArtifactType.IRON_FORTRESS, EquipSlot.OFF_HAND,
                "fortress_shield",      "§7鉄壁の盾",              5, 50, 100,   0);
        reg(ArtifactType.IRON_FORTRESS, EquipSlot.HELMET,
                "fortress_helm",        "§7鉄壁の兜",              0, 30, 150,   0);
        reg(ArtifactType.IRON_FORTRESS, EquipSlot.CHESTPLATE,
                "fortress_plate",       "§7鉄壁の胸当て",            0, 50, 250,   0);
        reg(ArtifactType.IRON_FORTRESS, EquipSlot.LEGGINGS,
                "fortress_legs",        "§7鉄壁の脚甲",             0, 35, 180,   0);
        reg(ArtifactType.IRON_FORTRESS, EquipSlot.BOOTS,
                "fortress_boots",       "§7鉄壁の靴",               0, 25, 120,   0);

        // ── HERO_OF_LIGHT セット (6ピース) ──
        reg(ArtifactType.HERO_OF_LIGHT, EquipSlot.MAIN_HAND,
                "hero_sword",           "§e光の勇者の剣",          60, 10,  50,  30);
        reg(ArtifactType.HERO_OF_LIGHT, EquipSlot.OFF_HAND,
                "hero_shield",          "§e光の勇者の盾",           10, 30,  80,  20);
        reg(ArtifactType.HERO_OF_LIGHT, EquipSlot.HELMET,
                "hero_helm",            "§e光の勇者の兜",            5, 15, 120,  20);
        reg(ArtifactType.HERO_OF_LIGHT, EquipSlot.CHESTPLATE,
                "hero_plate",           "§e光の勇者の鎧",           10, 25, 200,  40);
        reg(ArtifactType.HERO_OF_LIGHT, EquipSlot.LEGGINGS,
                "hero_legs",            "§e光の勇者の脚甲",          5, 18, 120,  25);
        reg(ArtifactType.HERO_OF_LIGHT, EquipSlot.BOOTS,
                "hero_boots",           "§e光の勇者の靴",            5, 12,  80,  15);

        // ── ANCIENT_KING セット (6ピース) ──
        reg(ArtifactType.ANCIENT_KING, EquipSlot.MAIN_HAND,
                "king_sword",           "§5古代王の覇剣",          90, 15, 100,  50);
        reg(ArtifactType.ANCIENT_KING, EquipSlot.OFF_HAND,
                "king_orb",             "§5古代王のオーブ",         30, 20,  80, 100);
        reg(ArtifactType.ANCIENT_KING, EquipSlot.HELMET,
                "king_crown",           "§5古代王の王冠",           10, 20, 150,  80);
        reg(ArtifactType.ANCIENT_KING, EquipSlot.CHESTPLATE,
                "king_armor",           "§5古代王の鎧",             20, 35, 300, 100);
        reg(ArtifactType.ANCIENT_KING, EquipSlot.LEGGINGS,
                "king_legs",            "§5古代王の脚甲",           10, 25, 200,  60);
        reg(ArtifactType.ANCIENT_KING, EquipSlot.BOOTS,
                "king_boots",           "§5古代王の靴",             10, 18, 120,  40);
    }

    private static void reg(ArtifactType type, EquipSlot slot,
                            String id, String name,
                            int atk, int def, int hp, int mp) {
        registry.put(id, new ArtifactPiece(type, slot, id, name, atk, def, hp, mp));
    }

    /** IDからピースを取得 */
    public static ArtifactPiece get(String id) {
        return registry.get(id);
    }

    /** 全ピース取得 */
    public static Collection<ArtifactPiece> all() {
        return registry.values();
    }

    /** 指定セットに属するピースを取得 */
    public static List<ArtifactPiece> ofSet(ArtifactType type) {
        List<ArtifactPiece> list = new ArrayList<>();
        for (ArtifactPiece p : registry.values()) {
            if (p.getSetType() == type) list.add(p);
        }
        return list;
    }

    /** IDが登録済みか */
    public static boolean exists(String id) {
        return registry.containsKey(id);
    }
}
