package com.woxloi.mythicrpg.pet;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.job.JobType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * pets.yml からペット定義を読み込むローダー。
 */
public class PetLoader {

    private PetLoader() {}

    public static Map<String, PetDefinition> load() {
        Map<String, PetDefinition> result = new LinkedHashMap<>();

        File file = new File(MythicRPG.getInstance().getDataFolder(), "pets.yml");
        if (!file.exists()) MythicRPG.getInstance().saveResource("pets.yml", false);

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        InputStream def = MythicRPG.getInstance().getResource("pets.yml");
        if (def != null) {
            yaml.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(def, StandardCharsets.UTF_8)));
        }

        ConfigurationSection pets = yaml.getConfigurationSection("pets");
        if (pets == null) { MythicLogger.warn("pets.yml に pets セクションがありません"); return result; }

        for (String id : pets.getKeys(false)) {
            ConfigurationSection sec = pets.getConfigurationSection(id);
            if (sec == null) continue;
            try {
                result.put(id, parsePet(id, sec));
            } catch (Exception e) {
                MythicLogger.warn("ペット [" + id + "] 読み込み失敗: " + e.getMessage());
            }
        }

        MythicLogger.info("ペット読み込み完了: " + result.size() + "件");
        return result;
    }

    private static PetDefinition parsePet(String id, ConfigurationSection sec) {
        String display     = sec.getString("display", id);
        String description = sec.getString("description", "");
        String mythicId    = sec.getString("mythicmob-id", id);
        int    reqLevel    = sec.getInt("required-level", 1);
        int    maxLevel    = sec.getInt("max-level", 30);
        double hpPer       = sec.getDouble("hp-per-level", 8.0);
        double atkPer      = sec.getDouble("attack-per-level", 3.0);
        double defPer      = sec.getDouble("defense-per-level", 1.5);
        List<String> skills = sec.getStringList("skills");

        // allowed-jobs: "ALL" か JobType名リスト
        Set<JobType> allowedJobs;
        Object jobsObj = sec.get("allowed-jobs");
        if ("ALL".equalsIgnoreCase(String.valueOf(jobsObj))) {
            allowedJobs = EnumSet.allOf(JobType.class);
        } else {
            List<String> jobList = sec.getStringList("allowed-jobs");
            Set<JobType> parsed = EnumSet.noneOf(JobType.class);
            for (String j : jobList) {
                try { parsed.add(JobType.valueOf(j.toUpperCase())); }
                catch (IllegalArgumentException ignored) {}
            }
            allowedJobs = parsed.isEmpty() ? EnumSet.allOf(JobType.class) : parsed;
        }

        return new PetDefinition(id, display, description, mythicId, reqLevel,
                allowedJobs, maxLevel, skills, hpPer, atkPer, defPer);
    }
}
