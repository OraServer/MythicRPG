package com.woxloi.mythicrpg.job;

import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class JobListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle().equals("§6§lジョブを選択")) {

            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            PlayerData data = PlayerDataManager.get(player);
            if (data == null || data.hasJob()) return;

            switch (event.getCurrentItem().getType()) {
                case IRON_SWORD -> select(player, JobType.WARRIOR);
                case BLAZE_ROD -> select(player, JobType.MAGE);
                case BOW -> select(player, JobType.ARCHER);
            }
        }
    }

    private void select(Player player, JobType job) {
        JobManager.setJob(player, job);
        player.closeInventory();
        player.sendMessage("§aジョブを §e" + job.getDisplayName() + " §aに設定しました！");
    }
}
