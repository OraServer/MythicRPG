package com.woxloi.mythicrpg.skill;

import com.woxloi.mythicrpg.job.JobType;
import com.woxloi.mythicrpg.skill.impl.BasicAttackSkill;
import com.woxloi.mythicrpg.skill.impl.FireballSkill;
import com.woxloi.mythicrpg.skill.impl.PowerSlashSkill;

import java.util.*;

public class SkillRegistry {

    private static final Map<JobType, List<Skill>> skills = new HashMap<>();

    static {
        skills.put(JobType.WARRIOR, List.of(
                new BasicAttackSkill(),
                new PowerSlashSkill()
        ));

        skills.put(JobType.MAGE, List.of(
                new FireballSkill()
        ));
    }

    public static List<Skill> getSkills(JobType job) {
        return skills.getOrDefault(job, Collections.emptyList());
    }

    public static Skill getSkill(JobType job, String id) {
        return getSkills(job).stream()
                .filter(s -> s.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }
}
