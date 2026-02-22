package com.woxloi.mythicrpg.party;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /mrpg party <sub> コマンドハンドラー
 *
 * create          - パーティー作成
 * invite <player> - 招待 (別途招待承諾は /mrpg party join <leader>)
 * join <leader>   - 参加
 * leave           - 離脱
 * disband         - 解散 (リーダーのみ)
 * info            - パーティー情報
 */
public class PartyCommand {

    public static boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cプレイヤーのみ実行可能です");
            return true;
        }

        if (args.length < 2) {
            showInfo(player);
            return true;
        }

        String sub = args[1].toLowerCase();
        return switch (sub) {
            case "create"  -> create(player);
            case "invite"  -> invite(player, args);
            case "join"    -> join(player, args);
            case "leave"   -> leave(player);
            case "disband" -> disband(player);
            case "info"    -> showInfo(player);
            default -> {
                player.sendMessage("§c不明なサブコマンド: " + sub);
                yield true;
            }
        };
    }

    private static boolean create(Player player) {
        if (RpgPartyManager.isInParty(player.getUniqueId())) {
            player.sendMessage("§cすでにパーティーに所属しています");
            return true;
        }
        Party party = RpgPartyManager.create(player);
        if (party == null) { player.sendMessage("§cパーティー作成に失敗しました"); return true; }
        player.sendMessage("§aパーティーを作成しました！ ID: §f" + party.getPartyId().toString().substring(0, 8));
        player.sendMessage("§7/mrpg party invite <プレイヤー名> で招待できます");
        return true;
    }

    private static boolean invite(Player player, String[] args) {
        if (args.length < 3) { player.sendMessage("§c使い方: /mrpg party invite <プレイヤー名>"); return true; }
        Party party = RpgPartyManager.getParty(player.getUniqueId());
        if (party == null) { player.sendMessage("§cパーティーに所属していません"); return true; }
        if (!party.isLeader(player.getUniqueId())) { player.sendMessage("§cリーダーのみ招待できます"); return true; }
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) { player.sendMessage("§cプレイヤーが見つかりません"); return true; }
        target.sendMessage("§b" + player.getName() + " §fからパーティーに招待されました");
        target.sendMessage("§7/mrpg party join " + player.getName() + " で参加できます");
        player.sendMessage("§a" + target.getName() + " を招待しました");
        return true;
    }

    private static boolean join(Player player, String[] args) {
        if (args.length < 3) { player.sendMessage("§c使い方: /mrpg party join <リーダー名>"); return true; }
        Player leader = Bukkit.getPlayer(args[2]);
        if (leader == null) { player.sendMessage("§cプレイヤーが見つかりません"); return true; }
        Party party = RpgPartyManager.getParty(leader.getUniqueId());
        if (party == null) { player.sendMessage("§c" + leader.getName() + " はパーティーに所属していません"); return true; }
        boolean ok = RpgPartyManager.join(player, party.getPartyId());
        if (!ok) player.sendMessage("§c参加できませんでした（満員か既に参加済み）");
        return true;
    }

    private static boolean leave(Player player) {
        if (!RpgPartyManager.isInParty(player.getUniqueId())) {
            player.sendMessage("§cパーティーに所属していません"); return true;
        }
        RpgPartyManager.leave(player);
        player.sendMessage("§eパーティーを離脱しました");
        return true;
    }

    private static boolean disband(Player player) {
        Party party = RpgPartyManager.getParty(player.getUniqueId());
        if (party == null) { player.sendMessage("§cパーティーに所属していません"); return true; }
        if (!party.isLeader(player.getUniqueId())) { player.sendMessage("§cリーダーのみ解散できます"); return true; }
        RpgPartyManager.disband(party.getPartyId());
        player.sendMessage("§eパーティーを解散しました");
        return true;
    }

    private static boolean showInfo(Player player) {
        Party party = RpgPartyManager.getParty(player.getUniqueId());
        if (party == null) { player.sendMessage("§7パーティーに所属していません"); return true; }
        player.sendMessage("§b§l━━━ パーティー情報 ━━━");
        for (PartyMember m : party.getMembers()) {
            String role = m.isLeader() ? "§6[L]" : "§7[M]";
            player.sendMessage(role + " §f" + m.getName());
        }
        player.sendMessage("§7メンバー: §f" + party.getSize() + "/4");
        player.sendMessage("§b§l━━━━━━━━━━━━━━━━");
        return true;
    }
}
