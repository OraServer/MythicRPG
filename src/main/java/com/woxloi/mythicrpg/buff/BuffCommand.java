package com.woxloi.mythicrpg.buff;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /mrpg buff <player> <type> <magnitude> <seconds>
 * /mrpg debuff <player> <type> <magnitude> <seconds>
 * /mrpg clearbuff <player>
 */
public class BuffCommand {

    public static boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c使い方: /mrpg buff <プレイヤー> <タイプ> [倍率] [秒数]");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("clearbuff")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) { sender.sendMessage("§cプレイヤーが見つかりません"); return true; }
            BuffManager.clearAll(target);
            sender.sendMessage("§a" + target.getName() + " のバフ/デバフをクリアしました");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage("§c使い方: /mrpg " + sub + " <プレイヤー> <タイプ> <倍率> <秒数>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { sender.sendMessage("§cプレイヤーが見つかりません"); return true; }

        BuffType type;
        try {
            type = BuffType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§c不明なバフタイプ: " + args[2]);
            StringBuilder sb = new StringBuilder("§7利用可能: ");
            for (BuffType t : BuffType.values()) sb.append(t.name()).append(" ");
            sender.sendMessage(sb.toString());
            return true;
        }

        double magnitude;
        int seconds;
        try {
            magnitude = Double.parseDouble(args[3]);
            seconds = args.length >= 5 ? Integer.parseInt(args[4]) : 30;
        } catch (NumberFormatException e) {
            sender.sendMessage("§c倍率・秒数は数値で指定してください");
            return true;
        }

        BuffManager.applyBuff(target, type, magnitude, seconds * 20, "admin");
        sender.sendMessage("§a" + target.getName() + " に " + type.getDisplayName() + " x" + magnitude + " を " + seconds + "秒付与しました");
        return true;
    }

    /** バフ一覧表示 */
    public static boolean showList(CommandSender sender, Player target) {
        var buffs = BuffManager.getBuffs(target);
        if (buffs.isEmpty()) {
            sender.sendMessage("§7" + target.getName() + " にアクティブなバフはありません");
            return true;
        }
        sender.sendMessage("§e§l" + target.getName() + " のバフ/デバフ:");
        for (var buff : buffs) {
            String color = buff.getType().isBuff() ? "§a" : "§c";
            sender.sendMessage(color + "  ■ " + buff.getType().getDisplayName()
                    + " §7×" + buff.getMagnitude() + " §7残り" + buff.getRemainingSeconds() + "秒");
        }
        return true;
    }
}
