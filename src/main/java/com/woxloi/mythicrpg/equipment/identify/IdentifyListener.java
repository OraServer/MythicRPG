package com.woxloi.mythicrpg.equipment.identify;

import com.woxloi.mythicrpg.MythicRPG;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 鑑定書を使って未鑑定装備を鑑定するリスナー。
 * 鑑定書を手に持ってインベントリ内の未鑑定アイテムを右クリックする。
 */
public class IdentifyListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        // 手に鑑定書を持っているか確認
        if (!IdentifyManager.isIdentifyScroll(mainHand)) return;

        // 右クリックのみ処理
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
         && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;

        event.setCancelled(true);

        // インベントリから未鑑定アイテムを探す
        ItemStack unidentified = null;
        int unidSlot = -1;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack is = player.getInventory().getItem(i);
            if (IdentifyManager.isUnidentified(is)) {
                unidentified = is;
                unidSlot = i;
                break;
            }
        }

        if (unidentified == null) {
            MythicRPG.msg(player, "§cインベントリに未鑑定装備がありません");
            return;
        }

        // 鑑定実行
        ItemStack identified = IdentifyManager.identify(unidentified);
        if (identified == null) {
            MythicRPG.msg(player, "§c鑑定に失敗しました");
            return;
        }

        // 未鑑定アイテムを鑑定済みに置き換え
        player.getInventory().setItem(unidSlot, identified);

        // 鑑定書を1個消費
        if (mainHand.getAmount() > 1) {
            mainHand.setAmount(mainHand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        player.updateInventory();
        MythicRPG.msg(player, ChatColor.GOLD + "✦ 鑑定成功！ " + ChatColor.RESET
                + identified.getItemMeta().getDisplayName());

        // 効果音・パーティクル
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        player.getWorld().spawnParticle(
                org.bukkit.Particle.ENCHANTMENT_TABLE,
                player.getLocation().add(0, 1, 0),
                30, 0.5, 0.5, 0.5, 0.1
        );
    }
}
