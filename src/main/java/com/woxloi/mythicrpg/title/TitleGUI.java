package com.woxloi.mythicrpg.title;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * /mrpg title で開く称号選択GUI
 */
public class TitleGUI implements Listener {

    private static final String TITLE = "§6§l称号選択";

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(TITLE));
        Set<TitleDefinition> unlocked = TitleManager.getUnlocked(player);
        TitleDefinition active = TitleManager.getActiveTitle(player.getUniqueId());

        TitleDefinition[] all = TitleDefinition.values();
        for (int i = 0; i < Math.min(all.length, 45); i++) {
            TitleDefinition title = all[i];
            boolean isUnlocked = unlocked.contains(title);
            boolean isActive   = title == active;

            Material mat = isActive ? Material.NETHER_STAR : (isUnlocked ? Material.GOLD_INGOT : Material.IRON_NUGGET);
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text(title.getDisplayTag()));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7" + title.getDescription()));
            lore.add(Component.text(""));
            if (isActive)        lore.add(Component.text("§a§l現在選択中"));
            else if (isUnlocked) lore.add(Component.text("§e§lクリックで選択"));
            else                 lore.add(Component.text("§c§l未解放"));

            meta.lore(lore);
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        // 解除ボタン
        ItemStack remove = new ItemStack(Material.BARRIER);
        ItemMeta rm = remove.getItemMeta();
        rm.displayName(Component.text("§c称号を外す"));
        remove.setItemMeta(rm);
        inv.setItem(49, remove);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().title().equals(Component.text(TITLE))) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0) return;

        if (slot == 49) {
            // 称号を外す
            TitleManager.setActiveTitle(player, null);
            player.sendMessage("§7称号を外しました");
            open(player);
            return;
        }

        TitleDefinition[] all = TitleDefinition.values();
        if (slot >= all.length) return;

        TitleDefinition selected = all[slot];
        if (!TitleManager.getUnlocked(player).contains(selected)) {
            player.sendMessage("§cこの称号はまだ解放されていません");
            return;
        }

        TitleManager.setActiveTitle(player, selected);
        player.sendMessage("§a称号を " + selected.getDisplayTag() + " §aに設定しました");
        open(player);
    }
}
