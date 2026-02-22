package com.woxloi.mythicrpg.stats;

import java.util.UUID;

/**
 * プレイヤーのステータスポイント振り分け状態
 * レベルアップ時にポイントが加算され、好きなステータスに振り分けられる
 */
public class StatPoint {

    private final UUID uuid;
    private int freePoints;  // 未割り振りポイント

    // 振り分け済みポイント
    private int strPoints;   // 筋力 → ATK UP
    private int vitPoints;   // 体力 → MaxHP UP
    private int intPoints;   // 知力 → MaxMP UP
    private int agiPoints;   // 俊敏 → MaxSP + 速度UP

    /** レベルアップ時に付与するポイント数 */
    public static final int POINTS_PER_LEVEL = 3;

    public StatPoint(UUID uuid) {
        this.uuid = uuid;
    }

    // ─── 割り振り ───

    /**
     * 指定ステータスにn点振り分ける
     * @return 成功したかどうか
     */
    public boolean allocate(StatType stat, int points) {
        if (freePoints < points || points <= 0) return false;
        freePoints -= points;
        switch (stat) {
            case STR -> strPoints += points;
            case VIT -> vitPoints += points;
            case INT -> intPoints += points;
            case AGI -> agiPoints += points;
        }
        return true;
    }

    /**
     * リセット（全ポイントを未割り振りに戻す）
     */
    public void reset() {
        freePoints += strPoints + vitPoints + intPoints + agiPoints;
        strPoints = vitPoints = intPoints = agiPoints = 0;
    }

    // ─── レベルアップ時ポイント付与 ───
    public void addPoints(int amount) { freePoints += amount; }

    // ─── ステータス計算ヘルパー ───

    /** STR 1点 = 攻撃力 +5 */
    public double getBonusAttack() { return strPoints * 5.0; }

    /** VIT 1点 = MaxHP +20 */
    public double getBonusMaxHp()  { return vitPoints * 20.0; }

    /** INT 1点 = MaxMP +15 */
    public double getBonusMaxMp()  { return intPoints * 15.0; }

    /** AGI 1点 = MaxSP +10 */
    public double getBonusMaxSp()  { return agiPoints * 10.0; }

    // ─── Getters / Setters ───
    public UUID getUuid()       { return uuid; }
    public int getFreePoints()  { return freePoints; }
    public int getStrPoints()   { return strPoints; }
    public int getVitPoints()   { return vitPoints; }
    public int getIntPoints()   { return intPoints; }
    public int getAgiPoints()   { return agiPoints; }

    public void setFreePoints(int v) { freePoints = v; }
    public void setStrPoints(int v)  { strPoints  = v; }
    public void setVitPoints(int v)  { vitPoints  = v; }
    public void setIntPoints(int v)  { intPoints  = v; }
    public void setAgiPoints(int v)  { agiPoints  = v; }
}
