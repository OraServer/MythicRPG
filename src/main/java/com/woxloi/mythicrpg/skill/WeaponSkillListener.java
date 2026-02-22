package com.woxloi.mythicrpg.skill;

import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import com.woxloi.mythicrpg.skill.loader.YamlSkill;
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

        Material weapon = player.getInventory().getItemInMainHand().getType();

        SkillTrigger trigger = switch (e.getAction()) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> SkillTrigger.RIGHT_CLICK;
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK   -> SkillTrigger.LEFT_CLICK;
            default -> null;
        };
        if (trigger == null) return;

        for (Skill skill : SkillRegistry.getSkills(data.getJob())) {
            if (skill.getTrigger() == trigger && isValidWeapon(skill, weapon)) {
                SkillManager.useSkill(player, skill.getId());
                e.setCancelled(true);
                return;
            }
        }
    }

    /**
     * YamlSkill の weapon フィールドでバリデーション。
     * ANY → 常にtrue
     * SWORD → *_SWORD で終わるMaterial
     * それ以外 → Material名と完全一致
     */
    private boolean isValidWeapon(Skill skill, Material weapon) {
        String wep = "ANY";
        if (skill instanceof YamlSkill ys) {
            wep = ys.getWeapon().toUpperCase();
        }
        return switch (wep) {
            case "ANY"   -> true;
            case "SWORD" -> weapon.name().endsWith("_SWORD");
            case "BOW"   -> weapon == Material.BOW || weapon == Material.CROSSBOW;
            default      -> weapon.name().equals(wep);
        };
    }
}
