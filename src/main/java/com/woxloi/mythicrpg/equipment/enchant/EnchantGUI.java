package com.woxloi.mythicrpg.equipment.enchant;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * エンチャント付与台GUI（54スロット）。
 * 左側にエンチャント種別選択、右側にランク選択、中央に対象アイテム配置。
 */
public class EnchantGUI {

    private static final String TITLE = "§5✦ §dエンチャント台";
    private static final int SIZE = 54;

    // スロット定義
    public static final int ITEM_SLOT = 22;   // 対象アイテム（中央）
    public static final int CONFIRM_SLOT = 40; // 確認ボタン

    private EnchantGUI() {}

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);

        // 対象アイテムスロットマーカー
        inv.setItem(ITEM_SLOT, buildPlaceholder("§b装備品をここに置く", Material.ITEM_FRAME));

        // エンチャント種別一覧（左列）
        EnchantType[] types = EnchantType.values();
        int[] typeSlots = {0, 9, 18, 27, 36, 1, 10, 19, 28, 37};
        for (int i = 0; i < Math.min(types.length, typeSlots.length); i++) {
            inv.setItem(typeSlots[i], buildTypeIcon(types[i]));
        }

        // ランク選択（右列: ランク1〜5）
        int[] rankSlots = {8, 17, 26, 35, 44};
        for (int r = 0; r < 5; r++) {
            inv.setItem(rankSlots[r], buildRankIcon(r + 1));
        }

        // 確認ボタン
        inv.setItem(CONFIRM_SLOT, buildConfirm());

        // ボーダー
        ItemStack border = buildBorder();
        for (int slot : new int[]{4, 13, 31}) {
            inv.setItem(slot, border);
        }

        player.openInventory(inv);
    }

    private static ItemStack buildTypeIcon(EnchantType type) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName("§e" + type.getDisplayName());
        meta.setLore(Arrays.asList(
            "§7" + type.getDescription(1).replace("5.0", "5"),
            "",
            "§8クリックで選択"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildRankIcon(int rank) {
        Material mat = switch (rank) {
            case 1 -> Material.GRAY_DYE;
            case 2 -> Material.GREEN_DYE;
            case 3 -> Material.CYAN_DYE;
            case 4 -> Material.PURPLE_DYE;
            case 5 -> Material.ORANGE_DYE;
            default -> Material.WHITE_DYE;
        };
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String rankName = switch (rank) {
            case 1 -> "§7Rank I";
            case 2 -> "§aRank II";
            case 3 -> "§bRank III";
            case 4 -> "§dRank IV";
            case 5 -> "§6Rank V";
            default -> "Rank " + rank;
        };
        meta.setDisplayName(rankName);
        meta.setLore(List.of("§7効果倍率: §f" + (rank * 5) + "%", "", "§8クリックで選択"));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildConfirm() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a✔ エンチャント実行");
            meta.setLore(List.of("§7種別とランクを選択してから", "§7クリックしてください"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack buildPlaceholder(String name, Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack buildBorder() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String getTitle() { return TITLE; }
}
