package com.woxloi.mythicrpg.stats;

import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * /mrpg stats で開くステータスポイント振り分けGUI
 */
public class StatGUI implements Listener {

    private static final String TITLE = "§b§lステータス振り分け";

    // スロット配置
    // 10=STR, 12=VIT, 14=INT, 16=AGI
    // +1pt: 11, 13, 15, 17
    // リセット: 31, 残りポイント: 22

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, Component.text(TITLE));
        PlayerData data = PlayerDataManager.get(player);
        StatPoint sp    = StatPointManager.get(player.getUniqueId());
        if (data == null) return;

        // STR
        inv.setItem(10, makeStatItem(Material.DIAMOND_SWORD, StatType.STR, sp.getStrPoints(), sp.getBonusAttack(), "ATK"));
        inv.setItem(11, makePlusBtn(StatType.STR, sp.getFreePoints()));

        // VIT
        inv.setItem(12, makeStatItem(Material.APPLE, StatType.VIT, sp.getVitPoints(), sp.getBonusMaxHp(), "MaxHP"));
        inv.setItem(13, makePlusBtn(StatType.VIT, sp.getFreePoints()));

        // INT
        inv.setItem(14, makeStatItem(Material.LAPIS_LAZULI, StatType.INT, sp.getIntPoints(), sp.getBonusMaxMp(), "MaxMP"));
        inv.setItem(15, makePlusBtn(StatType.INT, sp.getFreePoints()));

        // AGI
        inv.setItem(16, makeStatItem(Material.FEATHER, StatType.AGI, sp.getAgiPoints(), sp.getBonusMaxSp(), "MaxSP"));
        inv.setItem(17, makePlusBtn(StatType.AGI, sp.getFreePoints()));

        // 残りポイント表示
        ItemStack pts = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta pm = pts.getItemMeta();
        pm.displayName(Component.text("§e残りポイント: §f" + sp.getFreePoints() + "pt"));
        pm.lore(List.of(Component.text("§7レベルアップで+" + StatPoint.POINTS_PER_LEVEL + "pt")));
        pts.setItemMeta(pm);
        inv.setItem(22, pts);

        // リセットボタン
        ItemStack reset = new ItemStack(Material.BARRIER);
        ItemMeta rm = reset.getItemMeta();
        rm.displayName(Component.text("§cリセット（全ポイントを戻す）"));
        reset.setItemMeta(rm);
        inv.setItem(31, reset);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().title().equals(Component.text(TITLE))) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        switch (slot) {
            case 11 -> { StatPointManager.allocate(player, StatType.STR, 1); open(player); }
            case 13 -> { StatPointManager.allocate(player, StatType.VIT, 1); open(player); }
            case 15 -> { StatPointManager.allocate(player, StatType.INT, 1); open(player); }
            case 17 -> { StatPointManager.allocate(player, StatType.AGI, 1); open(player); }
            case 31 -> { StatPointManager.reset(player); open(player); }
        }
    }

    private static ItemStack makeStatItem(Material mat, StatType stat, int pts, double bonus, String bonusLabel) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(stat.getDisplayName()));
        meta.lore(List.of(
                Component.text("§7振り分け済み: §f" + pts + "pt"),
                Component.text("§7" + bonusLabel + " ボーナス: §f+" + (int) bonus),
                Component.text("§7" + stat.getDescription())
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack makePlusBtn(StatType stat, int freePoints) {
        ItemStack item = new ItemStack(freePoints > 0 ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(freePoints > 0
                ? "§a§l+1 " + stat.getDisplayName()
                : "§cポイント不足"));
        item.setItemMeta(meta);
        return item;
    }
}
