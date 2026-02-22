package com.woxloi.mythicrpg.combo;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * プレイヤーのコンボ状態を管理。
 *
 * コンボ仕様:
 * - 攻撃ヒットごとにコンボカウントが+1
 * - 3秒以内に次の攻撃をしないとリセット
 * - コンボ数に応じてダメージ倍率が上昇
 *   コンボ1〜4   : ×1.0
 *   コンボ5〜9   : ×1.1  (§e2コンボ!)
 *   コンボ10〜19 : ×1.25 (§610コンボ!!)
 *   コンボ20〜29 : ×1.5  (§c20コンボ!!!)
 *   コンボ30+    : ×2.0  (§4§lMAX COMBO!)
 */
public class ComboManager {

    public static final long COMBO_WINDOW_MS = 3000L;  // コンボ有効期間

    private static final Map<UUID, List<ComboEntry>> comboMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> maxComboMap = new ConcurrentHashMap<>();

    /* =====================
       コンボ追加
     ===================== */

    /**
     * 攻撃ヒット時に呼ぶ。
     * @return 現在のコンボ数
     */
    public static int addHit(Player player, double damage, boolean isCrit) {
        UUID uuid = player.getUniqueId();
        List<ComboEntry> entries = comboMap.computeIfAbsent(uuid, k -> new ArrayList<>());

        // 有効期限切れのコンボを削除
        entries.removeIf(e -> !e.isValid(COMBO_WINDOW_MS));

        ComboType type = isCrit ? ComboType.CRITICAL : ComboType.BASIC;
        entries.add(new ComboEntry(type, damage));

        int count = entries.size();

        // 最大コンボ更新
        maxComboMap.merge(uuid, count, Math::max);

        // コンボ通知
        notifyCombo(player, count);

        return count;
    }

    /** スキル使用時のコンボ追加 */
    public static int addSkillHit(Player player, double damage) {
        UUID uuid = player.getUniqueId();
        List<ComboEntry> entries = comboMap.computeIfAbsent(uuid, k -> new ArrayList<>());
        entries.removeIf(e -> !e.isValid(COMBO_WINDOW_MS));
        entries.add(new ComboEntry(ComboType.SKILL, damage));
        int count = entries.size();
        maxComboMap.merge(uuid, count, Math::max);
        return count;
    }

    /* =====================
       コンボリセット
     ===================== */

    public static void reset(Player player) {
        comboMap.remove(player.getUniqueId());
    }

    /* =====================
       ダメージ倍率計算
     ===================== */

    public static double getDamageMultiplier(Player player) {
        int count = getComboCount(player);
        if (count < 5)  return 1.0;
        if (count < 10) return 1.1;
        if (count < 20) return 1.25;
        if (count < 30) return 1.5;
        return 2.0;
    }

    /* =====================
       取得
     ===================== */

    public static int getComboCount(Player player) {
        List<ComboEntry> entries = comboMap.get(player.getUniqueId());
        if (entries == null) return 0;
        entries.removeIf(e -> !e.isValid(COMBO_WINDOW_MS));
        return entries.size();
    }

    public static int getMaxCombo(Player player) {
        return maxComboMap.getOrDefault(player.getUniqueId(), 0);
    }

    public static void resetMaxCombo(Player player) {
        maxComboMap.remove(player.getUniqueId());
    }

    /* =====================
       通知
     ===================== */

    private static void notifyCombo(Player player, int count) {
        if (count == 5)  player.sendActionBar(net.kyori.adventure.text.Component.text("§e§l5コンボ！ ×1.1ダメージ"));
        if (count == 10) player.sendActionBar(net.kyori.adventure.text.Component.text("§6§l10コンボ！！ ×1.25ダメージ"));
        if (count == 20) player.sendActionBar(net.kyori.adventure.text.Component.text("§c§l20コンボ！！！ ×1.5ダメージ"));
        if (count == 30) player.sendActionBar(net.kyori.adventure.text.Component.text("§4§lMAX COMBO!!! ×2.0ダメージ！！"));
    }
}
