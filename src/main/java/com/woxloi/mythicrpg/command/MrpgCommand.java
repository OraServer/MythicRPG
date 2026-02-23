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
import java.util.Map;

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
                String[] subArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
                BuffCommand.execute(player, subArgs);
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
                com.woxloi.mythicrpg.artifact.ArtifactRegistry.load();
                sender.sendMessage(MythicRPG.PREFIX + "§aリロードしました (config / skills / artifacts)");
            }
            case "toggle" -> {
                if (!sender.hasPermission("mythicrpg.admin")) { sender.sendMessage(MythicRPG.PREFIX + "§c権限がありません"); return true; }
                if (args.length < 2) {
                    sendToggleStatus(sender);
                    return true;
                }
                String sys = args[1].toLowerCase();
                if (!com.woxloi.mythicrpg.core.PluginToggleManager.validSystems().contains(sys)) {
                    sender.sendMessage(MythicRPG.PREFIX + "§c不明なシステム: §e" + sys);
                    sender.sendMessage(MythicRPG.PREFIX + "§7有効なシステム: §f" 
                        + String.join(", ", com.woxloi.mythicrpg.core.PluginToggleManager.validSystems()));
                    return true;
                }
                boolean newState = com.woxloi.mythicrpg.core.PluginToggleManager.toggle(sys);
                sender.sendMessage(MythicRPG.PREFIX + "§e" + sys + " §7→ "
                    + (newState ? "§aON" : "§cOFF"));
            }
            case "pvpzone" -> {
                if (!(sender instanceof Player player)) return true;
                if (!player.hasPermission("mythicrpg.admin")) { MythicRPG.playerPrefixMsg(player, "§c権限がありません"); return true; }
                if (args.length < 2) {
                    MythicRPG.playerPrefixMsg(player, "§7/mrpg pvpzone create <id> <range> §8- 現在位置を中心に±range");
                    return true;
                }
                if (args[1].equalsIgnoreCase("create")) {
                    if (args.length < 4) { MythicRPG.playerPrefixMsg(player, "§c使い方: /mrpg pvpzone create <id> <range>"); return true; }
                    String zoneId = args[2];
                    double range;
                    try { range = Double.parseDouble(args[3]); } catch (NumberFormatException ex) { MythicRPG.playerPrefixMsg(player, "§crangeは数値で指定してください"); return true; }
                    var loc = player.getLocation();
                    var corner1 = loc.clone().add(-range, -range, -range);
                    var corner2 = loc.clone().add(range, range, range);
                    com.woxloi.mythicrpg.pvp.PvpZoneManager.registerZone(
                        new com.woxloi.mythicrpg.pvp.PvpZoneManager.PvpZone(
                            zoneId, "PvPゾーン[" + zoneId + "]", corner1, corner2));
                    MythicRPG.playerPrefixMsg(player, "§aPvPゾーン §e" + zoneId + " §aを半径 §e" + (int)range + " §aで登録しました");
                }
            }
            default -> { if (sender instanceof Player p) sendHelp(p); }
        }
        return true;
    }

    private void sendToggleStatus(org.bukkit.command.CommandSender sender) {
        sender.sendMessage("§e§l--- MythicRPG システム状態 ---");
        com.woxloi.mythicrpg.core.PluginToggleManager.allStates()
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> sender.sendMessage(
                String.format("§7%-12s §8: %s", e.getKey(),
                    e.getValue() ? "§aON" : "§cOFF")));
        sender.sendMessage("§7/mrpg toggle <system> でON/OFFを切替");
    }

    private void sendHelp(Player player) {

        MythicRPG.playerPrefixMsg(player, "§6§l━━━ MythicRPG コマンド ━━━");

        // =========================
        // 基本
        // =========================
        MythicRPG.playerPrefixMsg(player, "§e▶ 基本");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg skill <id> §8- スキル使用");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg skills §8- スキル一覧GUI");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg job §8- ジョブ選択");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg stats [gui] §8- ステータス表示");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg statdetail §8- 詳細ステータス");

        // =========================
        // システム
        // =========================
        MythicRPG.playerPrefixMsg(player, "§e▶ システム");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg buff §8- バフ確認");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg title §8- 称号設定");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg titlebook §8- 称号図鑑");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg combo §8- コンボ情報");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg profile §8- プロフィール");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg element §8- 属性耐性");

        // =========================
        // コンテンツ
        // =========================
        MythicRPG.playerPrefixMsg(player, "§e▶ コンテンツ");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg dungeon [leave] §8- ダンジョン");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg party ... §8- パーティ");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg pet [summon/dismiss/info] §8- ペット");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg artifact §8- アーティファクト");
        MythicRPG.playerPrefixMsg(player, " §7/mrpg pvp §8- PvPランキング");

        // =========================
        // 管理者
        // =========================
        if (player.hasPermission("mythicrpg.admin")) {
            MythicRPG.playerPrefixMsg(player, "§c▶ 管理者");
            MythicRPG.playerPrefixMsg(player, " §7/mrpg reload §8- 設定リロード");
            MythicRPG.playerPrefixMsg(player, " §7/mrpg toggle <system> §8- システム切替");
            MythicRPG.playerPrefixMsg(player, " §7/mrpg pvpzone create <id> <range> §8- PvPゾーン作成");
        }
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
            if (sender.hasPermission("mythicrpg.admin")) { cmds.add("reload"); cmds.add("pvpzone"); cmds.add("toggle"); }
            return cmds.stream().filter(c -> c.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) return switch (args[0].toLowerCase()) {
            case "stats"    -> List.of("gui");
            case "artifact" -> List.of("gui","info","check","list","give");
            case "buff"     -> List.of("list","add","clear");
            case "party"    -> List.of("create","invite","join","leave","disband","info");
            case "dungeon"  -> List.of("leave");
            case "pet"      -> List.of("summon","dismiss","info");
            case "pvpzone"  -> List.of("create");
            case "toggle"   -> new ArrayList<>(com.woxloi.mythicrpg.core.PluginToggleManager.validSystems());
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
