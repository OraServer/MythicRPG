package com.woxloi.mythicrpg.element;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * プレイヤーの属性耐性一覧を表示するGUI。
 * /mrpg element で開く。
 */
public class ElementResistanceGUI implements Listener {

    private static final String TITLE = "§5⚗ §d属性耐性";

    public static final ElementResistanceGUI INSTANCE = new ElementResistanceGUI();

    private static final Material[] ELEMENT_MATERIALS = {
        Material.BLAZE_POWDER,    // FIRE
        Material.PRISMARINE_SHARD,// WATER
        Material.FEATHER,         // WIND
        Material.DIRT,            // EARTH
        Material.GLOWSTONE_DUST,  // LIGHT
        Material.INK_SAC,         // DARK
        Material.GRAY_DYE         // NONE
    };

    private ElementResistanceGUI() {}

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        ElementType[] types = ElementType.values();
        Map<ElementType, Double> res = ElementManager.getAllResistances(player.getUniqueId());

        // 属性ごとに耐性を表示（上段: 3列2行 = 7属性）
        int[] displaySlots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < types.length; i++) {
            ElementType type = types[i];
            double resistance = res.getOrDefault(type, 0.0);
            inv.setItem(displaySlots[i], buildResistanceIcon(type, resistance, ELEMENT_MATERIALS[i]));
        }

        // ヘッダー情報
        inv.setItem(4, buildInfoIcon(player));

        // ボーダー
        ItemStack border = buildBorder();
        for (int s : new int[]{0,1,2,3,5,6,7,8,9,17,18,19,20,21,22,23,24,25,26}) {
            inv.setItem(s, border);
        }

        player.openInventory(inv);
    }

    private static ItemStack buildResistanceIcon(ElementType type, double resistance, Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(type.getIcon() + " " + type.getDisplayName());

        List<String> lore = new ArrayList<>();
        String resistText;
        String bar;

        if (resistance > 0) {
            int pct = (int)(resistance * 100);
            resistText = "§a+" + pct + "% 軽減";
            bar = "§a" + "█".repeat(pct / 10) + "§8" + "░".repeat(10 - pct / 10);
        } else if (resistance < 0) {
            int pct = (int)(-resistance * 100);
            resistText = "§c-" + pct + "% 弱点";
            bar = "§c" + "█".repeat(pct / 10) + "§8" + "░".repeat(10 - pct / 10);
        } else {
            resistText = "§7±0% 等倍";
            bar = "§8░░░░░░░░░░";
        }

        lore.add(bar);
        lore.add(resistText);
        lore.add("");

        // 相性一覧
        lore.add("§8--- 攻撃相性 ---");
        for (ElementType atk : ElementType.values()) {
            if (atk == ElementType.NONE) continue;
            double mult = atk.getAffinityMultiplier(type);
            if (mult == 1.0) continue;
            String tag = mult >= 2.0 ? "§c弱点" : "§b耐性";
            lore.add("§7" + atk.getIcon() + atk.getDisplayName() + ": " + tag + " §8×" + mult);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildInfoIcon(Player player) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b" + player.getName() + " の属性耐性");
            meta.setLore(List.of(
                "§7装備・バフによって耐性は変動します",
                "§7弱点属性には注意してください"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack buildBorder() {
        ItemStack item = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); item.setItemMeta(meta); }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (TITLE.equals(event.getView().getTitle())) event.setCancelled(true);
    }

    public static String getTitle() { return TITLE; }
}
