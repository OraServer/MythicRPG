package com.woxloi.mythicrpg.buff;

/**
 * バフ/デバフの種類
 */
public enum BuffType {

    // ─── バフ ───
    ATK_UP("攻撃力上昇",     true,  true),
    DEF_UP("防御力上昇",     true,  true),
    SPEED_UP("移動速度上昇", true,  true),
    REGEN_HP("HP自動回復",   true,  true),
    REGEN_MP("MP自動回復",   true,  true),
    REGEN_SP("SP自動回復",   true,  true),
    EXP_BOOST("EXP倍率上昇", true,  true),
    CRIT_UP("クリティカル率上昇", true, true),

    // ─── デバフ ───
    ATK_DOWN("攻撃力低下",    false, true),
    DEF_DOWN("防御力低下",    false, true),
    SPEED_DOWN("移動速度低下",false, true),
    POISON("毒",              false, true),
    BURN("燃焼",              false, true),
    FREEZE("凍結",            false, false),  // スタック不可
    BLIND("暗闇",             false, false),
    SILENCE("沈黙（スキル封印）", false, false),
    STUN("スタン",            false, false);

    private final String displayName;
    private final boolean isBuff;
    private final boolean stackable;

    BuffType(String displayName, boolean isBuff, boolean stackable) {
        this.displayName = displayName;
        this.isBuff = isBuff;
        this.stackable = stackable;
    }

    public String getDisplayName() { return displayName; }
    public boolean isBuff()        { return isBuff; }
    public boolean isDebuff()      { return !isBuff; }
    public boolean isStackable()   { return stackable; }
}
