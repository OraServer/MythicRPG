package com.woxloi.mythicrpg.equipment.refine;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RefineGUIのクリックイベントリスナー。
 */
public class RefineGUIListener implements Listener {

    /** プレイヤーが選択した素材数 (UUID → count) */
    private static final Map<UUID, Integer> selectedMaterial = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!RefineGUI.TITLE.equals(event.getView().getTitle())) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // 手に持っている装備を取得
        ItemStack hand = player.getInventory().getItemInMainHand();
        RpgItem item = RpgItemSerializer.deserialize(hand);
        if (item == null) {
            MythicRPG.msg(player, "§c手に装備を持ってください");
            player.closeInventory();
            return;
        }

        // 素材数選択 (スロット11,12,14)
        if (slot == 11) { selectedMaterial.put(player.getUniqueId(), 1); notifySelected(player, 1); return; }
        if (slot == 12) { selectedMaterial.put(player.getUniqueId(), 3); notifySelected(player, 3); return; }
        if (slot == 14) { selectedMaterial.put(player.getUniqueId(), 5); notifySelected(player, 5); return; }

        // 精錬実行 (スロット22)
        if (slot == 22) {
            int matCount = selectedMaterial.getOrDefault(player.getUniqueId(), 1);

            // 精錬石チェック（インベントリから消費）
            if (!consumeMaterial(player, matCount)) {
                MythicRPG.msg(player, "§c精錬石が不足しています（必要: " + matCount + "個）");
                return;
            }

            RefineManager.RefineResult result = RefineManager.refine(item, matCount);
            MythicRPG.msg(player, result.message);

            // 効果音
            switch (result) {
                case GREAT_SUCCESS -> player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                case SUCCESS       -> player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1f, 1.5f);
                case FAIL          -> player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_LAND, 1f, 0.5f);
                case BREAK         -> player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1f, 0.5f);
            }

            // アイテム更新
            ItemStack updated = RpgItemSerializer.serialize(item);
            player.getInventory().setItemInMainHand(updated);
            player.updateInventory();
            EquipmentManager.applyStats(player);

            // GUI再描画
            RefineGUI.open(player, item);
        }
    }

    private void notifySelected(Player player, int count) {
        MythicRPG.msg(player, "§e精錬石 " + count + "個を選択しました。§a[精錬実行]をクリック");
    }

    private boolean consumeMaterial(Player player, int count) {
        int found = 0;
        for (ItemStack is : player.getInventory().getContents()) {
            if (is != null && is.getType() == org.bukkit.Material.QUARTZ) {
                found += is.getAmount();
            }
        }
        if (found < count) return false;

        int remaining = count;
        for (int i = 0; i < player.getInventory().getSize() && remaining > 0; i++) {
            ItemStack is = player.getInventory().getItem(i);
            if (is == null || is.getType() != org.bukkit.Material.QUARTZ) continue;
            int take = Math.min(is.getAmount(), remaining);
            is.setAmount(is.getAmount() - take);
            remaining -= take;
        }
        player.updateInventory();
        return true;
    }
}
