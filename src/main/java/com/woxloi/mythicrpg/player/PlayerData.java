package com.woxloi.mythicrpg.player;

import com.woxloi.mythicrpg.job.JobType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerData {

    /* =====================
       基本情報
     ===================== */
    private final UUID uuid;

    private int level;
    private double exp;

    private JobType job;

    /* =====================
       ステータス
     ===================== */
    private double maxHp;
    private double hp;

    private double attack;

    /* =====================
       リソース
     ===================== */
    private double maxMp;
    private double mp;

    private double maxSp;
    private double sp;

    /* =====================
       スキル
     ===================== */
    private final Set<String> unlockedSkills = new HashSet<>();

    /* =====================
       コンストラクタ
     ===================== */
    public PlayerData(UUID uuid) {
        this.uuid = uuid;

        this.level = 1;
        this.exp = 0;

        this.job = null;

        this.maxHp = 0;
        this.hp = 0;
        this.attack = 0;

        this.maxMp = 0;
        this.mp = 0;

        this.maxSp = 0;
        this.sp = 0;
    }

    /* =====================
       UUID
     ===================== */
    public UUID getUuid() {
        return uuid;
    }

    /* =====================
       Level / EXP
     ===================== */
    public int getLevel() {
        return level;
    }

    public double getExp() {
        return exp;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public void addExp(double amount) {
        this.exp += amount;
    }

    /* =====================
       Job
     ===================== */
    public JobType getJob() {
        return job;
    }

    public void setJob(JobType job) {
        this.job = job;
    }

    public boolean hasJob() {
        return job != null;
    }

    /* =====================
       HP
     ===================== */
    public double getMaxHp() {
        return maxHp;
    }

    public double getHp() {
        return hp;
    }

    public void setMaxHp(double maxHp) {
        this.maxHp = Math.max(0, maxHp);
        this.hp = this.maxHp;
    }

    /** Job再計算・LvUP用 */
    public void setHp(double hp) {
        this.hp = Math.max(0, Math.min(maxHp, hp));
    }

    public void addHp(double value) {
        setHp(this.hp + value);
    }

    public boolean consumeHp(double value) {
        if (hp < value) return false;
        hp -= value;
        return true;
    }

    /* =====================
       Attack
     ===================== */
    public double getAttack() {
        return attack;
    }

    public void setAttack(double attack) {
        this.attack = attack;
    }

    public void addAttack(double value) {
        this.attack += value;
    }

    /* =====================
       MP
     ===================== */
    public double getMaxMp() {
        return maxMp;
    }

    public double getMp() {
        return mp;
    }

    public void setMaxMp(double maxMp) {
        this.maxMp = Math.max(0, maxMp);
        this.mp = this.maxMp;
    }

    /** Job再計算・LvUP用 */
    public void setMp(double mp) {
        this.mp = Math.max(0, Math.min(maxMp, mp));
    }

    public void addMp(double value) {
        setMp(this.mp + value);
    }

    public boolean consumeMp(double value) {
        if (mp < value) return false;
        mp -= value;
        return true;
    }

    /* =====================
       SP
     ===================== */
    public double getMaxSp() {
        return maxSp;
    }

    public double getSp() {
        return sp;
    }

    public void setMaxSp(double maxSp) {
        this.maxSp = Math.max(0, maxSp);
        this.sp = this.maxSp;
    }

    /** Job再計算・LvUP用 */
    public void setSp(double sp) {
        this.sp = Math.max(0, Math.min(maxSp, sp));
    }

    public void addSp(double value) {
        setSp(this.sp + value);
    }

    public boolean consumeSp(double value) {
        if (sp < value) return false;
        sp -= value;
        return true;
    }

    /* =====================
       Skill
     ===================== */
    public Set<String> getUnlockedSkills() {
        return unlockedSkills;
    }

    public boolean hasSkill(String skillId) {
        return unlockedSkills.contains(skillId);
    }

    public void unlockSkill(String skillId) {
        unlockedSkills.add(skillId);
    }
}
