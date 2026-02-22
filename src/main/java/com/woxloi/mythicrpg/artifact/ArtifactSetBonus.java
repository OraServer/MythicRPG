package com.woxloi.mythicrpg.artifact;

import com.woxloi.mythicrpg.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumMap;
import java.util.Map;

/**
 * アーティファクトセットボーナスの適用・剥奪を担当。
 *
 * ArtifactManagerから装備枚数を受け取り、
 * 各段階のボーナスをPlayerDataへ反映する。
 */
public class ArtifactSetBonus {

    /**
     * プレイヤーの全セットボーナスを再計算して適用する。
     *
     * @param player        対象プレイヤー
     * @param data          PlayerData
     * @param equippedCounts セットごとの装備枚数 (ArtifactType → 枚数)
     */
    public static void applyAll(Player player, PlayerData data,
                                Map<ArtifactType, Integer> equippedCounts) {
        // まずボーナスをリセット
        resetBonuses(player, data);

        for (Map.Entry<ArtifactType, Integer> e : equippedCounts.entrySet()) {
            ArtifactType type  = e.getKey();
            int          count = e.getValue();
            int          tier  = type.getActiveTier(count);

            if (tier <= 0) continue;
            applySetBonus(player, data, type, tier);
        }

        // HP/MP上限を更新
        data.applyMaxHp();
        data.applyMaxMp();
    }

    /** セットボーナスをPlayerDataに加算する */
    private static void applySetBonus(Player player, PlayerData data,
                                      ArtifactType type, int tier) {
        switch (type) {

            case DRAGON_SLAYER -> {
                if (tier >= 1) { data.addBonusAtk(15 * (int)(data.getAttack()) / 100); }
                if (tier >= 2) { /* 炎ダメージ・竜属性は専用フラグで管理 */ }
                if (tier >= 3) { data.addBonusAtk(30 * (int)(data.getAttack()) / 100); }
            }

            case SHADOW_ASSASSIN -> {
                if (tier >= 1) { data.addBonusDef(-3); /* クリ率は内部フラグ */ }
                if (tier >= 2) { data.addBonusAtk(20 * (int)(data.getAttack()) / 100); }
                if (tier >= 3) { /* 影潜りスキル解放は SkillManager 側で処理 */ }
            }

            case ARCANE_SCHOLAR -> {
                if (tier >= 1) { data.addBonusMp(100); }
                if (tier >= 2) { data.addBonusMp(150); data.addBonusAtk(10); }
                if (tier >= 3) { data.addBonusMp(100); }
            }

            case IRON_FORTRESS -> {
                if (tier >= 1) { data.addBonusMaxHp(200); }
                if (tier >= 2) { data.addBonusMaxHp(300); data.addBonusDef(20); }
                if (tier >= 3) { data.addBonusMaxHp(200); data.addBonusDef(30); }
            }

            case EARTH_GUARDIAN -> {
                if (tier >= 1) { data.addBonusMaxHp(300); }
                if (tier >= 2) {
                    data.addBonusMaxHp(200);
                    // 5HP/秒再生: PoisionEffect REGENERATION
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false, false));
                }
            }

            case HERO_OF_LIGHT -> {
                if (tier >= 1) {
                    data.addBonusAtk(10 * (int)(data.getAttack()) / 100);
                    data.addBonusMaxHp(100);
                }
                if (tier >= 2) {
                    data.addBonusAtk(5 * (int)(data.getAttack()) / 100);
                    data.addBonusMaxHp(100);
                }
                if (tier >= 3) {
                    data.addBonusAtk(5 * (int)(data.getAttack()) / 100);
                    data.addBonusMaxHp(100);
                    data.addBonusMp(50);
                }
            }

            case FROST_WITCH -> {
                if (tier >= 1) { data.addBonusAtk(15 * (int)(data.getAttack()) / 100); }
                if (tier >= 2) { /* 凍結付与フラグはArtifactManager側で判定 */ }
            }

            case STORM_ARCHER -> {
                if (tier >= 1) { data.addBonusAtk(20 * (int)(data.getAttack()) / 100); }
                if (tier >= 2) { /* 矢貫通フラグ */ }
            }

            case WANDERER -> {
                if (tier >= 1) {
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false, false));
                }
                if (tier >= 2) { /* クエストEXP補正は QuestRewardHandler 側 */ }
            }

            case ANCIENT_KING -> {
                if (tier >= 1) {
                    data.addBonusAtk(10 * (int)(data.getAttack()) / 100);
                    data.addBonusMaxHp(100);
                    data.addBonusMp(50);
                }
                if (tier >= 2) {
                    data.addBonusAtk(10 * (int)(data.getAttack()) / 100);
                    data.addBonusMaxHp(100);
                    data.addBonusMp(50);
                }
                if (tier >= 3) {
                    data.addBonusAtk(10 * (int)(data.getAttack()) / 100);
                    data.addBonusMaxHp(200);
                    data.addBonusMp(100);
                }
            }
        }
    }

    /** ボーナス系ステータスを0にリセット（既存ボーナスを消す） */
    private static void resetBonuses(Player player, PlayerData data) {
        data.clearArtifactBonuses();

        // 付与したPotionEffectも消す
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.SPEED);
    }
}
