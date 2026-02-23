package com.woxloi.mythicrpg.combat;

import com.woxloi.mythicrpg.combo.ComboManager;
import com.woxloi.mythicrpg.equipment.EquipmentManager;
import com.woxloi.mythicrpg.equipment.model.EquipStats;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import com.woxloi.mythicrpg.core.PluginToggleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

/**
 * プレイヤー vs Mob の戦闘ダメージをRPGダメージ式に置き換えるリスナー。
 *
 * 【計算式】
 *   攻撃ダメージ = (baseAtk + equipAtk) × comboMult × critMult
 *   Mobへの最終ダメージ: そのまま setDamage（バニラHP減算）
 *
 * 【Mobからプレイヤーへのダメージ】
 *   被ダメージ = baseMobDamage × (100 / (100 + totalDef))
 *   ＝ 防御力で軽減するが0にはならない
 *
 * ※ PvPダメージはPvpListenerで別途処理されるためここでは対象外。
 * ※ コンボ・クリティカルはComboListenerでも適用されている可能性があるため、
 *    このリスナーはそちらより低い優先度(NORMAL)で動作し
 *    ComboListener(HIGH)の後に発火する。ただしCOMBOは独自管理のため重複を避ける。
 */
public class MobDamageListener implements Listener {

    private static final Random RNG = new Random();

    // ──────────────────────────────────────────────
    //  プレイヤー → Mob攻撃
    // ──────────────────────────────────────────────
    /**
     * プレイヤーがMobを攻撃した際、RPGステータスに基づいてダメージを計算する。
     * ComboListener(HIGH)の後 → このリスナー(NORMAL)の順で処理される。
     * ComboListenerが既にcrit/combo倍率を適用しているため、ここでは基礎ダメージのみ置き換える。
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerAttackMob(EntityDamageByEntityEvent event) {
        if (!PluginToggleManager.isEnabled("combat")) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (event.getEntity() instanceof Player) return; // PvPはスキップ（PvpListenerが処理）

        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        EquipStats equip = EquipmentManager.getTotalStats(player);

        // 基礎攻撃力
        double totalAtk = data.getAttack() + equip.attack;

        // クリティカル判定（ComboListenerに委譲しているが、万一未適用の場合のフォールバック）
        boolean isCrit = RNG.nextDouble() < equip.critRate;
        double critMult = isCrit ? (1.5 + equip.critDamage) : 1.0;

        // コンボ倍率
        double comboMult = ComboManager.getDamageMultiplier(player);

        // 最終ダメージ
        double finalDamage = Math.max(1.0, totalAtk * critMult * comboMult);
        event.setDamage(finalDamage);

        // クリティカル時のActionBar表示
        if (isCrit) {
            player.sendActionBar(Component.text(
                "§e§lCRITICAL! §c§l" + String.format("%.0f", finalDamage)));
        }
    }

    // ──────────────────────────────────────────────
    //  Mob → プレイヤー被ダメージ (防御力軽減)
    // ──────────────────────────────────────────────
    /**
     * Mobからプレイヤーへのダメージに防御力軽減を適用する。
     * 軽減式: finalDamage = rawDamage × (100 / (100 + defense))
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMobAttackPlayer(EntityDamageByEntityEvent event) {
        if (!PluginToggleManager.isEnabled("combat")) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getDamager() instanceof Player) return; // PvPはスキップ

        // Mobか確認（プレイヤー以外のLivingEntityからのダメージ対象）
        if (!(event.getDamager() instanceof LivingEntity)) return;

        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        EquipStats equip = EquipmentManager.getTotalStats(player);
        double defense = equip.defense;

        // 防御軽減: rawDamage × (100 / (100 + defense))
        double rawDamage = event.getDamage();
        double reducedDamage = Math.max(0.5, rawDamage * (100.0 / (100.0 + defense)));

        event.setDamage(reducedDamage);

        // 防御力が高い時は軽減量をアクションバーに表示
        double reduction = rawDamage - reducedDamage;
        if (reduction >= 1.0) {
            player.sendActionBar(Component.text(
                "§7-" + String.format("%.0f", reducedDamage) + " §8(軽減: " + String.format("%.0f", reduction) + ")"));
        }
    }
}
