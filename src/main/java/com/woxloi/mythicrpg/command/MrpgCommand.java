package com.woxloi.mythicrpg.command;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.artifact.ArtifactCommand;
import com.woxloi.mythicrpg.buff.BuffCommand;
import com.woxloi.mythicrpg.combo.ComboManager;
import com.woxloi.mythicrpg.dungeon.DungeonGUI;
import com.woxloi.mythicrpg.dungeon.DungeonManager;
import com.woxloi.mythicrpg.element.ElementResistanceGUI;
import com.woxloi.mythicrpg.job.JobSelectGUI;
import com.woxloi.mythicrpg.party.PartyCommand;
import com.woxloi.mythicrpg.pet.PetManager;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import com.woxloi.mythicrpg.pvp.PvpRankingManager;
import com.woxloi.mythicrpg.skill.SkillManager;
import com.woxloi.mythicrpg.skill.loader.SkillLoader;
import com.woxloi.mythicrpg.stats.StatGUI;
import com.woxloi.mythicrpg.title.TitleGUI;
import com.woxloi.mythicrpg.title.TitleManager;
import com.woxloi.mythicrpg.ui.ProfileGUI;
import com.woxloi.mythicrpg.ui.skill.SkillGUI;
import com.woxloi.mythicrpg.ui.stats.StatDetailGUI;
import com.woxloi.mythicrpg.ui.title.TitleDetailGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class MrpgCommand implements CommandExecutor, TabCompleter {

    private final ArtifactCommand artifactCmd = new ArtifactCommand();
    private final BuffCommand     buffCmd     = new BuffCommand();
    private final PartyCommand    partyCmd    = new PartyCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { if (sender instanceof Player p) sendHelp(p); return true; }

        switch (args[0].toLowerCase()) {
            case "skill" -> {
                if (!(sender instanceof Player player)) return true;
                if (args.length < 2) { MythicRPG.playerPrefixMsg(player, "§c使い方: /mrpg skill <id>"); return true; }
                SkillManager.useSkill(player, args[1]);
            }
            case "skills" -> {
                if (!(sender instanceof Player player)) return true;
                PlayerData data = PlayerDataManager.get(player);
                if (data == null || !data.hasJob()) { MythicRPG.playerPrefixMsg(player, "§cジョブを先に選択してください"); return true; }
                SkillGUI.open(player, data);
            }
            case "job" -> {
                if (!(sender instanceof Player player)) return true;
                PlayerData data = PlayerDataManager.get(player);
                if (data != null && data.hasJob()) { MythicRPG.playerPrefixMsg(player, "§cすでにジョブが設定されています: §e" + data.getJob().getDisplayName()); return true; }
                JobSelectGUI.open(player);
            }
            case "stats" -> {
                if (!(sender instanceof Player player)) return true;
                PlayerData data = PlayerDataManager.get(player);
                if (data == null) return true;
                if (args.length >= 2 && args[1].equalsIgnoreCase("gui")) {
                    StatGUI.open(player);
                } else {
                    MythicRPG.playerPrefixMsg(player, "§7Lv: §e" + data.getLevel()
                            + "  §7HP: §c" + (int)data.getHp() + "/" + (int)data.getMaxHp()
                            + "  §7MP: §b" + (int)data.getMp() + "/" + (int)data.getMaxMp()
                            + "  §7ATK: §6" + (int)data.getAttack());
                }
            }
            case "buff" -> {
                if (!(sender instanceof Player player)) return true;
                buffCmd.execute(player, args);
            }
            case "title" -> {
                if (!(sender instanceof Player player)) return true;
                TitleManager.checkUnlock(player);
                TitleGUI.open(player);
            }
            case "combo" -> {
                if (!(sender instanceof Player player)) return true;
                int maxCombo = ComboManager.getMaxCombo(player);
                int current  = ComboManager.getComboCount(player);
                double mult  = ComboManager.getDamageMultiplier(player);
                MythicRPG.playerPrefixMsg(player, "§e§lコンボ §7現在: §f" + current
                        + "  §7倍率: §6\u00d7" + String.format("%.1f", mult)
                        + "  §7最大: §e" + maxCombo);
            }
            case "party" -> {
                if (!(sender instanceof Player player)) return true;
                partyCmd.execute(player, args);
            }
            case "artifact" -> {
                if (!(sender instanceof Player player)) return true;
                artifactCmd.execute(player, args);
            }
            case "profile" -> {
                if (!(sender instanceof Player player)) return true;
                ProfileGUI.open(player);
            }
            case "dungeon" -> {
                if (!(sender instanceof Player player)) return true;
                if (args.length >= 2 && args[1].equalsIgnoreCase("leave")) {
                    DungeonManager.leave(player);
                } else {
                    DungeonGUI.open(player);
                }
            }
            case "pet" -> {
                if (!(sender instanceof Player player)) return true;
                if (args.length >= 2) {
                    switch (args[1].toLowerCase()) {
                        case "summon" -> {
                            if (args.length < 3) { MythicRPG.playerPrefixMsg(player, "§c使い方: /mrpg pet summon <id>"); return true; }
                            String err = PetManager.summon(player, args[2]);
                            if (err != null) MythicRPG.playerPrefixMsg(player, "§c" + err);
                        }
                        case "dismiss" -> PetManager.dismiss(player);
                        case "info" -> {
                            var petData = PetManager.getPetData(player.getUniqueId());
                            if (petData == null) { MythicRPG.playerPrefixMsg(player, "§7ペットがいません"); return true; }
                            var def = PetManager.getDefinition(petData.getPetDefinitionId());
                            MythicRPG.playerPrefixMsg(player, def != null ? def.getDisplayName() + " §7" + petData.getExpDisplay() : "§c不明なペット");
                        }
                        default -> sendPetHelp(player);
                    }
                } else {
                    sendPetHelp(player);
                }
            }
            case "pvp" -> {
                if (!(sender instanceof Player player)) return true;
                PvpRankingManager.sendRanking(player);
            }
            case "element" -> {
                if (!(sender instanceof Player player)) return true;
                ElementResistanceGUI.open(player);
            }
            case "statdetail" -> {
                if (!(sender instanceof Player player)) return true;
                StatDetailGUI.open(player);
            }
            case "titlebook" -> {
                if (!(sender instanceof Player player)) return true;
                TitleDetailGUI.open(player);
            }
            case "reload" -> {
                if (!sender.hasPermission("mythicrpg.admin")) { sender.sendMessage(MythicRPG.PREFIX + "§c権限がありません"); return true; }
                MythicRPG.getInstance().reloadConfig();
                SkillLoader.load();
                sender.sendMessage(MythicRPG.PREFIX + "§aリロードしました");
            }
            default -> { if (sender instanceof Player p) sendHelp(p); }
        }
        return true;
    }

    private void sendHelp(Player player) {
        String help = "§e--- MythicRPG コマンド一覧 ---\n"
                + "§7・/skill <id> - スキルを使用\n"
                + "§7・/skills - スキル一覧を表示\n"
                + "§7・/job - 職業情報を表示\n"
                + "§7・/stats [gui] - ステータスを確認\n"
                + "§7・/buff - バフの確認\n"
                + "§7・/title - タイトルの確認・変更\n"
                + "§7・/titlebook - タイトルブックを開く\n"
                + "§7・/combo - コンボ情報を表示\n"
                + "§7・/party - パーティー管理\n"
                + "§7・/artifact - アーティファクト管理\n"
                + "§7・/profile - プロフィールを表示\n"
                + "§7・/dungeon [leave] - ダンジョン関連コマンド\n"
                + "§7・/pet [summon/dismiss/info] - ペット管理\n"
                + "§7・/pvp - PvPモード切り替え\n"
                + "§7・/element - 属性情報を表示\n"
                + "§7・/statdetail - 詳細ステータス表示";
        if (player.hasPermission("mythicrpg.admin")) {
            help += "\n§c・/reload - プラグインを再読み込み（管理者専用）";
        }
        MythicRPG.playerPrefixMsg(player, help);
    }

    private void sendPetHelp(Player player) {
        MythicRPG.playerPrefixMsg(player, "§7/mrpg pet summon <id> / dismiss / info");
        MythicRPG.playerPrefixMsg(player, "§7使用可能なペットID: §e"
                + String.join(", ", PetManager.getAllDefinitions().stream()
                        .map(d -> d.getId()).toList()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> cmds = new ArrayList<>(List.of(
                "skill","skills","job","stats","buff","title","titlebook",
                "combo","party","artifact","profile","dungeon","pet","pvp","element","statdetail"
            ));
            if (sender.hasPermission("mythicrpg.admin")) cmds.add("reload");
            return cmds.stream().filter(c -> c.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) return switch (args[0].toLowerCase()) {
            case "stats"    -> List.of("gui");
            case "artifact" -> List.of("gui","info","check","list","give");
            case "buff"     -> List.of("list","add","clear");
            case "party"    -> List.of("create","invite","join","leave","disband","info");
            case "dungeon"  -> List.of("leave");
            case "pet"      -> List.of("summon","dismiss","info");
            default -> List.of();
        };
        if (args.length == 3 && args[0].equalsIgnoreCase("pet") && args[1].equalsIgnoreCase("summon")) {
            return PetManager.getAllDefinitions().stream()
                    .map(d -> d.getId())
                    .filter(id -> id.startsWith(args[2].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
