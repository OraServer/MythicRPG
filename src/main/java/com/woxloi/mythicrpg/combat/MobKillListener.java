package com.woxloi.mythicrpg.combat;

import com.woxloi.mythicrpg.level.LevelManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobKillListener implements Listener {

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;

        double exp = 10; // 仮（STEP4でMobLv依存にする）
        LevelManager.addExp(killer, exp);
    }
}
