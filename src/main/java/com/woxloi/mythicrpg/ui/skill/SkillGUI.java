package com.woxloi.mythicrpg.ui.skill;

import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.skill.Skill;
import com.woxloi.mythicrpg.skill.SkillRegistry;
import com.woxloi.mythicrpg.skill.ResourceType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SkillGUI {

    public static final String TITLE = "§6§lスキル一覧";

    public static void open(Player player, PlayerData data) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        int slot = 10;

        for (Skill skill : SkillRegistry.getSkills(data.getJob())) {
            inv.setItem(slot++, createSkillItem(data, skill));
        }

        player.openInventory(inv);
    }

    private static ItemStack createSkillItem(PlayerData data, Skill skill) {
        boolean unlocked = data.getLevel() >= skill.getUnlockLevel();

        ItemStack item = new ItemStack(
                unlocked ? Material.ENCHANTED_BOOK : Material.BOOK
        );

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(
                (unlocked ? "§a" : "§c") + skill.getName()
        );

        List<String> lore = new ArrayList<>();
        lore.add("§7解放Lv: §e" + skill.getUnlockLevel());

        if (skill.getResourceType() != ResourceType.NONE) {
            lore.add("§7消費: §b" + skill.getCost() + " " + skill.getResourceType());
        }

        lore.add("§7CT: §e" + skill.getCooldown() + "秒");

        lore.add("");
        lore.add(unlocked
                ? "§a▶ クリックで使用"
                : "§c✖ 未解放");

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }
}
