package com.woxloi.mythicrpg.artifact;

import com.woxloi.mythicrpg.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

/**
 * アーティファクトセットボーナスの適用・剥奪を担当。
 *
 * artifacts.yml の bonus-stats セクションを読んで適用する。
 * ArtifactRegistryのsetDefRegistryが正規のソースとなる。
 *
 * 対応ステータスキー:
 *   atk          - 固定ATK加算
 *   atk-percent  - ATK%加算 (例: 15 → attack×15%)
 *   def          - 固定DEF加算
 *   max-hp       - MaxHP加算
 *   max-mp       - MaxMP加算
 *   special      - 特殊効果ID（下記参照）
 *
 * 特殊効果ID:
 *   speed_boost      - 移動速度+1段階
 *   hp_regen         - HP自然回復付与
 *   no_knockback     - ノックバック耐性
 *   dragon_fire      - 竜属性フラグ
 *   freeze_on_hit    - 凍結フラグ
 *   revival_chance   - 復活フラグ
 *   exp_boost        - EXP取得量+20%フラグ
 *   quest_exp_boost  - クエストEXP+30%フラグ
 *   mana_amp         - スキルコスト軽減フラグ
 */
public class ArtifactSetBonus {

    /**
     * プレイヤーの全セットボーナスを再計算して適用する。
     */
    public static void applyAll(Player player, PlayerData data,
                                Map<ArtifactType, Integer> equippedCounts) {
        resetBonuses(player, data);

        for (Map.Entry<ArtifactType, Integer> e : equippedCounts.entrySet()) {
            ArtifactType type  = e.getKey();
            int          count = e.getValue();
            int          tier  = type.getActiveTier(count);
            if (tier <= 0) continue;

            // YAMLのsetDefを優先、なければフォールバックなし
            ArtifactRegistry.ArtifactSetDef def =
                    ArtifactRegistry.getSetDef(type.name());

            if (def != null) {
                // tier 1 〜 tier段階まで順番に適用（累積）
                for (int t = 1; t <= tier; t++) {
                    Map<String, Object> bonusMap = def.tierBonuses().get(t);
                    if (bonusMap == null) continue;
                    applyBonusMap(player, data, bonusMap);
                }
            } else {
                // YAMLにsetDefがない場合のフォールバック（旧enum処理）
                applyLegacyBonus(player, data, type, tier);
            }
        }

        data.applyMaxHp();
        data.applyMaxMp();
    }

    /** bonus-statsマップを読んでPlayerDataに適用 */
    private static void applyBonusMap(Player player, PlayerData data, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();

            switch (key) {
                case "atk" -> {
                    int v = toInt(val);
                    data.addBonusAtk(v);
                }
                case "atk-percent" -> {
                    int pct = toInt(val);
                    data.addBonusAtk((int)(data.getAttack() * pct / 100.0));
                }
                case "def" -> data.addBonusDef(toInt(val));
                case "max-hp" -> data.addBonusMaxHp(toInt(val));
                case "max-mp" -> data.addBonusMp(toInt(val));
                case "crit-rate"   -> { /* 将来対応 - EquipStatsに統合予定 */ }
                case "crit-damage" -> { /* 将来対応 */ }
                case "special" -> applySpecial(player, data, String.valueOf(val));
            }
        }
    }

    /** 特殊効果IDの適用 */
    private static void applySpecial(Player player, PlayerData data, String effectId) {
        switch (effectId) {
            case "speed_boost" ->
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false, false));
            case "hp_regen" ->
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false, false));
            // フラグ系はArtifactManagerの判定メソッドがPlayerDataのキャッシュを参照する
            // ここでは特に何もしない（ArtifactManager.shouldFreeze等で参照される）
            case "dragon_fire", "freeze_on_hit", "revival_chance",
                 "exp_boost", "quest_exp_boost", "mana_amp",
                 "no_knockback", "arrow_pierce", "earth_immunity",
                 "dragon_roar_skill", "shadow_vanish_skill", "iron_wall_skill",
                 "arcane_release_skill", "holy_burst_skill", "kings_aura_skill",
                 "shadow_step" -> { /* フラグはArtifactManagerのキャッシュで管理 */ }
            default -> { /* 不明な特殊効果は無視 */ }
        }
    }

    /** 旧フォールバック（YAMLにsetDefが存在しない場合） */
    private static void applyLegacyBonus(Player player, PlayerData data,
                                          ArtifactType type, int tier) {
        switch (type) {
            case ARCANE_SCHOLAR -> {
                if (tier >= 1) data.addBonusMp(100);
                if (tier >= 2) { data.addBonusMp(150); data.addBonusAtk(10); }
                if (tier >= 3)   data.addBonusMp(100);
            }
            case IRON_FORTRESS -> {
                if (tier >= 1) data.addBonusMaxHp(200);
                if (tier >= 2) { data.addBonusMaxHp(300); data.addBonusDef(20); }
                if (tier >= 3) { data.addBonusMaxHp(200); data.addBonusDef(30); }
            }
            case EARTH_GUARDIAN -> {
                if (tier >= 1) data.addBonusMaxHp(300);
                if (tier >= 2) {
                    data.addBonusMaxHp(200);
                    player.addPotionEffect(new PotionEffect(
                        PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false, false));
                }
            }
            case WANDERER -> {
                if (tier >= 1) player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false, false));
            }
            // 他はATK%系なので共通処理
            default -> {
                int atkPct = switch(type) {
                    case DRAGON_SLAYER   -> tier >= 1 ? 15 : 0;
                    case SHADOW_ASSASSIN -> tier >= 1 ? 10 : 0;
                    case HERO_OF_LIGHT   -> tier >= 1 ? 10 : 0;
                    case FROST_WITCH     -> tier >= 1 ? 15 : 0;
                    case STORM_ARCHER    -> tier >= 1 ? 20 : 0;
                    case ANCIENT_KING    -> tier >= 1 ? 10 : 0;
                    default -> 0;
                };
                if (atkPct > 0) data.addBonusAtk((int)(data.getAttack() * atkPct / 100.0));
            }
        }
    }

    /** ボーナス系ステータスを0にリセット */
    private static void resetBonuses(Player player, PlayerData data) {
        data.clearArtifactBonuses();
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    private static int toInt(Object val) {
        if (val instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(val)); }
        catch (NumberFormatException e) { return 0; }
    }
}
