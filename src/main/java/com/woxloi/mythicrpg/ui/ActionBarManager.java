package com.woxloi.mythicrpg.ui;

import com.woxloi.mythicrpg.level.LevelManager;
import com.woxloi.mythicrpg.player.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class ActionBarManager {

    public static void update(Player player, PlayerData data) {
        String msg =
                "§c❤ " + (int) data.getHp() + "/" + (int) data.getMaxHp() +
                "  §b✦MP " + (int) data.getMp() + "/" + (int) data.getMaxMp() +
                "  §a✦SP " + (int) data.getSp() + "/" + (int) data.getMaxSp() +
                "  §e✦EXP " + (int) data.getExp() + "/" +
                        (int) LevelManager.getRequiredExp(data.getLevel());

        player.sendActionBar(Component.text(msg));
    }
}
