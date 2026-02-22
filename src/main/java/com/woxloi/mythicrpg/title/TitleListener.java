package com.woxloi.mythicrpg.title;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

/**
 * 称号解放条件を監視するリスナー
 * (Mob討伐・レベルUP はそれぞれの既存リスナーから TitleManager を呼ぶ)
 */
public class TitleListener implements Listener {

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        TitleManager.incrementCraft(player);
    }
}
