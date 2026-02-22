package com.woxloi.mythicrpg.equipment.transfer;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.equipment.EquipmentManager;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.model.RpgItemSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ステータス継承GUIリスナー。
 */
public class TransferGUIListener implements Listener {

    /** プレイヤーごとのセッション */
    private static final Map<UUID, RpgItem> sourceMap = new HashMap<>();
    private static final Map<UUID, RpgItem> targetMap = new HashMap<>();
    private static final Map<UUID, Integer> sourceSlotMap = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!TransferGUI.TITLE.equals(event.getView().getTitle())) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // 継承元スロット(11): メインハンドのアイテムをセット
        if (slot == TransferGUI.SLOT_SOURCE) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            RpgItem item = RpgItemSerializer.deserialize(hand);
            if (item == null) {
                MythicRPG.msg(player, "§c手に装備を持った状態でクリックしてください");
                return;
            }
            sourceMap.put(player.getUniqueId(), item);
            MythicRPG.msg(player, "§b継承元に §e" + item.displayName + " §bをセットしました");
            refreshGUI(player);
            return;
        }

        // 継承先スロット(15): メインハンドのアイテムをセット
        if (slot == TransferGUI.SLOT_TARGET) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            RpgItem item = RpgItemSerializer.deserialize(hand);
            if (item == null) {
                MythicRPG.msg(player, "§c手に装備を持った状態でクリックしてください");
                return;
            }
            targetMap.put(player.getUniqueId(), item);
            MythicRPG.msg(player, "§a継承先に §e" + item.displayName + " §aをセットしました");
            refreshGUI(player);
            return;
        }

        // 実行スロット(13)
        if (slot == TransferGUI.SLOT_EXECUTE) {
            UUID uuid = player.getUniqueId();
            RpgItem source = sourceMap.get(uuid);
            RpgItem target = targetMap.get(uuid);

            if (source == null || target == null) {
                MythicRPG.msg(player, "§c継承元と継承先を両方セットしてください");
                return;
            }

            String err = TransferManager.transfer(source, target);
            if (err != null) {
                MythicRPG.msg(player, "§c" + err);
                return;
            }

            // インベントリから継承元を削除し、継承先を更新
            removeItemFromInventory(player, source);
            updateItemInInventory(player, target);

            sourceMap.remove(uuid);
            targetMap.remove(uuid);

            EquipmentManager.applyStats(player);
            MythicRPG.msg(player, "§a✓ ステータス継承が完了しました！");
            player.closeInventory();
        }
    }

    private void refreshGUI(Player player) {
        UUID uuid = player.getUniqueId();
        TransferGUI.open(player, sourceMap.get(uuid), targetMap.get(uuid));
    }

    private void removeItemFromInventory(Player player, RpgItem item) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack is = player.getInventory().getItem(i);
            if (is == null) continue;
            RpgItem ri = RpgItemSerializer.deserialize(is);
            if (ri != null && ri.id.equals(item.id)) {
                player.getInventory().setItem(i, null);
                return;
            }
        }
    }

    private void updateItemInInventory(Player player, RpgItem item) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack is = player.getInventory().getItem(i);
            if (is == null) continue;
            RpgItem ri = RpgItemSerializer.deserialize(is);
            if (ri != null && ri.id.equals(item.id)) {
                player.getInventory().setItem(i, RpgItemSerializer.serialize(item));
                return;
            }
        }
        player.updateInventory();
    }
}
