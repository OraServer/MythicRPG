package com.woxloi.mythicrpg.equipment.identify;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.model.RpgItemSerializer;
import com.woxloi.mythicrpg.equipment.random.RandomItemGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 未鑑定装備システム。
 * 未鑑定アイテムを鑑定書で鑑定するとランダムステータスの装備が出現する。
 */
public class IdentifyManager {

    private static final Random RANDOM = new Random();

    /** 未鑑定アイテムのタグ（PDC管理用）*/
    public static final String TAG_UNIDENTIFIED = "mrpg_unidentified";
    public static final String TAG_RARITY       = "mrpg_unid_rarity";

    /**
     * 未鑑定アイテムを生成する。
     */
    public static ItemStack createUnidentifiedItem(String rarityStr) {
        ItemStack is = new ItemStack(Material.POTION);
        // MaterialがなければPAPERで代用
        is = new ItemStack(Material.PAPER);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_GRAY + "§k■ " + ChatColor.GRAY + "未鑑定装備 §7[" + rarityStr + "]" + ChatColor.DARK_GRAY + " §k■");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY  + "???",
                    "",
                    ChatColor.YELLOW + "鑑定書を使用して鑑定できます",
                    ChatColor.DARK_GRAY + "Rarity: " + rarityStr
            ));
            // PersistentDataContainerにタグ保存
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(MythicRPG.getInstance(), TAG_UNIDENTIFIED);
            org.bukkit.NamespacedKey rarityKey = new org.bukkit.NamespacedKey(MythicRPG.getInstance(), TAG_RARITY);
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            meta.getPersistentDataContainer().set(rarityKey, org.bukkit.persistence.PersistentDataType.STRING, rarityStr);
            is.setItemMeta(meta);
        }
        return is;
    }

    /**
     * 鑑定書アイテムを生成する。
     */
    public static ItemStack createScrollOfIdentify() {
        ItemStack is = new ItemStack(Material.MAP);
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "✦ 鑑定書 ✦");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "未鑑定装備に右クリックで使用",
                    ChatColor.YELLOW + "装備の秘めた力を解放する"
            ));
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(MythicRPG.getInstance(), "identify_scroll");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            is.setItemMeta(meta);
        }
        return is;
    }

    /**
     * アイテムが未鑑定かどうかを確認する。
     */
    public static boolean isUnidentified(ItemStack is) {
        if (is == null || !is.hasItemMeta()) return false;
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(MythicRPG.getInstance(), TAG_UNIDENTIFIED);
        return is.getItemMeta().getPersistentDataContainer()
                 .has(key, org.bukkit.persistence.PersistentDataType.BYTE);
    }

    /**
     * アイテムが鑑定書かどうかを確認する。
     */
    public static boolean isIdentifyScroll(ItemStack is) {
        if (is == null || !is.hasItemMeta()) return false;
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(MythicRPG.getInstance(), "identify_scroll");
        return is.getItemMeta().getPersistentDataContainer()
                 .has(key, org.bukkit.persistence.PersistentDataType.BYTE);
    }

    /**
     * 未鑑定アイテムを鑑定して装備を生成する。
     * @return 生成された装備ItemStack, または null(失敗時)
     */
    public static ItemStack identify(ItemStack unidentified) {
        if (!isUnidentified(unidentified)) return null;

        org.bukkit.NamespacedKey rarityKey = new org.bukkit.NamespacedKey(MythicRPG.getInstance(), TAG_RARITY);
        String rarityStr = unidentified.getItemMeta().getPersistentDataContainer()
                .getOrDefault(rarityKey, org.bukkit.persistence.PersistentDataType.STRING, "COMMON");

        com.woxloi.mythicrpg.equipment.model.EquipRarity rarity;
        try {
            rarity = com.woxloi.mythicrpg.equipment.model.EquipRarity.valueOf(rarityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            rarity = com.woxloi.mythicrpg.equipment.model.EquipRarity.COMMON;
        }

        RpgItem item = RandomItemGenerator.generate(rarity, RANDOM);
        return RpgItemSerializer.serialize(item);
    }
}
