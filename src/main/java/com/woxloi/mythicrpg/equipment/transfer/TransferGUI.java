package com.woxloi.mythicrpg.equipment.transfer;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.equipment.model.EquipStats;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * ステータス継承GUIの構築。
 * 左に継承元、右に継承先を置いて中央の矢印ボタンで実行。
 */
public class TransferGUI {

    public static final String TITLE = ChatColor.LIGHT_PURPLE + "⟹ ステータス継承台 ⟹";
    private static final int SIZE = 27;

    public static final int SLOT_SOURCE  = 11; // 継承元
    public static final int SLOT_TARGET  = 15; // 継承先
    public static final int SLOT_EXECUTE = 13; // 実行ボタン

    public static void open(Player player, RpgItem source, RpgItem target) {
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);

        if (source != null) inv.setItem(SLOT_SOURCE, buildDisplay(source, "§b継承元（消滅）"));
        if (target != null) inv.setItem(SLOT_TARGET, buildDisplay(target, "§a継承先"));

        inv.setItem(SLOT_EXECUTE, buildExecuteButton(source, target));
        inv.setItem(4, buildInfo());

        player.openInventory(inv);
    }

    private static ItemStack buildDisplay(RpgItem item, String label) {
        ItemStack is = item.baseItem.clone();
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(label + " " + item.displayName);
            EquipStats s = item.baseStats;
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "スロット: " + item.slot.getDisplayName(),
                    ChatColor.GRAY + "レアリティ: " + item.rarity.displayName,
                    ChatColor.GRAY + "ATK: " + (int)s.attack,
                    ChatColor.GRAY + "DEF: " + (int)s.defense,
                    ChatColor.GRAY + "MaxHP: " + (int)s.maxHpBonus,
                    ChatColor.GRAY + "強化Lv: +" + item.enhanceLevel
            ));
            is.setItemMeta(meta);
        }
        return is;
    }

    private static ItemStack buildExecuteButton(RpgItem source, RpgItem target) {
        boolean ready = source != null && target != null;
        Material mat = ready ? Material.EMERALD : Material.BARRIER;
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ready ? ChatColor.GREEN + "▶ 継承実行" : ChatColor.RED + "アイテムが不足");
            if (ready) {
                EquipStats preview = TransferManager.previewTransfer(source, target);
                meta.setLore(Arrays.asList(
                        ChatColor.AQUA + "継承ステータス(30%)",
                        ChatColor.GRAY + "ATK+" + (int)preview.attack,
                        ChatColor.GRAY + "DEF+" + (int)preview.defense,
                        ChatColor.GRAY + "MaxHP+" + (int)preview.maxHpBonus,
                        "",
                        ChatColor.RED + "継承元は消滅します",
                        ChatColor.YELLOW + "クリックで実行"
                ));
            }
            is.setItemMeta(meta);
        }
        return is;
    }

    private static ItemStack buildInfo() {
        ItemStack is = new ItemStack(Material.BOOK);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "継承について");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "高レア装備→低レア装備に",
                    ChatColor.GRAY + "ステータスの30%を引き継ぐ",
                    ChatColor.RED  + "継承元のアイテムは消滅します",
                    ChatColor.GRAY + "同スロットのみ継承可能"
            ));
            is.setItemMeta(meta);
        }
        return is;
    }
}
