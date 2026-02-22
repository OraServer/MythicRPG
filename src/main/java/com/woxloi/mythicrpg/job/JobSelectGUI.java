package com.woxloi.mythicrpg.job;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class JobSelectGUI {

    public static final String TITLE = "§6§lジョブを選択";

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, Component.text(TITLE));
        inv.setItem(2, createJobItem(Material.IRON_SWORD, JobType.WARRIOR, "§7近接・高耐久"));
        inv.setItem(4, createJobItem(Material.BLAZE_ROD,  JobType.MAGE,    "§7高火力・MP型"));
        inv.setItem(6, createJobItem(Material.BOW,        JobType.ARCHER,  "§7遠距離・バランス"));
        player.openInventory(inv);
    }

    private static ItemStack createJobItem(Material mat, JobType job, String desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§e" + job.getDisplayName()));
        meta.lore(List.of(Component.text(desc)));
        item.setItemMeta(meta);
        return item;
    }
}
