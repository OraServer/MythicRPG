package com.woxloi.mythicrpg.skill.impl;

import com.woxloi.mythicrpg.skill.ResourceType;
import com.woxloi.mythicrpg.skill.Skill;
import com.woxloi.mythicrpg.skill.SkillTrigger;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;

public class FireballSkill extends Skill {

    public FireballSkill() {
        super(
                "fireball",
                "ファイアボール",
                5,                      // CD
                6L,                     // Lv6
                SkillTrigger.RIGHT_CLICK,
                ResourceType.MP,
                20.0
        );
    }

    @Override
    public void execute(Player player) {
        player.launchProjectile(SmallFireball.class);
    }
}
