package com.woxloi.mythicrpg.combat;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import com.woxloi.mythicrpg.ui.ScoreboardManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * バニラのHPイベントとPlayerData.hpを双方向に同期する。
 *
 * 【なぜ必要か】
 * PlayerDataはHP/MaxHPを独自フィールドで管理しているが、
 * バニラのEntityDamageEvent等ではBukkit側のgetHealth()が変動する。
 * この2つを連動させないとスコアボード・アクションバー・各種計算が狂う。
 *
 * 【方式】
 * - ダメージ発生後（MONITOR優先度）にBukkit HPを読み取りPlayerDataに同期
 * - PlayerDataのMaxHPをBukkitのmax_healthアトリビュートに反映
 * - 死亡時はHP=0に、リスポーン時はMaxHP全回復
 */
public class CombatListener implements Listener {

    /**
     * ダメージ後にBukkit HPをPlayerDataへ同期する。
     * MONITOR優先度で処理されたダメージ量を確定値として読む。
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        // Bukkit HPを正規化してPlayerDataに反映（1tick後に確定値を取得）
        MythicRPG.getInstance().getServer().getScheduler().runTask(
                MythicRPG.getInstance(), () -> {
                    if (!player.isOnline()) return;
                    // Bukkit HP (0〜20) → PlayerData HP (0〜maxHp) にスケーリング
                    double ratio = player.getHealth() / player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    data.setHp(data.getTotalMaxHp() * ratio);
                    ScoreboardManager.update(player);
                });
    }

    /**
     * HP回復イベントも同様に同期する。
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        MythicRPG.getInstance().getServer().getScheduler().runTask(
                MythicRPG.getInstance(), () -> {
                    if (!player.isOnline()) return;
                    double ratio = player.getHealth() / player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    data.setHp(data.getTotalMaxHp() * ratio);
                    ScoreboardManager.update(player);
                });
    }

    /**
     * 死亡時: PlayerData.hp = 0 に確定する。
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData data = PlayerDataManager.get(player);
        if (data != null) {
            data.setHp(0);
            ScoreboardManager.update(player);
        }
        // キル側の称号カウントはMobKillListenerで処理
    }

    /**
     * リスポーン時: PlayerData.hp を MaxHP に全回復する。
     * さらにBukkit側のmax_healthアトリビュートもPlayerDataに合わせて設定する。
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        // 1tick後にBukkit HP・アトリビュートを更新
        MythicRPG.getInstance().getServer().getScheduler().runTask(
                MythicRPG.getInstance(), () -> {
                    applyMaxHealthAttribute(player, data);
                    data.setHp(data.getTotalMaxHp()); // 全回復
                    ScoreboardManager.update(player);
                });
    }

    /**
     * PlayerDataのMaxHPをBukkitのmax_healthアトリビュートに反映する。
     * PlayerData.maxHp は RPGのHP上限（例: 500）で、Bukkit側は1〜2048の範囲。
     * ここではPlayerDataのMaxHPをそのままBukkit側に設定し、
     * Bukkit HPの変動をPlayerData HPにスケーリングで同期する方式を取る。
     *
     * 最大値はBukkitの上限2048に丸める。
     */
    public static void applyMaxHealthAttribute(Player player, PlayerData data) {
        var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;
        double newMax = Math.min(2048.0, Math.max(1.0, data.getTotalMaxHp()));
        attr.setBaseValue(newMax);
        // 現在HPも超過しないよう調整
        if (player.getHealth() > newMax) {
            player.setHealth(newMax);
        }
    }
}
