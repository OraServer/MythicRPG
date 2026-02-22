package com.woxloi.mythicrpg.equipment.enhancer;

import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.model.RpgItemSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 装備強化専用GUI。
 * スロット22: 強化対象アイテム配置
 * スロット26: エメラルド（強化素材）配置
 * スロット40: 強化実行ボタン
 */
public class EnhanceGUI {

    public static final String TITLE = "§e§l装備強化";
    public static final int TARGET_SLOT   = 22;
    public static final int MATERIAL_SLOT = 26;
    public static final int BUTTON_SLOT   = 40;

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(TITLE));

        // 配置ガイド
        inv.setItem(TARGET_SLOT, buildGuide(Material.ITEM_FRAME,
                "§e強化するアイテムをここに配置",
                "§7RPGアイテムのみ有効"));
        inv.setItem(MATERIAL_SLOT, buildGuide(Material.EMERALD,
                "§aエメラルド（強化素材）",
                "§7自動で必要数が消費されます"));
        inv.setItem(BUTTON_SLOT, buildButton());

        // 成功率表
        inv.setItem(45, buildRateTable());

        fillGlass(inv);
        player.openInventory(inv);
    }

    private static ItemStack buildGuide(Material mat, String name, String desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(List.of(Component.text(desc)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildButton() {
        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§6§l強化実行！"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7強化対象アイテムをスロットに置いてクリック"));
        lore.add(Component.text("§c失敗時: +5以上はダウングレード"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildRateTable() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§b強化成功率"));
        meta.lore(List.of(
            Component.text("§7+0〜+5 : §a100%"),
            Component.text("§7+6     : §e70%"),
            Component.text("§7+7     : §e60%"),
            Component.text("§7+8     : §e50%"),
            Component.text("§7+9     : §e40%"),
            Component.text("§7+10    : §c30%"),
            Component.text("§7+11〜+15: §c20%"),
            Component.text("§7+16〜   : §4§l10%")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static void fillGlass(Inventory inv) {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.displayName(Component.text(" "));
        glass.setItemMeta(meta);
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, glass);
        }
    }
}
