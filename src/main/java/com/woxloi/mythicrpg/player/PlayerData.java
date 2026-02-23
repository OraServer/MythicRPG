package com.woxloi.mythicrpg.player;

import com.woxloi.mythicrpg.job.JobType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private int level;
    private double exp;
    private JobType job;

    private double maxHp, hp, attack;
    private double maxMp, mp;
    private double maxSp, sp;

    private final Set<String> unlockedSkills = new HashSet<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.exp = 0;
    }

    public UUID getUuid() { return uuid; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public double getExp() { return exp; }
    public void setExp(double exp) { this.exp = exp; }
    public void addExp(double amount) { this.exp += amount; }

    public JobType getJob() { return job; }
    public void setJob(JobType job) { this.job = job; }
    public boolean hasJob() { return job != null; }

    public double getMaxHp() { return maxHp; }
    public double getHp() { return hp; }
    public void setMaxHp(double v) { this.maxHp = Math.max(0, v); this.hp = this.maxHp; }
    public void setHp(double v) { this.hp = Math.max(0, Math.min(maxHp, v)); }
    public void addHp(double v) { setHp(hp + v); }
    public boolean consumeHp(double v) { if (hp < v) return false; hp -= v; return true; }

    public double getAttack() { return attack; }
    public void setAttack(double v) { this.attack = v; }

    public double getMaxMp() { return maxMp; }
    public double getMp() { return mp; }
    public void setMaxMp(double v) { this.maxMp = Math.max(0, v); this.mp = this.maxMp; }
    public void setMp(double v) { this.mp = Math.max(0, Math.min(maxMp, v)); }
    public void addMp(double v) { setMp(mp + v); }
    public boolean consumeMp(double v) { if (mp < v) return false; mp -= v; return true; }

    public double getMaxSp() { return maxSp; }
    public double getSp() { return sp; }
    public void setMaxSp(double v) { this.maxSp = Math.max(0, v); this.sp = this.maxSp; }
    public void setSp(double v) { this.sp = Math.max(0, Math.min(maxSp, v)); }
    public void addSp(double v) { setSp(sp + v); }
    public boolean consumeSp(double v) { if (sp < v) return false; sp -= v; return true; }

    public Set<String> getUnlockedSkills() { return unlockedSkills; }
    public boolean hasSkill(String id) { return unlockedSkills.contains(id); }
    public void unlockSkill(String id) { unlockedSkills.add(id); }

    // ──────────────────────────────────────────
    //  アーティファクトセットボーナス用フィールド
    // ──────────────────────────────────────────

    /** アーティファクト由来のATK加算値 */
    private int artifactBonusAtk  = 0;
    /** アーティファクト由来のDEF加算値 */
    private int artifactBonusDef  = 0;
    /** アーティファクト由来のMaxHP加算値 */
    private int artifactBonusMaxHp = 0;
    /** アーティファクト由来のMaxMP加算値 */
    private int artifactBonusMaxMp = 0;

    /** セット枚数キャッシュ (ArtifactType → 枚数) */
    private java.util.Map<com.woxloi.mythicrpg.artifact.ArtifactType, Integer> artifactSetCounts
            = new java.util.EnumMap<>(com.woxloi.mythicrpg.artifact.ArtifactType.class);

    public void addBonusAtk(int v)   { this.artifactBonusAtk   += v; }
    public void addBonusDef(int v)   { this.artifactBonusDef   += v; }
    public void addBonusMaxHp(int v) { this.artifactBonusMaxHp += v; }
    public void addBonusMp(int v)    { this.artifactBonusMaxMp += v; }

    public int getArtifactBonusAtk()   { return artifactBonusAtk; }
    public int getArtifactBonusDef()   { return artifactBonusDef; }
    public int getArtifactBonusMaxHp() { return artifactBonusMaxHp; }
    public int getArtifactBonusMaxMp() { return artifactBonusMaxMp; }

    /** アーティファクトボーナスを全リセット */
    public void clearArtifactBonuses() {
        artifactBonusAtk   = 0;
        artifactBonusDef   = 0;
        artifactBonusMaxHp = 0;
        artifactBonusMaxMp = 0;
    }

    /** HP上限をアーティファクトボーナス込みで再適用 */
    public void applyMaxHp() {
        // アーティファクトボーナスはgetTotalMaxHp()で合算して参照する
        // HPが新しい上限を超えていたらクランプ
        double totalMax = getTotalMaxHp();
        if (this.hp > totalMax) this.hp = totalMax;
    }

    /** MP上限をアーティファクトボーナス込みで再適用 */
    public void applyMaxMp() {
        double totalMax = getTotalMaxMp();
        if (this.mp > totalMax) this.mp = totalMax;
    }

    /**
     * アーティファクトボーナス + 装備ボーナス込みの実効MaxHP。
     * スコアボード・アクションバー・Bukkit属性設定で使う。
     */
    public double getTotalMaxHp() {
        return maxHp + artifactBonusMaxHp + equipMaxHpBonus;
    }

    /**
     * アーティファクトボーナス + 装備ボーナス込みの実効MaxMP。
     */
    public double getTotalMaxMp() {
        return maxMp + artifactBonusMaxMp + equipMaxMpBonus;
    }

    public java.util.Map<com.woxloi.mythicrpg.artifact.ArtifactType, Integer> getArtifactSetCounts() {
        return artifactSetCounts;
    }
    public void setArtifactSetCounts(java.util.Map<com.woxloi.mythicrpg.artifact.ArtifactType, Integer> counts) {
        this.artifactSetCounts = counts;
    }

    // ──────────────────────────────────────────
    //  装備ステータスボーナス用フィールド
    // ──────────────────────────────────────────

    private double equipAttackBonus;
    private double equipDefenseBonus;
    private double equipMaxHpBonus;
    private double equipMaxMpBonus;
    private double equipMaxSpBonus;
    private double equipCritRate;
    private double equipCritDamage;
    private double equipMagicPower;

    public double getEquipAttackBonus()  { return equipAttackBonus; }
    public double getEquipDefenseBonus() { return equipDefenseBonus; }
    public double getEquipMaxHpBonus()   { return equipMaxHpBonus; }
    public double getEquipMaxMpBonus()   { return equipMaxMpBonus; }
    public double getEquipMaxSpBonus()   { return equipMaxSpBonus; }
    public double getEquipCritRate()     { return equipCritRate; }
    public double getEquipCritDamage()   { return equipCritDamage; }
    public double getEquipMagicPower()   { return equipMagicPower; }

    public void setEquipAttackBonus(double v)  { this.equipAttackBonus  = v; }
    public void setEquipDefenseBonus(double v) { this.equipDefenseBonus = v; }
    public void setEquipMaxHpBonus(double v)   { this.equipMaxHpBonus   = v; }
    public void setEquipMaxMpBonus(double v)   { this.equipMaxMpBonus   = v; }
    public void setEquipMaxSpBonus(double v)   { this.equipMaxSpBonus   = v; }
    public void setEquipCritRate(double v)     { this.equipCritRate     = v; }
    public void setEquipCritDamage(double v)   { this.equipCritDamage   = v; }
    public void setEquipMagicPower(double v)   { this.equipMagicPower   = v; }
}
