package com.woxloi.mythicrpg.pvp;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PvP攻撃の許可/禁止制御とRPGダメージ式の適用。
 * ゾーン外のPvPを遮断し、ゾーン内ではプレイヤーステータスに基づいてダメージを計算。
 */
public class PvpListener implements Listener {

    /** ゾーン突入メッセージの連続送信防止: UUID → 最終通知時刻 */
    private final Map<UUID, Long> lastZoneNotify = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        boolean attackerInZone = PvpZoneManager.isInPvpZone(attacker.getLocation());
        boolean victimInZone   = PvpZoneManager.isInPvpZone(victim.getLocation());

        // 両方がゾーン内でないとPvPできない
        if (!attackerInZone || !victimInZone) {
            event.setCancelled(true);
            long now = System.currentTimeMillis();
            long last = lastZoneNotify.getOrDefault(attacker.getUniqueId(), 0L);
            if (now - last > 3000) {
                MythicRPG.playerPrefixMsg(attacker, "§cここではPvPできません");
                lastZoneNotify.put(attacker.getUniqueId(), now);
            }
            return;
        }

        // PvPゾーン内: RPGダメージ式を適用
        PlayerData atkData = PlayerDataManager.get(attacker);
        PlayerData defData = PlayerDataManager.get(victim);
        if (atkData == null || defData == null) return;

        // ダメージ = 攻撃力 × (100 / (100 + 防御力))
        double totalAtk = atkData.getAttack() + atkData.getEquipAttackBonus();
        double totalDef = defData.getEquipDefenseBonus();
        double rpgDamage = totalAtk * (100.0 / (100.0 + totalDef));

        // クリティカル判定（攻撃側のcritRate）
        double critRate = atkData.getEquipCritRate();
        if (Math.random() < critRate) {
            rpgDamage *= (1.5 + atkData.getEquipCritDamage());
            attacker.sendActionBar(net.kyori.adventure.text.Component.text("§6§lCRITICAL!"));
        }

        event.setDamage(Math.max(1.0, rpgDamage));

        // キル時のランキング更新はPvpRankingManagerに委譲（onEntityDeath経由）
    }

    /** ゾーン入退場の通知 */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        Player player = event.getPlayer();
        boolean wasIn = PvpZoneManager.isInPvpZone(event.getFrom());
        boolean nowIn = PvpZoneManager.isInPvpZone(event.getTo());

        if (!wasIn && nowIn) {
            PvpZoneManager.PvpZone zone = PvpZoneManager.getZone(event.getTo());
            MythicRPG.playerPrefixMsg(player, "§c⚔ PvPゾーン §e「"
                    + (zone != null ? zone.displayName : "?") + "」§c に入りました！");
        } else if (wasIn && !nowIn) {
            MythicRPG.playerPrefixMsg(player, "§a安全地帯に戻りました");
        }
    }
}
