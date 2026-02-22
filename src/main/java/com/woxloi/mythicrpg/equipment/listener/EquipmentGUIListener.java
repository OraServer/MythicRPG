package com.woxloi.mythicrpg.equipment.listener;

import com.woxloi.mythicrpg.equipment.EquipmentManager;
import com.woxloi.mythicrpg.equipment.db.EquipmentRepository;
import com.woxloi.mythicrpg.equipment.enhancer.EnhanceManager;
import com.woxloi.mythicrpg.equipment.gui.EquipmentGUI;
import com.woxloi.mythicrpg.equipment.model.EquipSlot;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.model.RpgItemSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 装備GUI（EquipmentGUI）のクリックイベント処理。
 * - スロット枠をクリック → 装備/外す
 * - 強化ボタン(51) → 手持ちアイテムを強化
 */
public class EquipmentGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getView() == null) return;

        var title = e.getView().title();
        if (!title.equals(Component.text(EquipmentGUI.TITLE))) return;

        e.setCancelled(true);

        int slot = e.getRawSlot();

        // 強化ボタン
        if (slot == 51) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (!RpgItemSerializer.isRpgItem(hand)) {
                player.sendMessage("§cRPGアイテムを手に持ってください");
                return;
            }
            RpgItem item = RpgItemSerializer.fromItemStack(hand);
            String result = EnhanceManager.tryEnhance(player, item);
            if (result == null) {
                // 成功 - アイテムを更新
                player.getInventory().setItemInMainHand(RpgItemSerializer.toItemStack(item));
                player.sendMessage("§a§l強化成功！ §e+" + item.enhanceLevel);
                // 装備中なら再適用
                if (EquipmentManager.getEquipped(player, item.slot) != null) {
                    EquipmentManager.equip(player, item);
                    EquipmentRepository.saveSlot(player.getUniqueId(), item.slot, item);
                }
                // GUI更新
                Bukkit.getScheduler().runTaskLater(
                        org.bukkit.Bukkit.getPluginManager().getPlugin("MythicRPG"),
                        () -> EquipmentGUI.open(player), 1L);
            } else {
                player.sendMessage("§c" + result);
            }
            return;
        }

        // スロットクリック → 装備/外す
        for (EquipSlot equipSlot : EquipSlot.values()) {
            if (EquipmentGUI.slotToIndex(equipSlot) != slot) continue;

            RpgItem equipped = EquipmentManager.getEquipped(player, equipSlot);

            if (equipped != null) {
                // 外す → インベントリに戻す
                EquipmentManager.unequip(player, equipSlot);
                EquipmentRepository.deleteSlot(player.getUniqueId(), equipSlot);
                ItemStack give = RpgItemSerializer.toItemStack(equipped);
                player.getInventory().addItem(give);
                player.sendMessage("§e" + equipped.slot.displayName + "を外しました");
            } else {
                // カーソルのアイテムを装備
                ItemStack cursor = e.getCursor();
                if (cursor == null || cursor.getType().isAir()) {
                    player.sendMessage("§7装備したいアイテムをカーソルに持って、スロットをクリックしてください");
                    return;
                }
                RpgItem rpgItem = RpgItemSerializer.fromItemStack(cursor);
                if (rpgItem == null) {
                    player.sendMessage("§cこのアイテムはRPGアイテムではありません");
                    return;
                }
                if (rpgItem.slot != equipSlot) {
                    player.sendMessage("§cこのスロットには装備できません（スロット: " + rpgItem.slot.displayName + "）");
                    return;
                }
                String err = EquipmentManager.equip(player, rpgItem);
                if (err != null) {
                    player.sendMessage("§c" + err);
                    return;
                }
                e.setCursor(null);
                EquipmentRepository.saveSlot(player.getUniqueId(), equipSlot, rpgItem);
                player.sendMessage("§a" + rpgItem.displayName + "§aを装備しました");
            }

            // GUI更新
            Bukkit.getScheduler().runTaskLater(
                    org.bukkit.Bukkit.getPluginManager().getPlugin("MythicRPG"),
                    () -> EquipmentGUI.open(player), 1L);
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getView().title().equals(Component.text(EquipmentGUI.TITLE))) {
            e.setCancelled(true);
        }
    }
}
