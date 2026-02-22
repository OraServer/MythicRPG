package com.woxloi.mythicrpg.ui;

import com.woxloi.mythicrpg.equipment.EquipmentManager;
import com.woxloi.mythicrpg.equipment.model.EquipStats;
import com.woxloi.mythicrpg.level.LevelManager;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import com.woxloi.mythicrpg.title.TitleManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private static final Map<UUID, Scoreboard> boards = new HashMap<>();

    public static void init(Player player) {
        org.bukkit.scoreboard.ScoreboardManager mgr = Bukkit.getScoreboardManager();
        if (mgr == null) return;
        Scoreboard board = mgr.getNewScoreboard();
        Objective obj = board.registerNewObjective("mythicrpg", Criteria.DUMMY,
                net.kyori.adventure.text.Component.text("§6§lMythicRPG"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        boards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
    }

    public static void update(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        Scoreboard board = boards.get(player.getUniqueId());
        if (board == null) { init(player); board = boards.get(player.getUniqueId()); }
        if (board == null) return;

        Objective obj = board.getObjective("mythicrpg");
        if (obj == null) return;

        // ConcurrentModificationException 対策: コピーしてから削除
        new ArrayList<>(board.getEntries()).forEach(board::resetScores);

        // 装備ボーナス込みのステータス取得
        EquipStats equip = EquipmentManager.getTotalStats(player);
        double totalAtk  = data.getAttack() + equip.attack;
        double displayHp = data.getHp();
        double displayMaxHp = data.getMaxHp() + equip.maxHpBonus;
        double displayMp = data.getMp();
        double displayMaxMp = data.getMaxMp() + equip.maxMpBonus;

        // 称号タグ
        String titleTag = TitleManager.getDisplayTag(player.getUniqueId());

        int score = 12;
        if (titleTag != null && !titleTag.isBlank()) {
            obj.getScore(titleTag.trim()).setScore(score--);
        }

        String job = data.hasJob() ? data.getJob().getDisplayName() : "§8未選択";
        obj.getScore("§7ジョブ: §b" + job).setScore(score--);
        obj.getScore("§7Lv: §e" + data.getLevel()).setScore(score--);
        obj.getScore("§7EXP: §a" + (int) data.getExp()
                + "§8/§a" + (int) LevelManager.getRequiredExp(data.getLevel())).setScore(score--);
        obj.getScore("§8─────────").setScore(score--);
        // HP: PlayerDataのHPを表示（CombatListenerが同期済み）
        String hpColor = displayHp / displayMaxHp > 0.5 ? "§a" : displayHp / displayMaxHp > 0.25 ? "§e" : "§c";
        obj.getScore("§7HP: " + hpColor + (int) displayHp + "§8/§f" + (int) displayMaxHp).setScore(score--);
        obj.getScore("§7MP: §b" + (int) displayMp + "§8/§f" + (int) displayMaxMp).setScore(score--);
        obj.getScore("§7SP: §a" + (int) data.getSp() + "§8/§f" + (int)(data.getMaxSp() + equip.maxSpBonus)).setScore(score--);
        obj.getScore("§8─────────").setScore(score--);
        // ATKに装備ボーナスを含める
        obj.getScore("§7ATK: §c" + (int) totalAtk).setScore(score--);
        obj.getScore("§7DEF: §7" + (int) equip.defense).setScore(score);
    }

    public static void remove(Player player) {
        boards.remove(player.getUniqueId());
        org.bukkit.scoreboard.ScoreboardManager mgr = Bukkit.getScoreboardManager();
        if (mgr != null) player.setScoreboard(mgr.getMainScoreboard());
    }
}
