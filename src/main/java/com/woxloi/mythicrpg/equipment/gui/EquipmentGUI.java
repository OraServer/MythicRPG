package com.woxloi.mythicrpg.equipment.gui;

import com.woxloi.mythicrpg.equipment.EquipmentManager;
import com.woxloi.mythicrpg.equipment.model.EquipSlot;
import com.woxloi.mythicrpg.equipment.model.EquipStats;
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
 * 装備スロット一覧GUI（6行54マス）。
 * スロットレイアウト:
 *   行0: HELMET(1) CHESTPLATE(3) LEGGINGS(5) BOOTS(7) | 情報(13)
 *   行1: WEAPON(1) OFFHAND(3) RING_L(5) RING_R(7) NECKLACE(9) RELIC(11)
 *   行2: 合計ステータス表示
 */
public class EquipmentGUI {

    public static final String TITLE = "§6§l装備管理";

    /** スロット → インベントリインデックス のマッピング */
    public static int slotToIndex(EquipSlot slot) {
        return switch (slot) {
            case HELMET     -> 1;
            case CHESTPLATE -> 10;
            case LEGGINGS   -> 19;
            case BOOTS      -> 28;
            case WEAPON     -> 3;
            case OFFHAND    -> 12;
            case RING_L     -> 21;
            case RING_R     -> 30;
            case NECKLACE   -> 39;
            case RELIC      -> 48;
        };
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(TITLE));

        // 各スロットを配置
        for (EquipSlot slot : EquipSlot.values()) {
            RpgItem equipped = EquipmentManager.getEquipped(player, slot);
            int idx = slotToIndex(slot);

            if (equipped != null) {
                inv.setItem(idx, RpgItemSerializer.toItemStack(equipped));
            } else {
                inv.setItem(idx, buildEmptySlot(slot));
            }
        }

        // 合計ステータス表示
        inv.setItem(49, buildStatsItem(player));

        // 強化ボタン
        inv.setItem(51, buildEnhanceButton());

        // フィラー
        fillGlass(inv);

        player.openInventory(inv);
    }

    private static ItemStack buildEmptySlot(EquipSlot slot) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§7" + slot.icon + " " + slot.displayName + " §8(未装備)"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7アイテムをここにドラッグ&ドロップ"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildStatsItem(Player player) {
        EquipStats stats = EquipmentManager.getTotalStats(player);
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§b§l装備ステータス合計"));
        List<Component> lore = new ArrayList<>();
        if (stats.attack    > 0) lore.add(Component.text("§c攻撃力: §f+" + (int) stats.attack));
        if (stats.defense   > 0) lore.add(Component.text("§7防御力: §f+" + (int) stats.defense));
        if (stats.maxHpBonus> 0) lore.add(Component.text("§a最大HP: §f+" + (int) stats.maxHpBonus));
        if (stats.maxMpBonus> 0) lore.add(Component.text("§bMP上限: §f+" + (int) stats.maxMpBonus));
        if (stats.maxSpBonus> 0) lore.add(Component.text("§6SP上限: §f+" + (int) stats.maxSpBonus));
        if (stats.critRate  > 0) lore.add(Component.text(String.format("§e会心率: §f+%.1f%%", stats.critRate * 100)));
        if (stats.magicPower> 0) lore.add(Component.text("§d魔力: §f+" + (int) stats.magicPower));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildEnhanceButton() {
        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§e§l装備強化"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7強化したいアイテムを手に持って"));
        lore.add(Component.text("§7このボタンをクリック"));
        meta.lore(lore);
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
