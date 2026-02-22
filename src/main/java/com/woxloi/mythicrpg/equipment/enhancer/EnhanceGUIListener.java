package com.woxloi.mythicrpg.equipment.enhancer;

import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.model.RpgItemSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 強化GUI（EnhanceGUI）のイベントリスナー。
 * スロット40（強化ボタン）をクリックしたら強化実行。
 * GUIを閉じたらアイテムをインベントリに返却。
 */
public class EnhanceGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getView() == null) return;
        if (!e.getView().title().equals(Component.text(EnhanceGUI.TITLE))) return;

        int raw = e.getRawSlot();

        // 強化ボタン以外はキャンセルしない（アイテム配置を許可）
        if (raw == EnhanceGUI.BUTTON_SLOT) {
            e.setCancelled(true);
            executeEnhance(player);
            return;
        }

        // ガラスパネルはクリック不可
        if (raw < 54 && raw != EnhanceGUI.TARGET_SLOT && raw != EnhanceGUI.MATERIAL_SLOT) {
            var item = e.getCurrentItem();
            if (item != null && (item.getType() == org.bukkit.Material.BLACK_STAINED_GLASS_PANE
                    || item.getType() == org.bukkit.Material.BOOK)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        if (!e.getView().title().equals(Component.text(EnhanceGUI.TITLE))) return;

        // スロットに残ったアイテムを返却
        var inv = e.getInventory();
        ItemStack target   = inv.getItem(EnhanceGUI.TARGET_SLOT);
        ItemStack material = inv.getItem(EnhanceGUI.MATERIAL_SLOT);

        if (target != null && !target.getType().isAir()
                && !target.getType().equals(org.bukkit.Material.ITEM_FRAME)) {
            player.getInventory().addItem(target);
            inv.setItem(EnhanceGUI.TARGET_SLOT, null);
        }
        if (material != null && !material.getType().isAir()
                && !material.getType().equals(org.bukkit.Material.EMERALD)) {
            player.getInventory().addItem(material);
            inv.setItem(EnhanceGUI.MATERIAL_SLOT, null);
        }
    }

    private void executeEnhance(Player player) {
        var inv = player.getOpenInventory().getTopInventory();
        ItemStack targetStack = inv.getItem(EnhanceGUI.TARGET_SLOT);

        if (targetStack == null || targetStack.getType().isAir()
                || targetStack.getType() == org.bukkit.Material.ITEM_FRAME) {
            player.sendMessage("§c強化するアイテムをスロット22に置いてください");
            return;
        }

        RpgItem item = RpgItemSerializer.fromItemStack(targetStack);
        if (item == null) {
            player.sendMessage("§cRPGアイテムではありません");
            return;
        }

        String result = EnhanceManager.tryEnhance(player, item);
        if (result == null) {
            // 成功
            ItemStack updated = RpgItemSerializer.toItemStack(item);
            inv.setItem(EnhanceGUI.TARGET_SLOT, updated);
            player.sendMessage("§a§l強化成功！ §e§l+" + item.enhanceLevel);
        } else if (result.equals("FAIL_SILENT")) {
            // 失敗（メッセージはEnhanceManager内で表示済み）
            ItemStack updated = RpgItemSerializer.toItemStack(item);
            inv.setItem(EnhanceGUI.TARGET_SLOT, updated);
        } else {
            player.sendMessage("§c" + result);
        }

        // GUI更新
        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("MythicRPG"),
                () -> {
                    if (player.isOnline()) EnhanceGUI.open(player);
                }, 2L);
    }
}
