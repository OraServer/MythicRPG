package com.woxloi.mythicrpg.artifact;

import com.woxloi.mythicrpg.MythicRPG;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * /mrpg artifact <サブコマンド>
 *
 * サブコマンド一覧:
 *   gui         - アーティファクトセット確認GUIを開く
 *   info        - テキストでセット情報を表示
 *   give <id>   - 管理者専用: アーティファクトアイテムを付与
 *   list        - 全アーティファクトID一覧
 *   check       - 現在の装備セット状況を確認
 */
public class ArtifactCommand {

    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MythicRPG.PREFIX + "§cプレイヤーのみ実行可能です");
            return;
        }

        String sub = args.length >= 2 ? args[1].toLowerCase() : "gui";

        switch (sub) {

            /* ──────────────── gui ──────────────── */
            case "gui" -> ArtifactGUI.open(player);

            /* ──────────────── info ──────────────── */
            case "info" -> showInfo(player);

            /* ──────────────── check ──────────────── */
            case "check" -> showCheck(player);

            /* ──────────────── list ──────────────── */
            case "list" -> {
                player.sendMessage("§5§l--- アーティファクトID一覧 ---");
                for (ArtifactPiece p : ArtifactRegistry.all()) {
                    player.sendMessage("§7" + p.getPieceId()
                            + " §8→ §f" + p.getPieceName()
                            + " §8[" + p.getSetType().getDisplayName() + "§8]");
                }
            }

            /* ──────────────── give ──────────────── */
            case "give" -> {
                if (!player.hasPermission("mythicrpg.admin")) {
                    player.sendMessage(MythicRPG.PREFIX + "§c権限がありません");
                    return;
                }
                if (args.length < 3) {
                    player.sendMessage(MythicRPG.PREFIX + "§c使い方: /mrpg artifact give <pieceId> [playerName]");
                    return;
                }
                String pieceId = args[2];
                ArtifactPiece piece = ArtifactRegistry.get(pieceId);
                if (piece == null) {
                    player.sendMessage(MythicRPG.PREFIX + "§c不明なpieceId: " + pieceId);
                    return;
                }

                Player target = args.length >= 4
                        ? Bukkit.getPlayer(args[3])
                        : player;

                if (target == null) {
                    player.sendMessage(MythicRPG.PREFIX + "§cプレイヤーが見つかりません");
                    return;
                }

                ItemStack item = buildArtifactItem(piece);
                target.getInventory().addItem(item);
                player.sendMessage(MythicRPG.PREFIX
                        + "§a" + piece.getPieceName() + "§f を §e" + target.getName() + "§f に付与しました");
                if (target != player) {
                    target.sendMessage(MythicRPG.PREFIX
                            + "§5§l[アーティファクト] §f" + piece.getPieceName() + " を受け取りました！");
                }
            }

            default -> showHelp(player);
        }
    }

    /** テキストでセット情報を表示 */
    private void showInfo(Player player) {
        player.sendMessage("§5§l===== アーティファクトセット一覧 =====");
        for (ArtifactType type : ArtifactType.values()) {
            player.sendMessage("");
            player.sendMessage(type.getDisplayName() + " §7(" + ArtifactRegistry.ofSet(type).size() + "ピース)");
            player.sendMessage("§7" + type.getDescription());
            int[]    reqs  = type.getPiecesRequired();
            String[] descs = type.getBonusDescriptions();
            for (int i = 0; i < reqs.length; i++) {
                player.sendMessage("  §e" + reqs[i] + "セット: §f" + descs[i]);
            }
        }
    }

    /** 現在の装備状況を表示 */
    private void showCheck(Player player) {
        var counts = ArtifactManager.countEquipped(player);
        player.sendMessage("§5§l--- 装備中アーティファクト ---");
        boolean any = false;
        for (var e : counts.entrySet()) {
            ArtifactType type = e.getKey();
            int cnt  = e.getValue();
            int tier = type.getActiveTier(cnt);
            player.sendMessage("§f" + type.getDisplayName()
                    + " §7: " + cnt + "ピース"
                    + (tier > 0 ? " §a[" + tier + "段階発動中]" : " §8[未発動]"));
            any = true;
        }
        if (!any) player.sendMessage("§7アーティファクトを装備していません");
    }

    /** アーティファクトアイテムを生成 */
    private ItemStack buildArtifactItem(ArtifactPiece piece) {
        // スロットに応じてマテリアルを決定
        org.bukkit.Material mat = switch (piece.getSlot()) {
            case HELMET     -> org.bukkit.Material.DIAMOND_HELMET;
            case CHESTPLATE -> org.bukkit.Material.DIAMOND_CHESTPLATE;
            case LEGGINGS   -> org.bukkit.Material.DIAMOND_LEGGINGS;
            case BOOTS      -> org.bukkit.Material.DIAMOND_BOOTS;
            case MAIN_HAND  -> org.bukkit.Material.DIAMOND_SWORD;
            case OFF_HAND   -> org.bukkit.Material.SHIELD;
            default         -> org.bukkit.Material.DIAMOND;
        };

        ItemStack item = new ItemStack(mat);
        var meta = item.getItemMeta();
        meta.displayName(net.kyori.adventure.text.Component.text(piece.getPieceName()));

        java.util.List<net.kyori.adventure.text.Component> lore = new java.util.ArrayList<>();
        lore.add(net.kyori.adventure.text.Component.text("§5" + piece.getSetType().getDisplayName()));
        lore.add(net.kyori.adventure.text.Component.text("§7スロット: " + piece.getSlot().getDisplayName()));
        lore.add(net.kyori.adventure.text.Component.text(""));
        if (piece.getBonusAtk() > 0) lore.add(net.kyori.adventure.text.Component.text("§cATK §f+" + piece.getBonusAtk()));
        if (piece.getBonusDef() > 0) lore.add(net.kyori.adventure.text.Component.text("§7DEF §f+" + piece.getBonusDef()));
        if (piece.getBonusHp()  > 0) lore.add(net.kyori.adventure.text.Component.text("§aHP  §f+" + piece.getBonusHp()));
        if (piece.getBonusMp()  > 0) lore.add(net.kyori.adventure.text.Component.text("§bMP  §f+" + piece.getBonusMp()));
        meta.lore(lore);

        item.setItemMeta(meta);

        // アーティファクトIDをNBTに焼き込む
        return ArtifactManager.tagItem(item, piece.getPieceId());
    }

    private void showHelp(Player player) {
        player.sendMessage("§5§l--- アーティファクトコマンド ---");
        player.sendMessage("§7/mrpg artifact gui     §f- セット確認GUIを開く");
        player.sendMessage("§7/mrpg artifact info    §f- セット一覧をテキスト表示");
        player.sendMessage("§7/mrpg artifact check   §f- 現在の装備状況確認");
        player.sendMessage("§7/mrpg artifact list    §f- 全ピースID一覧");
        if (player.hasPermission("mythicrpg.admin")) {
            player.sendMessage("§7/mrpg artifact give <id> [player] §f- ピース付与 §c[管理者]");
        }
    }
}
