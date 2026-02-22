package com.woxloi.mythicrpg.artifact;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * アーティファクトシステムの中核。
 *
 * 役割:
 *  - アイテムへの artifact_id タグ付け
 *  - プレイヤーの装備を走査してセット枚数カウント
 *  - ArtifactSetBonusへの再計算依頼
 *  - 凍結・竜属性などの特殊判定用フラグ管理
 *
 * "artifact_id" はPersistentDataContainerに文字列で保存する。
 * 例: "dragon_slayer_sword"
 */
public class ArtifactManager {

    /** NBTキー: アーティファクトID */
    private static NamespacedKey KEY_ARTIFACT_ID;

    /**
     * プラグイン起動時に必ず呼ぶ。
     */
    public static void init() {
        KEY_ARTIFACT_ID = new NamespacedKey(MythicRPG.getInstance(), "artifact_id");
    }

    /** NamespacedKey取得 */
    public static NamespacedKey getKey() { return KEY_ARTIFACT_ID; }

    // ─────────────────────────────────────────────
    //  アイテムへのタグ付け
    // ─────────────────────────────────────────────

    /**
     * ItemStackにアーティファクトIDを書き込む。
     * EquipmentRegistryでアイテム生成後に呼ぶ。
     */
    public static ItemStack tagItem(ItemStack item, String artifactId) {
        if (item == null || !ArtifactRegistry.exists(artifactId)) return item;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.getPersistentDataContainer().set(KEY_ARTIFACT_ID, PersistentDataType.STRING, artifactId);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * ItemStackからアーティファクトIDを取得。
     * @return nullならアーティファクトではない
     */
    public static String getArtifactId(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(KEY_ARTIFACT_ID, PersistentDataType.STRING);
    }

    /**
     * ItemStackがアーティファクトかどうか。
     */
    public static boolean isArtifact(ItemStack item) {
        return getArtifactId(item) != null;
    }

    // ─────────────────────────────────────────────
    //  セット効果の再計算
    // ─────────────────────────────────────────────

    /**
     * プレイヤーの装備を全走査してセットボーナスを再適用する。
     * 装備変更イベント・ログイン時に呼ぶ。
     */
    public static void recalculate(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        Map<ArtifactType, Integer> counts = countEquipped(player);
        ArtifactSetBonus.applyAll(player, data, counts);

        // セット情報をPlayerDataにキャッシュ
        data.setArtifactSetCounts(counts);
    }

    /**
     * プレイヤーが現在装備しているアーティファクトのセット枚数を数える。
     */
    public static Map<ArtifactType, Integer> countEquipped(Player player) {
        Map<ArtifactType, Integer> counts = new EnumMap<>(ArtifactType.class);

        // アーマースロット
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            addIfArtifact(armor, counts);
        }
        // メインハンド
        addIfArtifact(player.getInventory().getItemInMainHand(), counts);
        // オフハンド
        addIfArtifact(player.getInventory().getItemInOffHand(), counts);

        return counts;
    }

    private static void addIfArtifact(ItemStack item, Map<ArtifactType, Integer> counts) {
        if (item == null) return;
        String id = getArtifactId(item);
        if (id == null) return;
        ArtifactPiece piece = ArtifactRegistry.get(id);
        if (piece == null) return;
        counts.merge(piece.getSetType(), 1, Integer::sum);
    }

    // ─────────────────────────────────────────────
    //  特殊判定ヘルパー (他クラスから参照)
    // ─────────────────────────────────────────────

    /** 凍結付与判定: FROST_WITCH 2セット発動時15%確率 */
    public static boolean shouldFreeze(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return false;
        Map<ArtifactType, Integer> counts = data.getArtifactSetCounts();
        if (counts == null) return false;
        int c = counts.getOrDefault(ArtifactType.FROST_WITCH, 0);
        return ArtifactType.FROST_WITCH.getActiveTier(c) >= 2
                && Math.random() < 0.15;
    }

    /** 竜属性ダメージ倍率: DRAGON_SLAYER 2セット以上で1.5倍 */
    public static double dragonDamageMultiplier(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return 1.0;
        Map<ArtifactType, Integer> counts = data.getArtifactSetCounts();
        if (counts == null) return 1.0;
        int c = counts.getOrDefault(ArtifactType.DRAGON_SLAYER, 0);
        int tier = ArtifactType.DRAGON_SLAYER.getActiveTier(c);
        if (tier >= 2) return 1.5;
        if (tier >= 1) return 1.15;
        return 1.0;
    }

    /** クエストEXP補正: WANDERER 2セットで+30% */
    public static double questExpMultiplier(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return 1.0;
        Map<ArtifactType, Integer> counts = data.getArtifactSetCounts();
        if (counts == null) return 1.0;
        int c = counts.getOrDefault(ArtifactType.WANDERER, 0);
        return ArtifactType.WANDERER.getActiveTier(c) >= 2 ? 1.30 : 1.0;
    }

    /** ANCIENT_KING 復活判定 (死亡時20%で発動) */
    public static boolean shouldRevive(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return false;
        Map<ArtifactType, Integer> counts = data.getArtifactSetCounts();
        if (counts == null) return false;
        int c = counts.getOrDefault(ArtifactType.ANCIENT_KING, 0);
        return ArtifactType.ANCIENT_KING.getActiveTier(c) >= 2
                && Math.random() < 0.20;
    }

    /** コンボ倍率ボーナス: ANCIENT_KING 2セットで+0.5 */
    public static double comboMultiplierBonus(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return 0;
        Map<ArtifactType, Integer> counts = data.getArtifactSetCounts();
        if (counts == null) return 0;
        int c = counts.getOrDefault(ArtifactType.ANCIENT_KING, 0);
        return ArtifactType.ANCIENT_KING.getActiveTier(c) >= 2 ? 0.5 : 0.0;
    }
}
