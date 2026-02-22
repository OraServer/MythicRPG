package com.woxloi.mythicrpg.combat;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.level.LevelManager;
import com.woxloi.mythicrpg.pet.PetManager;
import com.woxloi.mythicrpg.title.TitleManager;
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

        int mobLevel = (int) event.getMob().getLevel();
        double multiplier = MythicRPG.getInstance()
                .getConfig().getDouble("mythicmobs.exp-multiplier", 1.0);
        double exp = mobLevel * multiplier * 10.0;

        LevelManager.addExp(killer, exp);
        TitleManager.incrementMobKill(killer);
        PetManager.addPetExp(killer.getUniqueId(), mobLevel * 3.0);
    }

    /* =====================
       通常Mob キル (MythicMob以外)
     ===================== */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onNormalMobDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        if (MythicBukkit.inst().getMobManager()
                .isActiveMob(event.getEntity().getUniqueId())) return;

        LevelManager.addExp(killer, 10.0);
        TitleManager.incrementMobKill(killer);
        PetManager.addPetExp(killer.getUniqueId(), 5.0);
    }
}
