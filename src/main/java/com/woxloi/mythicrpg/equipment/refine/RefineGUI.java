package com.woxloi.mythicrpg.equipment.refine;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.model.EquipStats;
import com.woxloi.mythicrpg.equipment.model.RpgItemSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * 精錬台のGUI。
 * 中央に対象装備を置き、精錬素材数を選んで精錬を実行する。
 */
public class RefineGUI {

    public static final String TITLE = ChatColor.GOLD + "⚒ 精錬台 ⚒";
    private static final int SIZE = 27;

    // スロット定義
    private static final int SLOT_ITEM      = 13;
    private static final int SLOT_MAT_1     = 11;
    private static final int SLOT_MAT_3     = 12;
    private static final int SLOT_MAT_5     = 14;
    private static final int SLOT_REFINE    = 22;

    public static void open(Player player, RpgItem item) {
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);

        // 装備表示
        inv.setItem(SLOT_ITEM, buildItemDisplay(item));

        // 素材選択ボタン
        inv.setItem(SLOT_MAT_1, buildMatButton(1));
        inv.setItem(SLOT_MAT_3, buildMatButton(3));
        inv.setItem(SLOT_MAT_5, buildMatButton(5));

        // 精錬ボタン
        inv.setItem(SLOT_REFINE, buildRefineButton(item));

        // 説明
        inv.setItem(4, buildInfoItem(item));

        player.openInventory(inv);
    }

    private static ItemStack buildItemDisplay(RpgItem item) {
        ItemStack is = item.baseItem.clone();
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "[精錬対象] " + item.displayName
                    + RefineManager.getRefineTag(item));
            com.woxloi.mythicrpg.equipment.model.EquipStats s = item.baseStats;
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "精錬Lv: §e" + item.refineLevel,
                    ChatColor.GRAY + "ATK: §f" + (int)s.attack,
                    ChatColor.GRAY + "DEF: §f" + (int)s.defense,
                    ChatColor.GRAY + "MaxHP: §f" + (int)s.maxHpBonus
            ));
            is.setItemMeta(meta);
        }
        return is;
    }

    private static ItemStack buildMatButton(int count) {
        ItemStack is = new ItemStack(Material.QUARTZ, count);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.WHITE + "精錬石 ×" + count);
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "使用素材: " + count + "個",
                    ChatColor.YELLOW + "成功率+" + (count * 5) + "%",
                    ChatColor.GREEN + "クリックで選択"
            ));
            is.setItemMeta(meta);
        }
        return is;
    }

    private static ItemStack buildRefineButton(RpgItem item) {
        double rate = Math.max(10, 90 - item.refineLevel * 10);
        ItemStack is = new ItemStack(Material.ANVIL);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "▶ 精錬実行");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "現在の成功率: §e" + (int)rate + "%",
                    ChatColor.RED + "失敗: 素材消失",
                    ChatColor.DARK_RED + "破壊: 精錬Lvリセット",
                    "",
                    ChatColor.GREEN + "クリックで精錬"
            ));
            is.setItemMeta(meta);
        }
        return is;
    }

    private static ItemStack buildInfoItem(RpgItem item) {
        ItemStack is = new ItemStack(Material.BOOK);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "精錬について");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "精錬するとステータスが強化されます",
                    ChatColor.GRAY + "精錬Lvが高いほど失敗・破壊リスクUP",
                    ChatColor.YELLOW + "現在 +Lv" + item.refineLevel + " / 上限 +10"
            ));
            is.setItemMeta(meta);
        }
        return is;
    }
}
