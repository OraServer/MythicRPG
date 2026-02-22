package com.woxloi.mythicrpg.combo;

import java.util.UUID;

/**
 * プレイヤーのコンボ状態
 */
public class ComboState {

    private final UUID uuid;
    private int comboCount;
    private long lastHitTime;   // ms
    private static final long COMBO_TIMEOUT_MS = 3000; // 3秒でリセット

    public ComboState(UUID uuid) {
        this.uuid = uuid;
        this.comboCount = 0;
        this.lastHitTime = 0;
    }

    /** 攻撃ヒット時に呼ぶ */
    public void hit() {
        long now = System.currentTimeMillis();
        if (now - lastHitTime > COMBO_TIMEOUT_MS) {
            comboCount = 0; // タイムアウトでリセット
        }
        comboCount++;
        lastHitTime = now;
    }

    /** コンボリセット */
    public void reset() {
        comboCount = 0;
        lastHitTime = 0;
    }

    /** タイムアウトチェック */
    public boolean isTimedOut() {
        return comboCount > 0 && System.currentTimeMillis() - lastHitTime > COMBO_TIMEOUT_MS;
    }

    /**
     * コンボ倍率を返す
     * 1-4 hit: x1.0
     * 5-9 hit: x1.2
     * 10-19 hit: x1.5
     * 20+ hit: x2.0
     */
    public double getDamageMultiplier() {
        if (comboCount >= 20) return 2.0;
        if (comboCount >= 10) return 1.5;
        if (comboCount >= 5)  return 1.2;
        return 1.0;
    }

    public int getComboCount()   { return comboCount; }
    public long getLastHitTime() { return lastHitTime; }
    public UUID getUuid()        { return uuid; }
}
