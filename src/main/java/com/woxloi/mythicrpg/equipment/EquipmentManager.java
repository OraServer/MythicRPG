package com.woxloi.mythicrpg.equipment;

import com.woxloi.mythicrpg.equipment.model.EquipSlot;
import com.woxloi.mythicrpg.equipment.model.EquipStats;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.model.RpgItemSerializer;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * プレイヤーごとの装備スロット状態を管理する。
 * DB保存はEquipmentRepositoryに委譲。
 */
public class EquipmentManager {

    /** UUID → 装備スロットマップ */
    private static final Map<UUID, EnumMap<EquipSlot, RpgItem>> equips = new ConcurrentHashMap<>();

    /* =====================
       取得
     ===================== */
    public static EnumMap<EquipSlot, RpgItem> getEquipMap(Player player) {
        return equips.computeIfAbsent(player.getUniqueId(), k -> new EnumMap<>(EquipSlot.class));
    }

    public static RpgItem getEquipped(Player player, EquipSlot slot) {
        return getEquipMap(player).get(slot);
    }

    /* =====================
       装備・外す
     ===================== */

    /**
     * 装備を試みる。失敗理由を返す（null = 成功）。
     */
    public static String equip(Player player, RpgItem item) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return "プレイヤーデータが見つかりません";

        // レベル確認
        if (data.getLevel() < item.requiredLevel)
            return "必要レベル " + item.requiredLevel + " に達していません";

        // ジョブ確認
        if (!item.allowedJobs.isEmpty() && (data.getJob() == null || !item.allowedJobs.contains(data.getJob())))
            return "このジョブでは装備できません";

        // 装備
        getEquipMap(player).put(item.slot, item);
        applyStats(player);
        return null;  // 成功
    }

    public static void unequip(Player player, EquipSlot slot) {
        getEquipMap(player).remove(slot);
        applyStats(player);
    }

    /* =====================
       合計ステータス計算
     ===================== */

    /** 全装備の合計ステータスを返す */
    public static EquipStats getTotalStats(Player player) {
        EquipStats total = new EquipStats();
        for (RpgItem item : getEquipMap(player).values()) {
            total = total.add(item.getEffectiveStats());
        }
        return total;
    }

    /** PlayerData にステータスを反映する */
    public static void applyStats(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        EquipStats total = getTotalStats(player);

        // 装備ボーナスを PlayerData に反映
        data.setEquipAttackBonus(total.attack);
        data.setEquipDefenseBonus(total.defense);
        data.setEquipMaxHpBonus(total.maxHpBonus);
        data.setEquipMaxMpBonus(total.maxMpBonus);
        data.setEquipMaxSpBonus(total.maxSpBonus);
        data.setEquipCritRate(total.critRate);
        data.setEquipCritDamage(total.critDamage);
        data.setEquipMagicPower(total.magicPower);

        // 移動速度
        double baseSpeed = 0.2;
        player.setWalkSpeed((float) Math.min(1.0, baseSpeed + total.speed));

        // Bukkit の max_health アトリビュートを PlayerData に合わせて更新
        com.woxloi.mythicrpg.combat.CombatListener.applyMaxHealthAttribute(player, data);
    }

    /* =====================
       ロード/アンロード
     ===================== */
    public static void loadPlayer(UUID uuid, EnumMap<EquipSlot, RpgItem> loaded) {
        equips.put(uuid, loaded);
    }

    public static void unloadPlayer(UUID uuid) {
        equips.remove(uuid);
    }
}
