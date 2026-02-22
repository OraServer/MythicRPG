package com.woxloi.mythicrpg.pet;

import java.util.UUID;

/**
 * プレイヤーが所持するペットの状態データ。
 * PlayerDataとは別に管理し、DB保存の対象。
 */
public class PetData {

    private final UUID ownerUuid;
    private final String petDefinitionId;

    private int level;
    private double exp;
    private double currentHp;
    private boolean summoned;

    /** 召喚中のMythicMob EntityのUUID（ログアウト等でクリア） */
    private UUID summonedEntityUuid;

    public PetData(UUID ownerUuid, String petDefinitionId) {
        this.ownerUuid         = ownerUuid;
        this.petDefinitionId   = petDefinitionId;
        this.level             = 1;
        this.exp               = 0;
        this.currentHp         = -1; // -1 = 満タン（定義から計算）
        this.summoned          = false;
        this.summonedEntityUuid = null;
    }

    /**
     * 経験値を加算し、レベルアップ判定を行う。
     * @return true = レベルアップした
     */
    public boolean addExp(PetDefinition def, double amount) {
        exp += amount;
        double required = getRequiredExp();
        if (exp >= required && level < def.getMaxPetLevel()) {
            exp -= required;
            level++;
            currentHp = def.calcHp(level); // レベルアップでHP全回復
            return true;
        }
        return false;
    }

    /** 次のレベルに必要な経験値 */
    public double getRequiredExp() {
        return 50.0 * Math.pow(level, 1.3);
    }

    /** HPダメージを受ける。0以下になったらtrue（倒された） */
    public boolean takeDamage(PetDefinition def, double damage) {
        if (currentHp < 0) currentHp = def.calcHp(level);
        currentHp -= damage;
        return currentHp <= 0;
    }

    /** HP全回復 */
    public void healFull(PetDefinition def) {
        currentHp = def.calcHp(level);
    }

    // ─── Getters / Setters ────────────────────────

    public UUID getOwnerUuid()           { return ownerUuid; }
    public String getPetDefinitionId()   { return petDefinitionId; }
    public int getLevel()                { return level; }
    public void setLevel(int level)      { this.level = level; }
    public double getExp()               { return exp; }
    public void setExp(double exp)       { this.exp = exp; }
    public double getCurrentHp()         { return currentHp; }
    public void setCurrentHp(double hp)  { this.currentHp = hp; }
    public boolean isSummoned()          { return summoned; }
    public void setSummoned(boolean s)   { this.summoned = s; }
    public UUID getSummonedEntityUuid()  { return summonedEntityUuid; }
    public void setSummonedEntityUuid(UUID uuid) { this.summonedEntityUuid = uuid; }

    /** 経験値バーの表示文字列 */
    public String getExpDisplay() {
        double required = getRequiredExp();
        int pct = (int)(exp / required * 100);
        return "§bLv" + level + " §7EXP: §e" + (int)exp + "§7/§e" + (int)required
                + " §8(§e" + pct + "%§8)";
    }
}
