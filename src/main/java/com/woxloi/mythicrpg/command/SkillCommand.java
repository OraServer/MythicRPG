package com.woxloi.mythicrpg.command;

import com.woxloi.mythicrpg.skill.SkillManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) {
            player.sendMessage("§c/mrpg skill <id>");
            return true;
        }

        SkillManager.useSkill(player, args[0]);
        return true;
    }
}
