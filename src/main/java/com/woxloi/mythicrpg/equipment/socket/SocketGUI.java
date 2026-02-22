package com.woxloi.mythicrpg.equipment.socket;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.model.RpgItemSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * ソケット操作GUI。
 * 装備を中央に表示し、宝石を選んでスロットに挿入できる。
 */
public class SocketGUI {

    public static final String TITLE = ChatColor.DARK_PURPLE + "◆ ソケット加工台 ◆";
    private static final int SIZE = 54;

    /**
     * GUIを開く。targetItemはプレイヤーが手に持っているRpgItemを渡す。
     */
    public static void open(Player player, RpgItem item) {
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);

        // 中央にターゲット装備を表示
        inv.setItem(22, buildItemDisplay(item));

        // 宝石を並べる（上段）
        int col = 0;
        for (GemType gem : GemType.values()) {
            if (col >= 9) break;
            inv.setItem(col, buildGemItem(gem, item));
            col++;
        }

        // ソケットスロット状況（下段）
        if (item.socketSlots != null) {
            for (int i = 0; i < item.socketSlots.size() && i < 9; i++) {
                inv.setItem(45 + i, buildSlotDisplay(item.socketSlots.get(i), i));
            }
        }

        // 説明
        inv.setItem(49, buildInfoItem());

        player.openInventory(inv);
    }

    private static ItemStack buildItemDisplay(RpgItem item) {
        ItemStack is = item.baseItem.clone();
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "装備: " + item.displayName);
            int sockets = item.socketSlots == null ? 0 : item.socketSlots.size();
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "ソケット数: " + sockets,
                    ChatColor.GRAY + "宝石を選んでクリック→スロット選択"
            ));
            is.setItemMeta(meta);
        }
        return is;
    }

    private static ItemStack buildGemItem(GemType gem, RpgItem item) {
        ItemStack is = new ItemStack(gem.material);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(gem.getColoredName());
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "HP+" + gem.bonusMaxHp + "  ATK+" + gem.bonusAtk,
                    ChatColor.GRAY + "MP+" + gem.bonusMp    + "  DEF+" + gem.bonusDef,
                    ChatColor.GRAY + "SP+" + gem.bonusSp,
                    "",
                    ChatColor.YELLOW + "クリックでスロットに挿入"
            ));
            is.setItemMeta(meta);
        }
        return is;
    }

    private static ItemStack buildSlotDisplay(SocketSlot slot, int index) {
        Material mat = slot.isEmpty() ? Material.GRAY_STAINED_GLASS_PANE
                                      : slot.getGem().material;
        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.WHITE + "スロット " + (index + 1) + ": " + slot.toDisplayString());
            if (!slot.isEmpty()) {
                meta.setLore(List.of(ChatColor.RED + "クリックで取り外し（宝石消滅）"));
            }
            is.setItemMeta(meta);
        }
        return is;
    }

    private static ItemStack buildInfoItem() {
        ItemStack is = new ItemStack(Material.BOOK);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "ソケット加工について");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "上段: 挿入する宝石を選択",
                    ChatColor.GRAY + "下段: スロットを選択して挿入",
                    ChatColor.GRAY + "取り外しの際、宝石は消滅します"
            ));
            is.setItemMeta(meta);
        }
        return is;
    }
}
