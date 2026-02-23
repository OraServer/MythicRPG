package com.woxloi.mythicrpg.ui.title;

import com.woxloi.mythicrpg.dungeon.DungeonGUI;
import com.woxloi.mythicrpg.title.TitleDefinition;
import com.woxloi.mythicrpg.title.TitleManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;

/**
 * 全称号と解除進捗を一覧表示するGUI。
 * TitleGUI（選択用）の詳細版として機能する。
 */
public class TitleDetailGUI implements Listener {

    private static final String TITLE = "§6§l称号図鑑";

    public static final TitleDetailGUI INSTANCE = new TitleDetailGUI();

    private TitleDetailGUI() {}

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        Set<TitleDefinition> unlocked = TitleManager.getUnlocked(player);
        TitleDefinition active = TitleManager.getActiveTitle(player.getUniqueId());

        TitleDefinition[] all = TitleDefinition.values();
        for (int i = 0; i < Math.min(all.length, 45); i++) {
            TitleDefinition t = all[i];
            boolean isUnlocked = unlocked.contains(t);
            boolean isActive   = t.equals(active);
            inv.setItem(i, buildTitleIcon(t, player, isUnlocked, isActive));
        }

        // 下段ボーダーとサマリー
        ItemStack border = buildBorder();
        for (int s = 45; s < 54; s++) inv.setItem(s, border);
        inv.setItem(49, buildSummaryIcon(player, unlocked.size(), all.length));

        player.openInventory(inv);
    }

    private static ItemStack buildTitleIcon(TitleDefinition title, Player player,
                                             boolean isUnlocked, boolean isActive) {
        Material mat = isActive ? Material.GOLD_INGOT
                     : isUnlocked ? Material.EMERALD
                     : Material.GRAY_DYE;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(isUnlocked ? title.getDisplayTag() : "§8???");
        meta.setLore(TitleRenderer.buildTitleLore(title, player.getUniqueId(), isUnlocked, isActive));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack buildSummaryIcon(Player player, int unlockedCount, int totalCount) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            int pct = (int)((double)unlockedCount / totalCount * 100);
            meta.setDisplayName("§e" + player.getName() + " の称号図鑑");
            meta.setLore(java.util.List.of(
                "§7解放済み: §e" + unlockedCount + " §7/ §e" + totalCount,
                "§7達成率: §a" + pct + "%",
                "",
                "§7クリックで称号を設定できます"
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack buildBorder() {
        ItemStack item = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
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

        TitleDefinition[] all = TitleDefinition.values();
        if (slot >= all.length) return;

        TitleDefinition clicked = all[slot];
        Set<TitleDefinition> unlocked = TitleManager.getUnlocked(player);
        if (!unlocked.contains(clicked)) return;

        TitleManager.setActiveTitle(player, clicked);
        open(player); // 再描画
    }

    public static String getTitle() { return TITLE; }
}
