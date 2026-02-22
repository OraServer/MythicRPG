package com.woxloi.mythicrpg.skill;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SkillManager {

    public static void useSkill(Player player, String skillId) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null || !data.hasJob()) return;

        Skill skill = SkillRegistry.getSkill(data.getJob(), skillId);
        if (skill == null) {
            MythicRPG.msg(player, "§cそのスキルは使えません");
            return;
        }

        if (data.getLevel() < skill.getUnlockLevel()) {
            MythicRPG.msg(player, "§cLv§e" + skill.getUnlockLevel() + "§c以上で解放されます");
            return;
        }

        if (SkillCooldownManager.isOnCooldown(player.getUniqueId(), skillId)) {
            long sec = SkillCooldownManager.getRemaining(player.getUniqueId(), skillId);
            MythicRPG.msg(player, "§cクールタイム中: §e" + sec + "秒");
            return;
        }

        boolean canUse = switch (skill.getResourceType()) {
            case MP -> data.consumeMp(skill.getCost());
            case SP -> data.consumeSp(skill.getCost());
            case NONE -> true;
        };

        if (!canUse) {
            String res = skill.getResourceType().name();
            MythicRPG.msg(player, "§c" + res + "が不足しています");
            return;
        }

        skill.execute(player);
        SkillCooldownManager.setCooldown(player.getUniqueId(), skillId, skill.getCooldown());

        MythicRPG.msg(player, "§aスキル発動: §e" + skill.getName());
    }

    public static void onLevelUp(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null || !data.hasJob()) return;

        SkillRegistry.getSkills(data.getJob()).forEach(skill -> {
            if (!data.hasSkill(skill.getId()) && data.getLevel() >= skill.getUnlockLevel()) {
                data.unlockSkill(skill.getId());
                MythicRPG.msg(player, "§aスキル解放: §e" + skill.getName());
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
        });
    }
}
