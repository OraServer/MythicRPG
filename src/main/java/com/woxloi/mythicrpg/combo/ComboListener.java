package com.woxloi.mythicrpg.combo;

import com.woxloi.mythicrpg.equipment.EquipmentManager;
import com.woxloi.mythicrpg.equipment.model.EquipStats;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Random;

/**
 * コンボシステムのリスナー。
 * - 攻撃ヒット → コンボカウント
 * - 死亡/ログアウト → コンボリセット
 * - ダメージ計算にコンボ倍率と会心率を適用
 */
public class ComboListener implements Listener {

    private static final Random RNG = new Random();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;

        EquipStats stats = EquipmentManager.getTotalStats(player);

        // 会心判定
        double critRate = stats.critRate;
        boolean isCrit = RNG.nextDouble() < critRate;
        double baseDamage = e.getDamage();

        if (isCrit) {
            double multiplier = 1.5 + stats.critDamage;
            baseDamage *= multiplier;
            player.sendActionBar(net.kyori.adventure.text.Component.text(
                    "§e§lCRITICAL! §c§l" + String.format("%.0f", baseDamage)));
        }

        // コンボ倍率
        int combo = ComboManager.addHit(player, baseDamage, isCrit);
        double comboMultiplier = ComboManager.getDamageMultiplier(player);
        baseDamage *= comboMultiplier;

        // 最終ダメージ設定
        e.setDamage(baseDamage);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        ComboManager.reset(e.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        ComboManager.reset(e.getPlayer());
    }
}
