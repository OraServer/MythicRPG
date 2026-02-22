package com.woxloi.mythicrpg.buff;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * RPGバフポーションアイテムの生成・解析ユーティリティ
 */
public class BuffPotion {

    public static final String KEY_BUFF_TYPE      = "mrpg_buff_type";
    public static final String KEY_BUFF_MAGNITUDE = "mrpg_buff_magnitude";
    public static final String KEY_BUFF_DURATION  = "mrpg_buff_duration";

    /** バフポーションアイテムを生成 */
    public static ItemStack create(Plugin plugin, BuffType type, double magnitude, int seconds) {
        ItemStack item = new ItemStack(type.isBuff() ? Material.POTION : Material.SPLASH_POTION);
        ItemMeta meta = item.getItemMeta();

        String color = type.isBuff() ? "§a" : "§c";
        meta.displayName(Component.text(color + type.getDisplayName() + " ポーション"));
        meta.lore(List.of(
                Component.text("§7効果: §f" + type.getDisplayName()),
                Component.text("§7倍率: §f×" + magnitude),
                Component.text("§7持続: §f" + seconds + "秒"),
                Component.text(""),
                Component.text("§e右クリックで使用")
        ));

        NamespacedKey keyType = new NamespacedKey(plugin, KEY_BUFF_TYPE);
        NamespacedKey keyMag  = new NamespacedKey(plugin, KEY_BUFF_MAGNITUDE);
        NamespacedKey keyDur  = new NamespacedKey(plugin, KEY_BUFF_DURATION);

        meta.getPersistentDataContainer().set(keyType, PersistentDataType.STRING, type.name());
        meta.getPersistentDataContainer().set(keyMag,  PersistentDataType.DOUBLE, magnitude);
        meta.getPersistentDataContainer().set(keyDur,  PersistentDataType.INTEGER, seconds);

        item.setItemMeta(meta);
        return item;
    }

    /** アイテムがバフポーションかどうか判定 */
    public static boolean isBuffPotion(Plugin plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(plugin, KEY_BUFF_TYPE);
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    /** アイテムからBuffTypeを取得 */
    public static BuffType getType(Plugin plugin, ItemStack item) {
        NamespacedKey key = new NamespacedKey(plugin, KEY_BUFF_TYPE);
        String name = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return name == null ? null : BuffType.valueOf(name);
    }

    public static double getMagnitude(Plugin plugin, ItemStack item) {
        NamespacedKey key = new NamespacedKey(plugin, KEY_BUFF_MAGNITUDE);
        Double v = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
        return v == null ? 0.0 : v;
    }

    public static int getDuration(Plugin plugin, ItemStack item) {
        NamespacedKey key = new NamespacedKey(plugin, KEY_BUFF_DURATION);
        Integer v = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        return v == null ? 30 : v;
    }
}
