package com.woxloi.mythicrpg.equipment.model;

/** 装備品が持つステータスボーナス（加算値）。 */
public class EquipStats {
    public double attack      = 0;
    public double defense     = 0;
    public double maxHpBonus  = 0;
    public double maxMpBonus  = 0;
    public double maxSpBonus  = 0;
    public double critRate    = 0;    // 0.0〜1.0
    public double critDamage  = 0;    // 倍率加算 (0.5 = +50%)
    public double magicPower  = 0;
    public double speed       = 0;    // 移動速度加算

    /** 2つのEquipStatsを合算して新しいオブジェクトを返す */
    public EquipStats add(EquipStats other) {
        EquipStats r = new EquipStats();
        r.attack      = this.attack      + other.attack;
        r.defense     = this.defense     + other.defense;
        r.maxHpBonus  = this.maxHpBonus  + other.maxHpBonus;
        r.maxMpBonus  = this.maxMpBonus  + other.maxMpBonus;
        r.maxSpBonus  = this.maxSpBonus  + other.maxSpBonus;
        r.critRate    = this.critRate    + other.critRate;
        r.critDamage  = this.critDamage  + other.critDamage;
        r.magicPower  = this.magicPower  + other.magicPower;
        r.speed       = this.speed       + other.speed;
        return r;
    }

    /** 倍率をかけたコピーを返す（レアリティ・強化計算用） */
    public EquipStats multiply(double factor) {
        EquipStats r = new EquipStats();
        r.attack     = this.attack     * factor;
        r.defense    = this.defense    * factor;
        r.maxHpBonus = this.maxHpBonus * factor;
        r.maxMpBonus = this.maxMpBonus * factor;
        r.maxSpBonus = this.maxSpBonus * factor;
        r.critRate   = this.critRate   * factor;
        r.critDamage = this.critDamage * factor;
        r.magicPower = this.magicPower * factor;
        r.speed      = this.speed      * factor;
        return r;
    }
}
