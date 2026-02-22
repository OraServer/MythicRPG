package com.woxloi.mythicrpg.equipment;

import com.woxloi.mythicrpg.equipment.enhancer.EnhanceGUI;
import com.woxloi.mythicrpg.equipment.gui.EquipmentGUI;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.model.RpgItemSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /equip コマンド群。
 *
 * /equip gui          - 装備管理GUIを開く
 * /equip enhance      - 強化GUIを開く
 * /equip give <id>    - [admin] 装備アイテムを付与
 * /equip list         - 登録されている装備一覧
 * /equip info         - 手持ちアイテムの詳細
 * /equip stats        - 現在の装備合計ステータス
 */
public class EquipCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cプレイヤーのみ実行可能です");
            return true;
        }

        String sub = args.length > 0 ? args[0].toLowerCase() : "gui";

        switch (sub) {
            case "gui" -> EquipmentGUI.open(player);

            case "enhance", "enh" -> EnhanceGUI.open(player);

            case "give" -> {
                if (!player.hasPermission("mythicrpg.admin")) {
                    player.sendMessage("§c権限がありません");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§c使用法: /equip give <アイテムID>");
                    return true;
                }
                String id = args[1];
                RpgItem item = EquipmentRegistry.get(id);
                if (item == null) {
                    player.sendMessage("§cアイテムが見つかりません: " + id);
                    return true;
                }
                player.getInventory().addItem(RpgItemSerializer.toItemStack(item));
                player.sendMessage("§a" + item.displayName + "§aを付与しました");
            }

            case "list" -> {
                player.sendMessage("§6§l=== 登録装備一覧 ===");
                for (RpgItem item : EquipmentRegistry.all()) {
                    player.sendMessage("§7" + item.id + " §f- " + item.rarity.color + item.displayName
                            + " §8[" + item.slot.displayName + "] Lv." + item.requiredLevel);
                }
            }

            case "info" -> {
                var hand = player.getInventory().getItemInMainHand();
                RpgItem item = RpgItemSerializer.fromItemStack(hand);
                if (item == null) {
                    player.sendMessage("§cRPGアイテムを手に持ってください");
                    return true;
                }
                var stats = item.getEffectiveStats();
                player.sendMessage("§b§l=== " + item.displayName + " §b§l===");
                player.sendMessage("§7ID: §f" + item.id);
                player.sendMessage("§7レアリティ: " + item.rarity.displayName);
                player.sendMessage("§7スロット: §f" + item.slot.displayName);
                player.sendMessage("§7強化: §e+" + item.enhanceLevel + "§7/§e+" + item.maxEnhance);
                player.sendMessage("§7必要Lv: §f" + item.requiredLevel);
                if (stats.attack > 0)    player.sendMessage("§c攻撃力: §f+" + (int) stats.attack);
                if (stats.defense > 0)   player.sendMessage("§7防御力: §f+" + (int) stats.defense);
                if (stats.maxHpBonus > 0) player.sendMessage("§a最大HP: §f+" + (int) stats.maxHpBonus);
                if (stats.magicPower > 0) player.sendMessage("§d魔力: §f+" + (int) stats.magicPower);
            }

            case "stats" -> {
                var stats = EquipmentManager.getTotalStats(player);
                player.sendMessage("§b§l=== 装備合計ステータス ===");
                player.sendMessage("§c攻撃力: §f+" + (int) stats.attack);
                player.sendMessage("§7防御力: §f+" + (int) stats.defense);
                player.sendMessage("§a最大HP: §f+" + (int) stats.maxHpBonus);
                player.sendMessage("§bMP上限: §f+" + (int) stats.maxMpBonus);
                player.sendMessage("§6SP上限: §f+" + (int) stats.maxSpBonus);
                player.sendMessage(String.format("§e会心率: §f+%.1f%%", stats.critRate * 100));
                player.sendMessage(String.format("§e会心倍率: §f+%.0f%%", stats.critDamage * 100));
                player.sendMessage("§d魔力: §f+" + (int) stats.magicPower);
            }

            default -> player.sendMessage("§c不明なサブコマンド。/equip gui | enhance | give | list | info | stats");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("gui", "enhance", "give", "list", "info", "stats");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> ids = new ArrayList<>();
            for (RpgItem item : EquipmentRegistry.all()) ids.add(item.id);
            return ids;
        }
        return List.of();
    }
}
