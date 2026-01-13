package com.woxloi.mythicrpg.ui;

import com.woxloi.mythicrpg.level.LevelManager;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private static final Map<UUID, Scoreboard> boards = new HashMap<>();

    public static void init(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective(
                "mythicrpg", "dummy", "§6§lMythicRPG"
        );
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        boards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
    }

    public static void update(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        Scoreboard board = boards.get(player.getUniqueId());
        if (board == null) {
            init(player);
            board = boards.get(player.getUniqueId());
        }

        Objective obj = board.getObjective("mythicrpg");
        if (obj == null) return;

        // 全行クリア
        board.getEntries().forEach(board::resetScores);

        int score = 15;

        obj.getScore("§7Job: §b" +
                (data.hasJob() ? data.getJob().getDisplayName() : "未選択")
        ).setScore(score--);

        obj.getScore("§7Level: §e" + data.getLevel())
                .setScore(score--);

        obj.getScore("§7EXP: §a" +
                (int) data.getExp() + "/" +
                (int) LevelManager.getRequiredExp(data.getLevel())
        ).setScore(score--);

        // 空行①
        obj.getScore("§8 ").setScore(score--);

        obj.getScore("§7HP: §c" + (int) data.getMaxHp())
                .setScore(score--);

        obj.getScore("§7ATK: §6" + (int) data.getAttack())
                .setScore(score--);

        // 空行②（色コード違い）
        obj.getScore("§8  ").setScore(score--);

        obj.getScore("§7MP: §b" + (int) data.getMp())
                .setScore(score--);

        obj.getScore("§7SP: §a" + (int) data.getSp())
                .setScore(score--);
    }

    public static void remove(Player player) {
        boards.remove(player.getUniqueId());
        player.setScoreboard(
                Bukkit.getScoreboardManager().getMainScoreboard()
        );
    }
}
