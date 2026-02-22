package com.woxloi.mythicrpg.level;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.combat.CombatListener;
import com.woxloi.mythicrpg.job.JobManager;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import com.woxloi.mythicrpg.skill.SkillManager;
import com.woxloi.mythicrpg.stats.StatPointManager;
import com.woxloi.mythicrpg.title.TitleManager;
import com.woxloi.mythicrpg.ui.ScoreboardManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class LevelManager {

    public static double getRequiredExp(int level) {
        double base = MythicRPG.getInstance().getConfig().getDouble("level.base-exp", 100);
        double rate = MythicRPG.getInstance().getConfig().getDouble("level.exp-rate", 1.25);
        return base * Math.pow(level, rate);
    }

    public static void addExp(Player player, double amount) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        data.addExp(amount);

        while (data.getExp() >= getRequiredExp(data.getLevel())) {
            data.setExp(data.getExp() - getRequiredExp(data.getLevel()));
            levelUp(player, data);
        }

        ScoreboardManager.update(player);
    }

    private static void levelUp(Player player, PlayerData data) {
        data.setLevel(data.getLevel() + 1);

        // ジョブステータス再計算（MaxHP等が上がる）
        JobManager.onLevelUp(player);

        // スキルアンロック
        SkillManager.onLevelUp(player);

        // ステータスポイント付与
        StatPointManager.onLevelUp(player);

        // 称号チェック（levelベースの称号解放）
        TitleManager.checkUnlock(player);

        // Bukkit の max_health アトリビュートを更新
        CombatListener.applyMaxHealthAttribute(player, data);

        player.sendTitle("§6LEVEL UP!", "§eLv " + data.getLevel(), 10, 40, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        MythicRPG.msg(player, "§aレベルアップ！ §eLv " + data.getLevel());
    }
}
