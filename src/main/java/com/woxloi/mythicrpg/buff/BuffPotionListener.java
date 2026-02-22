package com.woxloi.mythicrpg.buff;

import com.woxloi.mythicrpg.MythicRPG;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * バフポーションを右クリックで使用するリスナー
 */
public class BuffPotionListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        MythicRPG plugin = MythicRPG.getInstance();

        if (!BuffPotion.isBuffPotion(plugin, item)) return;

        event.setCancelled(true);

        BuffType type      = BuffPotion.getType(plugin, item);
        double magnitude   = BuffPotion.getMagnitude(plugin, item);
        int    seconds     = BuffPotion.getDuration(plugin, item);

        if (type == null) return;

        BuffManager.applyBuff(event.getPlayer(), type, magnitude, seconds * 20, "potion");

        // アイテムを1個消費
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            event.getPlayer().getInventory().setItemInMainHand(null);
        }
    }
}
