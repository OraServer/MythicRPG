package com.woxloi.mythicrpg.skill.impl;

import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import com.woxloi.mythicrpg.skill.ResourceType;
import com.woxloi.mythicrpg.skill.Skill;
import com.woxloi.mythicrpg.skill.SkillTrigger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BasicAttackSkill extends Skill {

    public BasicAttackSkill() {
        super(
                "basic_attack",
                "基本攻撃",
                0,                      // クールダウンなし
                1L,                     // Lv1
                SkillTrigger.LEFT_CLICK,
                ResourceType.NONE,
                0.0
        );
    }

    @Override
    public void execute(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        LivingEntity target = getTarget(player, 3.0);
        if (target == null) return;

        double damage = data.getAttack();
        target.damage(damage, player);

        Vector knockback = target.getLocation().toVector()
                .subtract(player.getLocation().toVector())
                .normalize().multiply(0.3);

        target.setVelocity(knockback);
    }

    private LivingEntity getTarget(Player player, double range) {
        for (var entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity living && living != player) {
                return living;
            }
        }
        return null;
    }
}
