package com.woxloi.mythicrpg.equipment.socket;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.equipment.EquipmentManager;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.model.RpgItemSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * SocketGUIのクリックイベントを処理するリスナー。
 * 宝石の挿入・取り外しを行い、アイテムのLoreを更新する。
 */
public class SocketGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!SocketGUI.TITLE.equals(event.getView().getTitle())) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // 手に持っている装備を取得
        ItemStack hand = player.getInventory().getItemInMainHand();
        RpgItem rpgItem = RpgItemSerializer.deserialize(hand);
        if (rpgItem == null) {
            MythicRPG.msg(player, "§c手に装備を持ってからソケット加工台を開いてください");
            player.closeInventory();
            return;
        }

        // 上段(0-8): 宝石選択 → セッション保存
        if (slot >= 0 && slot < GemType.values().length) {
            GemType gem = GemType.values()[slot];
            // セッションに選択中の宝石を保存
            SocketSession.setSelectedGem(player.getUniqueId(), gem);
            MythicRPG.msg(player, gem.getColoredName() + "§r を選択しました。下段のスロットをクリックして挿入してください");
            return;
        }

        // 下段(45-53): スロット操作
        if (slot >= 45 && slot <= 53) {
            int slotIndex = slot - 45;
            GemType selectedGem = SocketSession.getSelectedGem(player.getUniqueId());

            if (selectedGem == null) {
                // 取り外し操作
                String err = SocketManager.removeGem(player, rpgItem, slotIndex);
                if (err != null) {
                    MythicRPG.msg(player, "§c" + err);
                } else {
                    MythicRPG.msg(player, "§a宝石を取り外しました（宝石は消滅しました）");
                    updateItemInHand(player, rpgItem);
                }
            } else {
                // 挿入操作
                String err = SocketManager.insertGem(player, rpgItem, slotIndex, selectedGem);
                if (err != null) {
                    MythicRPG.msg(player, "§c" + err);
                } else {
                    MythicRPG.msg(player, "§a" + selectedGem.getColoredName() + "§r を挿入しました！");
                    SocketSession.clearSelectedGem(player.getUniqueId());
                    updateItemInHand(player, rpgItem);
                }
            }
            // GUI再描画
            SocketGUI.open(player, rpgItem);
        }
    }

    private void updateItemInHand(Player player, RpgItem item) {
        ItemStack updated = RpgItemSerializer.serialize(item);
        player.getInventory().setItemInMainHand(updated);
        player.updateInventory();
        // ステータス再適用
        EquipmentManager.applyStats(player);
    }
}
