package com.woxloi.mythicrpg.combo;

/** 1回の攻撃をコンボ履歴として記録する。 */
public class ComboEntry {

    public final ComboType type;
    public final long      timestamp;
    public final double    damage;

    public ComboEntry(ComboType type, double damage) {
        this.type      = type;
        this.timestamp = System.currentTimeMillis();
        this.damage    = damage;
    }

    /** コンボ有効期間内かどうか（デフォルト3秒） */
    public boolean isValid(long windowMillis) {
        return System.currentTimeMillis() - timestamp < windowMillis;
    }
}
