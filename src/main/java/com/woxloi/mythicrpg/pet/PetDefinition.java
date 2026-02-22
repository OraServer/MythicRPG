package com.woxloi.mythicrpg.pet;

import com.woxloi.mythicrpg.job.JobType;

import java.util.List;
import java.util.Set;

/**
 * ペット定義データ。
 * MythicMobsのMob IDと連携し、召喚時にそのMobを生成する。
 */
public class PetDefinition {

    private final String id;
    private final String displayName;
    private final String description;
    private final String mythicMobId;    // MythicMobs で定義されたMob ID
    private final int requiredLevel;     // 召喚可能な最低プレイヤーレベル
    private final Set<JobType> allowedJobs;
    private final int maxPetLevel;
    private final List<String> skills;  // ペットが使えるスキルID

    // 成長パラメータ（ペットレベルごとの加算値）
    private final double hpPerLevel;
    private final double attackPerLevel;
    private final double defensePerLevel;

    public PetDefinition(String id, String displayName, String description,
                          String mythicMobId, int requiredLevel,
                          Set<JobType> allowedJobs, int maxPetLevel,
                          List<String> skills,
                          double hpPerLevel, double attackPerLevel, double defensePerLevel) {
        this.id             = id;
        this.displayName    = displayName;
        this.description    = description;
        this.mythicMobId    = mythicMobId;
        this.requiredLevel  = requiredLevel;
        this.allowedJobs    = allowedJobs;
        this.maxPetLevel    = maxPetLevel;
        this.skills         = skills;
        this.hpPerLevel     = hpPerLevel;
        this.attackPerLevel = attackPerLevel;
        this.defensePerLevel = defensePerLevel;
    }

    /** ペットレベルに応じたHP */
    public double calcHp(int petLevel) {
        return 20.0 + hpPerLevel * petLevel;
    }

    /** ペットレベルに応じた攻撃力 */
    public double calcAttack(int petLevel) {
        return 5.0 + attackPerLevel * petLevel;
    }

    /** ペットレベルに応じた防御力 */
    public double calcDefense(int petLevel) {
        return 2.0 + defensePerLevel * petLevel;
    }

    /** GUIのlore */
    public List<String> buildLore(int petLevel) {
        return List.of(
            "§7" + description,
            "",
            "§8--- ステータス (Lv" + petLevel + ") ---",
            "§aHP: §f" + (int)calcHp(petLevel),
            "§cATK: §f" + (int)calcAttack(petLevel),
            "§7DEF: §f" + (int)calcDefense(petLevel),
            "§7最大Lv: §e" + maxPetLevel,
            "",
            "§8--- スキル ---",
            skills.isEmpty() ? "§8なし" : "§e" + String.join("§7, §e", skills)
        );
    }

    public String getId()              { return id; }
    public String getDisplayName()     { return displayName; }
    public String getDescription()     { return description; }
    public String getMythicMobId()     { return mythicMobId; }
    public int getRequiredLevel()      { return requiredLevel; }
    public Set<JobType> getAllowedJobs() { return allowedJobs; }
    public int getMaxPetLevel()        { return maxPetLevel; }
    public List<String> getSkills()    { return skills; }
}
