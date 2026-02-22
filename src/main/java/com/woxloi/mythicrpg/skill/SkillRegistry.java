package com.woxloi.mythicrpg.skill;

import com.woxloi.mythicrpg.job.JobType;
import com.woxloi.mythicrpg.skill.loader.SkillLoader;

import java.util.List;

/**
 * スキルの取得窓口。
 * 実体は SkillLoader（YAMLファイルベース）に委譲する。
 */
public class SkillRegistry {

    public static List<Skill> getSkills(JobType job) {
        return SkillLoader.getSkills(job);
    }

    public static Skill getSkill(JobType job, String id) {
        return SkillLoader.getSkill(job, id);
    }
}
