package com.woxloi.mythicrpg.artifact;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.core.PluginToggleManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * 装備変更を検知してアーティファクトセットボーナスを再計算するリスナー。
 *
 * 検知イベント:
 *  - ログイン          → 初回計算
 *  - インベントリクリック → アーマースロット変更検知
 *  - メインハンド切替    → セット枚数変化の可能性
 *  - オフハンド切替     → 同上
 *  - ANCIENT_KING 復活判定 → 死亡時
 */
public class ArtifactListener implements Listener {

    /**
     * ログイン時に全セットボーナスを初期計算。
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!PluginToggleManager.isEnabled("artifact")) return;
        // 1tick後に実行（インベントリ読み込み完了を待つ）
        MythicRPG.getInstance().getServer().getScheduler()
                .runTaskLater(MythicRPG.getInstance(),
                        () -> ArtifactManager.recalculate(e.getPlayer()),
                        2L);
    }

    /**
     * インベントリクリック時 - アーマースロット変更を検知。
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        boolean isArmorSlot = e.getSlotType() == InventoryType.SlotType.ARMOR
                || (e.getInventory().getType() == InventoryType.PLAYER
                && isArmorSlotIndex(e.getRawSlot()));
        if (!isArmorSlot) return;

        // 1tick後にボーナス再計算（インベントリ操作完了後）
        MythicRPG.getInstance().getServer().getScheduler()
                .runTaskLater(MythicRPG.getInstance(),
                        () -> ArtifactManager.recalculate(player),
                        1L);
    }

    private boolean isArmorSlotIndex(int rawSlot) {
        return rawSlot >= 36 && rawSlot <= 39;
    }

    /**
     * メインハンド切替 - 武器アーティファクトのセット枚数が変わる可能性。
     */
    @EventHandler
    public void onHeldChange(PlayerItemHeldEvent e) {
        MythicRPG.getInstance().getServer().getScheduler()
                .runTaskLater(MythicRPG.getInstance(),
                        () -> ArtifactManager.recalculate(e.getPlayer()),
                        1L);
    }

    /**
     * オフハンド切替。
     */
    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent e) {
        MythicRPG.getInstance().getServer().getScheduler()
                .runTaskLater(MythicRPG.getInstance(),
                        () -> ArtifactManager.recalculate(e.getPlayer()),
                        1L);
    }

    /**
     * 死亡時: ANCIENT_KING セット2段階以上で20%の確率で復活。
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (!ArtifactManager.shouldRevive(player)) return;

        // キャンセルして復活処理
        e.setCancelled(false); // 死亡自体は発生させ、リスポーン後にHP回復
        MythicRPG.getInstance().getServer().getScheduler()
                .runTaskLater(MythicRPG.getInstance(), () -> {
                    if (player.isOnline()) {
                        player.setHealth(player.getMaxHealth() * 0.5);
                        player.sendMessage(MythicRPG.PREFIX
                                + "§5§l【古代王の奇跡】§f 死亡を免れた！(残HP 50%)");
                        ArtifactManager.recalculate(player);
                    }
                }, 1L);
    }
}
