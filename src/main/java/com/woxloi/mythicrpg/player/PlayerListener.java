package com.woxloi.mythicrpg.player;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.element.ElementManager;
import com.woxloi.mythicrpg.job.JobSelectGUI;
import com.woxloi.mythicrpg.pet.PetManager;
import com.woxloi.mythicrpg.title.TitleManager;
import com.woxloi.mythicrpg.ui.ScoreboardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        // スコアボード初期化（データ読み込み前に構造だけ作る）
        ScoreboardManager.init(player);

        // プレイヤーデータ + 装備データを非同期ロード
        // （PlayerDataManagerが完了後にEquipmentManager.applyStats・ScoreboardManager.updateも実行する）
        PlayerDataManager.load(player);

        // ジョブ未選択なら選択GUIを開く（2tick後：データロード完了を待つ）
        MythicRPG.getInstance().getServer().getScheduler().runTaskLater(
                MythicRPG.getInstance(),
                () -> {
                    PlayerData data = PlayerDataManager.get(player);
                    if (data != null && !data.hasJob()) {
                        JobSelectGUI.open(player);
                    }
                },
                40L  // 2秒待つ（非同期DBロード完了を確実に待つため）
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        PlayerDataManager.save(player);
        ScoreboardManager.remove(player);
        TitleManager.unload(player.getUniqueId());
        ElementManager.clearPlayer(player.getUniqueId());
        PetManager.unloadPetData(player.getUniqueId());
    }
}
