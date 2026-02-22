package com.woxloi.mythicrpg.equipment.set;

import com.woxloi.mythicrpg.equipment.EquipmentManager;
import com.woxloi.mythicrpg.equipment.model.EquipSlot;
import com.woxloi.mythicrpg.equipment.model.EquipStats;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * セット効果マネージャー。
 * プレイヤーの装備から同一セットIDを集計し、ボーナスを計算する。
 */
public class SetManager {

    /** 登録済みセット効果マップ */
    private static final Map<String, SetEffect> registeredSets = new HashMap<>();

    static {
        // デフォルトセット定義
        registerDefaultSets();
    }

    private static void registerDefaultSets() {
        // ドラゴンセット
        SetEffect dragon = new SetEffect("dragon_set", "§4ドラゴンの鱗");
        EquipStats t2 = new EquipStats(); t2.attack = 20; t2.defense = 10;
        EquipStats t4 = new EquipStats(); t4.maxHpBonus = 100; t4.critRate = 0.1;
        dragon.addTier(new SetEffect.SetTier(2, t2, "ATK+20, DEF+10"));
        dragon.addTier(new SetEffect.SetTier(4, t4, "MaxHP+100, クリティカル+10%"));
        registeredSets.put("dragon_set", dragon);

        // 賢者セット
        SetEffect sage = new SetEffect("sage_set", "§9賢者の叡智");
        EquipStats s2 = new EquipStats(); s2.magicPower = 15; s2.maxMpBonus = 50;
        EquipStats s4 = new EquipStats(); s4.magicPower = 30; s4.critDamage = 0.5;
        sage.addTier(new SetEffect.SetTier(2, s2, "魔法攻撃+15, MaxMP+50"));
        sage.addTier(new SetEffect.SetTier(4, s4, "魔法攻撃+30, クリダメ+50%"));
        registeredSets.put("sage_set", sage);

        // 影セット
        SetEffect shadow = new SetEffect("shadow_set", "§8影の暗殺者");
        EquipStats sh2 = new EquipStats(); sh2.speed = 0.02; sh2.critRate = 0.1;
        EquipStats sh4 = new EquipStats(); sh4.attack = 40; sh4.critDamage = 1.0;
        shadow.addTier(new SetEffect.SetTier(2, sh2, "移動速度UP, クリ率+10%"));
        shadow.addTier(new SetEffect.SetTier(4, sh4, "ATK+40, クリダメ+100%"));
        registeredSets.put("shadow_set", shadow);
    }

    /**
     * プレイヤーの装備からセットボーナスを計算する。
     */
    public static EquipStats calcSetBonus(Player player) {
        Map<String, Integer> setCount = new HashMap<>();

        // 装備スロット全体を走査
        for (EquipSlot slot : EquipSlot.values()) {
            RpgItem item = EquipmentManager.getEquipped(player, slot);
            if (item == null || item.setId == null) continue;
            setCount.merge(item.setId, 1, Integer::sum);
        }

        // ボーナス合算
        EquipStats total = new EquipStats();
        for (Map.Entry<String, Integer> entry : setCount.entrySet()) {
            SetEffect effect = registeredSets.get(entry.getKey());
            if (effect == null) continue;
            total = total.add(effect.getBonus(entry.getValue()));
        }
        return total;
    }

    /**
     * セットのLoreテキストをプレイヤー向けに生成する。
     */
    public static java.util.List<String> getSetLore(Player player, String setId) {
        SetEffect effect = registeredSets.get(setId);
        if (effect == null) return java.util.Collections.emptyList();

        // 現在の装備枚数を数える
        int count = 0;
        for (EquipSlot slot : EquipSlot.values()) {
            RpgItem item = EquipmentManager.getEquipped(player, slot);
            if (item != null && setId.equals(item.setId)) count++;
        }
        return effect.getLoreLines(count);
    }

    public static void register(SetEffect effect) {
        registeredSets.put(effect.setId, effect);
    }

    public static SetEffect get(String setId) {
        return registeredSets.get(setId);
    }

    public static Map<String, SetEffect> getAll() {
        return registeredSets;
    }
}
