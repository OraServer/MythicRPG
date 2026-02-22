package com.woxloi.mythicrpg.element;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.woxloi.mythicrpg.MythicRPG;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * プレイヤー・アイテム・Mobの属性を管理する。
 *
 * - アイテム属性: PersistentDataContainer の "element" キーで保持
 * - プレイヤー耐性: メモリキャッシュ（装備変更時に再計算）
 * - Mob属性: PersistentDataContainer で管理
 */
public class ElementManager {

    /** アイテムの属性キー */
    public static final NamespacedKey KEY_ELEMENT =
            new NamespacedKey(MythicRPG.getInstance(), "element_type");

    /** Mobの属性キー */
    public static final NamespacedKey KEY_MOB_ELEMENT =
            new NamespacedKey(MythicRPG.getInstance(), "mob_element");

    /** プレイヤー属性耐性キャッシュ: UUID → 属性 → 耐性値 (0.0〜1.0) */
    private static final Map<UUID, EnumMap<ElementType, Double>> resistances =
            new ConcurrentHashMap<>();

    private ElementManager() {}

    // ─── アイテム属性 ──────────────────────────

    /** ItemStackに属性を付与する */
    public static void setItemElement(ItemStack item, ElementType type) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer()
                .set(KEY_ELEMENT, PersistentDataType.STRING, type.name());
        item.setItemMeta(meta);
    }

    /** ItemStackの属性を取得する（未設定はNONE） */
    public static ElementType getItemElement(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return ElementType.NONE;
        String val = item.getItemMeta().getPersistentDataContainer()
                .get(KEY_ELEMENT, PersistentDataType.STRING);
        if (val == null) return ElementType.NONE;
        try { return ElementType.valueOf(val); } catch (Exception e) { return ElementType.NONE; }
    }

    // ─── Mob属性 ──────────────────────────────

    /** Entityに属性を付与する */
    public static void setMobElement(Entity entity, ElementType type) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(KEY_MOB_ELEMENT, PersistentDataType.STRING, type.name());
    }

    /** Entityの属性を取得する（未設定はNONE） */
    public static ElementType getMobElement(Entity entity) {
        String val = entity.getPersistentDataContainer()
                .get(KEY_MOB_ELEMENT, PersistentDataType.STRING);
        if (val == null) return ElementType.NONE;
        try { return ElementType.valueOf(val); } catch (Exception e) { return ElementType.NONE; }
    }

    // ─── プレイヤー耐性 ──────────────────────

    /** プレイヤーの属性耐性を設定する（装備計算後に呼ぶ） */
    public static void setResistance(UUID uuid, ElementType type, double value) {
        resistances.computeIfAbsent(uuid, k -> new EnumMap<>(ElementType.class))
                .put(type, Math.min(0.75, Math.max(-0.5, value))); // 最大75%軽減
    }

    /** プレイヤーの属性耐性を取得する (0.0=等倍, 0.5=50%軽減, -0.5=50%弱点) */
    public static double getResistance(UUID uuid, ElementType type) {
        EnumMap<ElementType, Double> map = resistances.get(uuid);
        return map == null ? 0.0 : map.getOrDefault(type, 0.0);
    }

    /** プレイヤーのキャッシュをクリア（ログアウト時） */
    public static void clearPlayer(UUID uuid) {
        resistances.remove(uuid);
    }

    // ─── ダメージ計算 ────────────────────────

    /**
     * 属性相性を考慮した最終ダメージ倍率を返す。
     *
     * @param attackElement 攻撃側の属性
     * @param defenderUuid  防御側のUUID（Playerの場合; nullなら耐性0）
     * @param defenseElement 防御側の属性（Mob属性）
     */
    public static double calcDamageMultiplier(ElementType attackElement,
                                               UUID defenderUuid,
                                               ElementType defenseElement) {
        double affinity   = attackElement.getAffinityMultiplier(defenseElement);
        double resistance = defenderUuid != null ? getResistance(defenderUuid, attackElement) : 0.0;
        return affinity * (1.0 - resistance);
    }

    /** 全プレイヤー耐性マップを返す（デバッグ用） */
    public static Map<ElementType, Double> getAllResistances(UUID uuid) {
        return resistances.getOrDefault(uuid, new EnumMap<>(ElementType.class));
    }
}
