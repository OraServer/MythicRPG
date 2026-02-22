package com.woxloi.mythicrpg.combat;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.level.LevelManager;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobKillListener implements Listener {

    /* =====================
       MythicMob キル
     ===================== */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        if (!(event.getKiller() instanceof Player killer)) return;

        // MythicMobのレベルを取得
        int mobLevel = (int) event.getMob().getLevel();
        double multiplier = MythicRPG.getInstance()
                .getConfig().getDouble("mythicmobs.exp-multiplier", 1.0);

        // EXP = MobLv × 倍率 × 10
        double exp = mobLevel * multiplier * 10.0;

        LevelManager.addExp(killer, exp);
    }

    /* =====================
       通常Mob キル (MythicMob以外)
     ===================== */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onNormalMobDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        // MythicMobなら上のイベントで処理するのでスキップ
        if (MythicBukkit.inst().getMobManager()
                .isActiveMob(event.getEntity().getUniqueId())) return;

        // 通常Mobは固定EXP
        LevelManager.addExp(killer, 10.0);
    }
}
