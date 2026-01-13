package com.woxloi.mythicrpg.ui;

import com.woxloi.mythicrpg.level.LevelManager;
import com.woxloi.mythicrpg.player.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class ActionBarManager {

    public static void update(Player player, PlayerData data) {

        int hp = (int) data.getHp();
        int maxHp = (int) data.getMaxHp();

        int mp = (int) data.getMp();
        int maxMp = (int) data.getMaxMp();

        int sp = (int) data.getSp();
        int maxSp = (int) data.getMaxSp();

        int exp = (int) data.getExp();
        int need = (int) LevelManager.getRequiredExp(data.getLevel());

        String msg =
                "§c❤ " + hp + "/" + maxHp +
                        "  §b✦MP " + mp + "/" + maxMp +
                        "  §a✦SP " + sp + "/" + maxSp +
                        "  §e✦EXP " + exp + "/" + need;

        player.sendActionBar(Component.text(msg));
    }
}
