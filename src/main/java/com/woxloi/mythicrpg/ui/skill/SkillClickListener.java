package com.woxloi.mythicrpg.ui.skill;

import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import com.woxloi.mythicrpg.skill.Skill;
import com.woxloi.mythicrpg.skill.SkillManager;
import com.woxloi.mythicrpg.skill.SkillRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SkillClickListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals(SkillGUI.TITLE)) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        PlayerData data = PlayerDataManager.get(player);
        if (data == null || !data.hasJob()) return;

        String name = item.getItemMeta().getDisplayName()
                .replace("§a", "")
                .replace("§c", "");

        for (Skill skill : SkillRegistry.getSkills(data.getJob())) {
            if (skill.getName().equals(name)) {
                SkillManager.useSkill(player, skill.getId());
                player.closeInventory();
                return;
            }
        }
    }
}
