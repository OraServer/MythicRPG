package com.woxloi.mythicrpg.artifact;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.core.PluginToggleManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    // 復活直後の連続発動防止用
    private final Set<UUID> reviveCooldown = new HashSet<>();

    /**
     * ログイン時に全セットボーナスを初期計算。
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!PluginToggleManager.isEnabled("artifact")) return;

        Bukkit.getScheduler().runTaskLater(
                MythicRPG.getInstance(),
                () -> ArtifactManager.recalculate(e.getPlayer()),
                2L
        );
    }

    /**
     * インベントリクリック時 - アーマースロット変更を検知。
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!PluginToggleManager.isEnabled("artifact")) return;
        if (!(e.getWhoClicked() instanceof Player player)) return;

        if (e.getClickedInventory() == null) return;

        // 「下側のインベントリ（プレイヤー）」のみ対象にする
        if (!e.getClickedInventory().equals(e.getView().getBottomInventory())) return;

        if (e.getSlotType() != InventoryType.SlotType.ARMOR) return;

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
        if (!PluginToggleManager.isEnabled("artifact")) return;

        Bukkit.getScheduler().runTaskLater(
                MythicRPG.getInstance(),
                () -> ArtifactManager.recalculate(e.getPlayer()),
                1L
        );
    }

    /**
     * オフハンド切替。
     */
    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent e) {
        if (!PluginToggleManager.isEnabled("artifact")) return;

        Bukkit.getScheduler().runTaskLater(
                MythicRPG.getInstance(),
                () -> ArtifactManager.recalculate(e.getPlayer()),
                1L
        );
    }

    /**
     * 死亡時: ANCIENT_KING セット2段階以上で20%の確率で復活。
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent e) {
        if (!PluginToggleManager.isEnabled("artifact")) return;
        if (!(e.getEntity() instanceof Player player)) return;
        if (!ArtifactManager.shouldRevive(player)) return;
        if (reviveCooldown.contains(player.getUniqueId())) return;

        if (player.getHealth() - e.getFinalDamage() <= 0) {

            e.setCancelled(true);

            reviveCooldown.add(player.getUniqueId());

            player.setHealth(player.getMaxHealth() * 0.5);
            player.setNoDamageTicks(40); // 2秒無敵

            player.sendMessage(MythicRPG.PREFIX
                    + "§5§l【古代王の奇跡】§f 死亡を免れた！(残HP 50%)");

            ArtifactManager.recalculate(player);

            // 3秒後にクールダウン解除
            Bukkit.getScheduler().runTaskLater(
                    MythicRPG.getInstance(),
                    () -> reviveCooldown.remove(player.getUniqueId()),
                    60L
            );
        }
    }
}
