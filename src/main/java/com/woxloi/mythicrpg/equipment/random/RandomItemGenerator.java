package com.woxloi.mythicrpg.equipment.random;

import com.woxloi.mythicrpg.equipment.model.*;
import com.woxloi.mythicrpg.equipment.socket.SocketManager;
import com.woxloi.mythicrpg.job.JobType;
import org.bukkit.Material;

import java.util.Random;

/**
 * ランダム装備生成エンジン。
 * レアリティ・スロット・ステータスをランダムに決定してRpgItemを生成する。
 */
public class RandomItemGenerator {

    private static final String[] PREFIXES = {
        "古き", "輝く", "呪われた", "神聖な", "暗黒の",
        "炎の", "氷結の", "雷光の", "影の", "黄金の"
    };

    private static final String[] SUFFIXES_WEAPON = { "剣", "刀", "刃", "槍", "弓", "杖" };
    private static final String[] SUFFIXES_ARMOR  = { "鎧", "盾", "外套", "帽子", "手甲" };

    /**
     * 指定レアリティのランダム装備を生成する。
     * rarityOverride が null の場合はランダム選択。
     */
    public static RpgItem generate(EquipRarity rarityOverride, Random random) {
        EquipRarity rarity = rarityOverride != null
                ? rarityOverride
                : rollRarity(random);

        EquipSlot slot = EquipSlot.values()[random.nextInt(EquipSlot.values().length)];
        String name = generateName(slot, random);
        String id   = "random_" + System.nanoTime();

        RpgItem item = new RpgItem(id, rarity.color + name, slot, rarity);
        item.baseStats = generateStats(rarity, slot, random);
        item.requiredLevel = generateRequiredLevel(rarity, random);
        item.maxEnhance    = 10;
        item.baseItem      = new org.bukkit.inventory.ItemStack(slotToMaterial(slot));

        // ソケット数（レアリティ依存）
        int socketCount = rarity.ordinal(); // COMMON=0, UNCOMMON=1, RARE=2, EPIC=3, LEGENDARY=4
        if (socketCount > 0) {
            item.socketSlots = SocketManager.createSlots(Math.min(socketCount, 3));
        }

        return item;
    }

    private static EquipRarity rollRarity(Random random) {
        int roll = random.nextInt(100);
        if (roll < 50) return EquipRarity.COMMON;
        if (roll < 75) return EquipRarity.UNCOMMON;
        if (roll < 90) return EquipRarity.RARE;
        if (roll < 98) return EquipRarity.EPIC;
        return EquipRarity.LEGENDARY;
    }

    private static String generateName(EquipSlot slot, Random random) {
        String prefix = PREFIXES[random.nextInt(PREFIXES.length)];
        boolean isWeapon = slot == EquipSlot.WEAPON || slot == EquipSlot.OFF_HAND;
        String[] suffixes = isWeapon ? SUFFIXES_WEAPON : SUFFIXES_ARMOR;
        String suffix = suffixes[random.nextInt(suffixes.length)];
        return prefix + suffix;
    }

    private static EquipStats generateStats(EquipRarity rarity, EquipSlot slot, Random random) {
        EquipStats stats = new EquipStats();
        double mult = 1.0 + rarity.ordinal() * 0.5;  // レアリティ倍率

        boolean isWeapon = slot == EquipSlot.WEAPON || slot == EquipSlot.OFF_HAND;
        if (isWeapon) {
            stats.attack    = (int)((5 + random.nextInt(15)) * mult);
            stats.maxHpBonus  = (int)((0 + random.nextInt(20)) * mult);
        } else {
            stats.defense    = (int)((3 + random.nextInt(10)) * mult);
            stats.maxHpBonus  = (int)((10 + random.nextInt(30)) * mult);
            stats.maxMpBonus  = (int)((5  + random.nextInt(15)) * mult);
        }
        return stats;
    }

    private static int generateRequiredLevel(EquipRarity rarity, Random random) {
        return switch (rarity) {
            case COMMON    -> 1  + random.nextInt(10);
            case UNCOMMON  -> 10 + random.nextInt(10);
            case RARE      -> 20 + random.nextInt(15);
            case EPIC      -> 35 + random.nextInt(15);
            case LEGENDARY -> 50 + random.nextInt(10);
            case MYTHIC    -> 60 + random.nextInt(10);
        };
    }

    private static Material slotToMaterial(EquipSlot slot) {
        return switch (slot) {
            case WEAPON, MAIN_HAND -> Material.IRON_SWORD;
            case OFF_HAND, OFFHAND -> Material.SHIELD;
            case HELMET            -> Material.IRON_HELMET;
            case CHESTPLATE        -> Material.IRON_CHESTPLATE;
            case LEGGINGS          -> Material.IRON_LEGGINGS;
            case BOOTS             -> Material.IRON_BOOTS;
            case RING_L, RING_R    -> Material.GOLD_INGOT;
            case NECKLACE          -> Material.STRING;
            case RELIC             -> Material.AMETHYST_SHARD;
            default                -> Material.PAPER;
        };
    }
}
