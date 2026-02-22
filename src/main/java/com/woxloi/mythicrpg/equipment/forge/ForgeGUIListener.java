package com.woxloi.mythicrpg.equipment.forge;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 鍛冶台GUIのクリックイベントを処理するリスナー。
 */
public class ForgeGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!ForgeGUI.getTitle().equals(title)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 45) return; // ボーダー or 範囲外

        List<ForgeRecipe> recipes = ForgeManager.getAllRecipes();
        if (slot >= recipes.size()) return;

        ForgeRecipe recipe = recipes.get(slot);

        // 製作実行
        boolean success = ForgeManager.craft(player, recipe);
        if (!success) {
            player.sendMessage(MythicRPG.PREFIX + "§c素材が不足しているか、レベルが足りません。");
        }
    }
}
