package com.woxloi.mythicrpg.job;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.entity.Player;

public class JobManager {

    public static void setJob(Player player, JobType job) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null || data.hasJob()) return;

        data.setJob(job);
        recalcStats(data, true);
        unlockInitialSkills(data);
    }

    public static void onLevelUp(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null || !data.hasJob()) return;

        recalcStats(data, false);
    }

    private static void recalcStats(PlayerData data, boolean firstTime) {
        String path = "jobs." + data.getJob().name().toLowerCase();
        int level = data.getLevel();

        double hpRate = firstTime ? 1 : data.getHp() / Math.max(1, data.getMaxHp());
        double mpRate = firstTime ? 1 : data.getMp() / Math.max(1, data.getMaxMp());
        double spRate = firstTime ? 1 : data.getSp() / Math.max(1, data.getMaxSp());

        double baseHp  = cfg(path + ".base-hp");
        double baseAtk = cfg(path + ".base-atk");
        double baseMp  = cfg(path + ".base-mp");
        double baseSp  = cfg(path + ".base-sp");

        double growHp  = cfg(path + ".grow-hp");
        double growAtk = cfg(path + ".grow-atk");
        double growMp  = cfg(path + ".grow-mp");
        double growSp  = cfg(path + ".grow-sp");

        data.setMaxHp(baseHp  + (level - 1) * growHp);
        data.setAttack(baseAtk + (level - 1) * growAtk);
        data.setMaxMp(baseMp  + (level - 1) * growMp);
        data.setMaxSp(baseSp  + (level - 1) * growSp);

        data.setHp(data.getMaxHp() * hpRate);
        data.setMp(data.getMaxMp() * mpRate);
        data.setSp(data.getMaxSp() * spRate);
    }

    private static double cfg(String path) {
        return MythicRPG.getInstance().getConfig().getDouble(path, 0);
    }

    private static void unlockInitialSkills(PlayerData data) {
        data.unlockSkill("basic_attack");
    }
}
