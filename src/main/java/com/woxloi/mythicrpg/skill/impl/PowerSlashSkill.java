package com.woxloi.mythicrpg.skill.impl;

import com.woxloi.mythicrpg.skill.ResourceType;
import com.woxloi.mythicrpg.skill.Skill;
import com.woxloi.mythicrpg.skill.SkillTrigger;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PowerSlashSkill extends Skill {

    public PowerSlashSkill() {
        super(
                "power_slash",
                "パワースラッシュ",
                5,                      // クールダウン（秒）
                5L,                     // 解放Lv
                SkillTrigger.RIGHT_CLICK,
                ResourceType.MP,
                20.0                    // MP消費
        );
    }

    @Override
    public void execute(Player player) {
        player.getWorld().spawnParticle(
                Particle.SWEEP_ATTACK,
                player.getLocation(),
                12,
                0.5, 0.2, 0.5,
                0
        );

        player.playSound(
                player.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                1f,
                1f
        );

        player.getNearbyEntities(3, 3, 3).stream()
                .filter(e -> e instanceof LivingEntity)
                .filter(e -> e != player)
                .map(e -> (LivingEntity) e)
                .forEach(e -> e.damage(6.0, player));
    }
}
