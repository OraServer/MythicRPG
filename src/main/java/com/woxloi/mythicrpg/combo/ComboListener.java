package com.woxloi.mythicrpg.combo;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * コンボシステムのリスナー。
 *
 * 役割:
 *   - 攻撃ヒット → コンボカウントの加算
 *   - 死亡/ログアウト → コンボリセット
 *
 * ※ ダメージ計算（クリティカル・コンボ倍率の適用）はMobDamageListenerが担当。
 *    ここでダメージを変更すると二重計算になるため行わない。
 */
public class ComboListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;
        if (e.getEntity() instanceof Player) return; // PvPはコンボ対象外

        // コンボカウントのみ（ダメージ変更なし）
        ComboManager.addHit(player, e.getFinalDamage(), false);
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

