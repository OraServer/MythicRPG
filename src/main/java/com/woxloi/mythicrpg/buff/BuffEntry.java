package com.woxloi.mythicrpg.buff;

/**
 * 1つのバフ/デバフインスタンス
 */
public class BuffEntry {

    private final BuffType type;
    private final double magnitude;   // 効果量 (例: ATK_UP → 1.2 = 20%増)
    private int remainingTicks;       // 残り時間(tick)
    private final String source;      // 付与元 (スキルIDや "potion" など)

    public BuffEntry(BuffType type, double magnitude, int durationTicks, String source) {
        this.type = type;
        this.magnitude = magnitude;
        this.remainingTicks = durationTicks;
        this.source = source;
    }

    /** 1ティック経過 → 残時間を減らす */
    public void tick() { remainingTicks--; }

    public boolean isExpired() { return remainingTicks <= 0; }

    public BuffType getType()         { return type; }
    public double getMagnitude()      { return magnitude; }
    public int getRemainingTicks()    { return remainingTicks; }
    public String getSource()         { return source; }

    /** 残り秒数 */
    public int getRemainingSeconds()  { return remainingTicks / 20; }

    @Override
    public String toString() {
        return type.getDisplayName() + " x" + magnitude + " (" + getRemainingSeconds() + "s)";
    }
}
