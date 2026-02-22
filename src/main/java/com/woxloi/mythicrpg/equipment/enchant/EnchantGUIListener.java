package com.woxloi.mythicrpg.equipment.enchant;

import com.woxloi.mythicrpg.MythicRPG;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * エンチャント台GUIのクリックイベントを処理するリスナー。
 */
public class EnchantGUIListener implements Listener {

    /** プレイヤーごとの選択状態 */
    private static final Map<UUID, EnchantType> selectedType = new HashMap<>();
    private static final Map<UUID, Integer> selectedRank = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!EnchantGUI.getTitle().equals(title)) return;

        event.setCancelled(true);

        int rawSlot = event.getRawSlot();
        UUID uuid = player.getUniqueId();

        // エンチャント種別スロット（左列）
        int[] typeSlots = {0, 9, 18, 27, 36, 1, 10, 19, 28, 37};
        EnchantType[] types = EnchantType.values();
        for (int i = 0; i < typeSlots.length && i < types.length; i++) {
            if (rawSlot == typeSlots[i]) {
                selectedType.put(uuid, types[i]);
                player.sendMessage(MythicRPG.PREFIX + "§e" + types[i].getDisplayName() + " §7を選択しました");
                return;
            }
        }

        // ランク選択スロット（右列）
        int[] rankSlots = {8, 17, 26, 35, 44};
        for (int r = 0; r < rankSlots.length; r++) {
            if (rawSlot == rankSlots[r]) {
                selectedRank.put(uuid, r + 1);
                player.sendMessage(MythicRPG.PREFIX + "§aRank " + (r + 1) + " §7を選択しました");
                return;
            }
        }

        // 確認ボタン
        if (rawSlot == EnchantGUI.CONFIRM_SLOT) {
            EnchantType type = selectedType.get(uuid);
            Integer rank = selectedRank.get(uuid);

            if (type == null || rank == null) {
                player.sendMessage(MythicRPG.PREFIX + "§cエンチャント種別とランクを選択してください");
                return;
            }

            // 対象アイテムチェック
            var targetItem = event.getInventory().getItem(EnchantGUI.ITEM_SLOT);
            if (targetItem == null || targetItem.getType().isAir()) {
                player.sendMessage(MythicRPG.PREFIX + "§cエンチャント対象の装備をスロットに置いてください");
                return;
            }

            // ここでRpgItem取得・エンチャント付与（将来的にシリアライザー経由）
            player.sendMessage(MythicRPG.PREFIX + "§a" + type.getDisplayName() + " Rank " + rank + " §7を付与しました！");
            selectedType.remove(uuid);
            selectedRank.remove(uuid);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!EnchantGUI.getTitle().equals(event.getView().getTitle())) return;

        UUID uuid = player.getUniqueId();
        selectedType.remove(uuid);
        selectedRank.remove(uuid);
    }
}
