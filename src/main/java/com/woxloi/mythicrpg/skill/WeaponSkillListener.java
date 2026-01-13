package com.woxloi.mythicrpg.skill;

import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class WeaponSkillListener implements Listener {

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        PlayerData data = PlayerDataManager.get(player);
        if (data == null || !data.hasJob()) return;

        if (player.getInventory().getItemInMainHand() == null) return;

        SkillTrigger trigger = switch (e.getAction()) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> SkillTrigger.RIGHT_CLICK;
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> SkillTrigger.LEFT_CLICK;
            default -> null;
        };

        if (trigger == null) return;

        Material weapon = player.getInventory().getItemInMainHand().getType();

        for (Skill skill : SkillRegistry.getSkills(data.getJob())) {
            if (skill.getTrigger() == trigger &&
                    isValidWeapon(skill, weapon)) {

                SkillManager.useSkill(player, skill.getId());
                e.setCancelled(true);
                return;
            }
        }
    }

    private boolean isValidWeapon(Skill skill, Material weapon) {
        return switch (skill.getId()) {
            case "slash" -> weapon.name().endsWith("_SWORD");
            case "fireball" -> weapon == Material.BLAZE_ROD;
            default -> false;
        };
    }
}
