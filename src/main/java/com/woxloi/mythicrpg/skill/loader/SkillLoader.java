package com.woxloi.mythicrpg.skill.loader;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.job.JobType;
import com.woxloi.mythicrpg.skill.ResourceType;
import com.woxloi.mythicrpg.skill.Skill;
import com.woxloi.mythicrpg.skill.SkillTrigger;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * plugins/MythicRPG/skills/<job>.yml からスキルを読み込む。
 * ファイルが存在しない場合は jar 内のデフォルトをコピーして使う。
 */
public class SkillLoader {

    private static final Map<JobType, List<Skill>> loadedSkills = new HashMap<>();

    public static void load() {
        loadedSkills.clear();
        MythicRPG plugin = MythicRPG.getInstance();

        for (JobType job : JobType.values()) {
            String fileName = "skills/" + job.name().toLowerCase() + ".yml";
            File file = new File(plugin.getDataFolder(), fileName);

            // 存在しなければデフォルトをコピー
            if (!file.exists()) {
                plugin.saveResource(fileName, false);
            }

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

            // jar内のデフォルトをフォールバックとして設定
            InputStream defaultStream = plugin.getResource(fileName);
            if (defaultStream != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                yaml.setDefaults(defaults);
            }

            List<Skill> skills = new ArrayList<>();
            var section = yaml.getConfigurationSection("skills");
            if (section == null) {
                MythicLogger.warn(fileName + " に skills セクションがありません");
                loadedSkills.put(job, skills);
                continue;
            }

            for (String id : section.getKeys(false)) {
                var s = section.getConfigurationSection(id);
                if (s == null) continue;

                try {
                    String name        = s.getString("name", id);
                    int    unlockLevel = s.getInt("unlock-level", 1);
                    long   cooldown    = s.getLong("cooldown", 0);
                    String triggerStr  = s.getString("trigger", "LEFT_CLICK");
                    String resourceStr = s.getString("resource", "NONE");
                    double cost        = s.getDouble("cost", 0);
                    String effect      = s.getString("effect", "basic_attack");
                    double damageMult  = s.getDouble("damage-multiplier", 1.0);
                    double fixedDmg    = s.getDouble("damage", 0);
                    double range       = s.getDouble("range", 3.0);
                    String weapon      = s.getString("weapon", "ANY");

                    SkillTrigger trigger;
                    try { trigger = SkillTrigger.valueOf(triggerStr); }
                    catch (IllegalArgumentException e) { trigger = SkillTrigger.LEFT_CLICK; }

                    ResourceType resource;
                    try { resource = ResourceType.valueOf(resourceStr); }
                    catch (IllegalArgumentException e) { resource = ResourceType.NONE; }

                    skills.add(new YamlSkill(
                            id, name, unlockLevel, cooldown,
                            trigger, resource, cost,
                            effect, damageMult, fixedDmg, range, weapon
                    ));

                    MythicLogger.debug("スキルロード: [" + job.name() + "] " + id);

                } catch (Exception e) {
                    MythicLogger.warn("スキル読み込み失敗 [" + job.name() + "/" + id + "]: " + e.getMessage());
                }
            }

            loadedSkills.put(job, skills);
            MythicLogger.info("[" + job.getDisplayName() + "] スキル " + skills.size() + "件ロード");
        }
    }

    public static List<Skill> getSkills(JobType job) {
        return loadedSkills.getOrDefault(job, List.of());
    }

    public static Skill getSkill(JobType job, String id) {
        return getSkills(job).stream()
                .filter(s -> s.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }
}
