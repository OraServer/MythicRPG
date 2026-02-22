package com.woxloi.mythicrpg.ui.stats;

import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
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
 * /mrpg stats detail で開く装備・バフ込み全ステータス詳細GUI。
 * StatsRendererを使って表示内容を構築する。
 */
public class StatDetailGUI implements Listener {

    private static final String TITLE = "§b§l詳細ステータス";

    private StatDetailGUI() {}

    public static void open(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        Inventory inv = Bukkit.createInventory(null, 36, TITLE);

        // メインステータス表示（中央）
        inv.setItem(13, buildMainStats(player, data));

        // カテゴリ別表示
        inv.setItem(10, buildCombatStats(player, data));
        inv.setItem(12, buildResourceStats(data));
        inv.setItem(14, buildEquipSummary(player));
        inv.setItem(16, buildBuffSummary(player));

        // ボーダー
        ItemStack border = buildBorder();
        for (int s : new int[]{0,1,2,3,4,5,6,7,8,9,11,15,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35}) {
            inv.setItem(s, border);
        }

        player.openInventory(inv);
    }

    private static ItemStack buildMainStats(Player player, PlayerData data) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName("§e§l" + player.getName());
        meta.setLore(StatsRenderer.buildFullStatsLore(data, player));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildCombatStats(Player player, PlayerData data) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName("§c戦闘ステータス");
        var equip = com.woxloi.mythicrpg.equipment.EquipmentManager.getTotalStats(player);
        meta.setLore(List.of(
            "§c攻撃力: §f" + (int)(data.getAttack() + equip.attack),
            "§7防御力: §f" + (int)equip.defense,
            "§d魔 力:  §f" + (int)equip.magicPower,
            "§e会心率: §f" + String.format("%.1f%%", equip.critRate * 100),
            "§e会心倍率: §f+" + String.format("%.0f%%", equip.critDamage * 100)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildResourceStats(PlayerData data) {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName("§aリソース");
        meta.setLore(List.of(
            "§aHP: §f" + (int)data.getHp() + " / " + (int)data.getMaxHp(),
            "§bMP: §f" + (int)data.getMp() + " / " + (int)data.getMaxMp(),
            "§6SP: §f" + (int)data.getSp() + " / " + (int)data.getMaxSp()
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildEquipSummary(Player player) {
        ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName("§6装備中アイテム");
        meta.setLore(List.of("§7装備スロットを確認するには", "§7/mrpg equip を使用"));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildBuffSummary(Player player) {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName("§5アクティブバフ");
        meta.setLore(List.of("§7詳細は /mrpg buff で確認"));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildBorder() {
        ItemStack item = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
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
