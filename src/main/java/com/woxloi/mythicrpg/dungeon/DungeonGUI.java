package com.woxloi.mythicrpg.dungeon;

import com.woxloi.mythicrpg.MythicRPG;
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

import java.util.ArrayList;
import java.util.List;

/**
 * ダンジョン選択GUI。
 * 利用可能なダンジョン一覧を表示し、クリックで入場を試みる。
 */
public class DungeonGUI implements Listener {

    private static final String TITLE = "§4⚔ §cダンジョン選択";

    private static final Material[] DUNGEON_ICONS = {
        Material.MOSSY_COBBLESTONE,
        Material.DARK_OAK_WOOD,
        Material.NETHER_BRICKS,
        Material.CRYING_OBSIDIAN,
        Material.ANCIENT_DEBRIS
    };

    private DungeonGUI() {}

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        PlayerData data = PlayerDataManager.get(player);
        int playerLevel = data != null ? data.getLevel() : 1;

        List<DungeonDefinition> defs = new ArrayList<>(DungeonManager.getAllDefinitions());
        for (int i = 0; i < Math.min(defs.size(), 45); i++) {
            Material icon = i < DUNGEON_ICONS.length ? DUNGEON_ICONS[i] : Material.STONE;
            inv.setItem(i * 2 + 10, buildDungeonIcon(defs.get(i), playerLevel, icon));
        }

        // ボーダー
        ItemStack border = buildBorder();
        for (int s = 45; s < 54; s++) inv.setItem(s, border);

        // ヘッダー
        inv.setItem(49, buildHeaderIcon(player, playerLevel));

        player.openInventory(inv);
    }

    private static ItemStack buildDungeonIcon(DungeonDefinition def, int playerLevel, Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        boolean canEnter = playerLevel >= def.getRequiredLevel();
        meta.setDisplayName((canEnter ? "§a" : "§c") + def.getDisplayName());
        meta.setLore(def.buildLore(playerLevel));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildHeaderIcon(Player player, int playerLevel) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + player.getName());
            meta.setLore(List.of("§7現在Lv: §e" + playerLevel,
                    DungeonManager.isInDungeon(player) ? "§c現在参加中" : "§a参加可能"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack buildBorder() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); item.setItemMeta(meta); }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!TITLE.equals(event.getView().getTitle())) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 45) return;

        List<DungeonDefinition> defs = new ArrayList<>(DungeonManager.getAllDefinitions());
        // スロット → インデックス変換（偶数スロット10,12,14...）
        for (int i = 0; i < defs.size(); i++) {
            if (slot == i * 2 + 10) {
                player.closeInventory();
                String error = DungeonManager.enter(player, defs.get(i).getId());
                if (error != null) MythicRPG.playerPrefixMsg(player, "§c" + error);
                return;
            }
        }
    }

    public static String getTitle() { return TITLE; }
}
