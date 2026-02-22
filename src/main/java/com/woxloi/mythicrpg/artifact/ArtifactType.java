package com.woxloi.mythicrpg.artifact;

/**
 * アーティファクト（装備品セット）の種別定義。
 *
 * 各アーティファクトは「名前付き装備セット」に属し、
 * 2点・4点・6点装備でセットボーナスが発動する。
 *
 * セット構成例:
 *   DRAGON_SLAYER → 炎の剣 + 竜鱗の鎧 + ... を揃えるとボーナス発動
 */
public enum ArtifactType {

    // ━━━━━━━━━ 攻撃特化 ━━━━━━━━━
    DRAGON_SLAYER("§4§l竜殺しの業物",
            "古代の竜を倒した英雄の装備セット",
            new int[]{2, 4, 6},
            new String[]{
                    "ATK +15%",
                    "炎ダメージ +25% / 竜属性ダメージ +50%",
                    "「竜の咆哮」スキル解放: 周囲の敵に爆発ダメージ"
            }),

    SHADOW_ASSASSIN("§8§l影の暗殺者",
            "闇に生きる刺客の装備セット",
            new int[]{2, 4, 6},
            new String[]{
                    "クリティカル率 +10%",
                    "背後攻撃ダメージ +40% / 移動速度 +10%",
                    "「影潜り」スキル解放: 3秒間透明化 + 次の攻撃が2倍ダメージ"
            }),

    STORM_ARCHER("§b§l嵐の射手",
            "風を操る弓使いの装備セット",
            new int[]{2, 4},
            new String[]{
                    "遠距離ダメージ +20%",
                    "多段射撃: 矢が貫通して2体まで命中"
            }),

    // ━━━━━━━━━ 防御特化 ━━━━━━━━━
    IRON_FORTRESS("§7§l鉄壁の要塞",
            "鉄壁の防衛者が纏う装備セット",
            new int[]{2, 4, 6},
            new String[]{
                    "MaxHP +200",
                    "受ダメージ -15% / ノックバック無効",
                    "「鉄壁」スキル解放: 10秒間ダメージ90%軽減"
            }),

    EARTH_GUARDIAN("§a§l大地の守護者",
            "大地の力を宿した守護者の装備セット",
            new int[]{2, 4},
            new String[]{
                    "MaxHP +300 / 自然回復 +5HP/秒",
                    "木・土属性ダメージ免疫"
            }),

    // ━━━━━━━━━ 魔法特化 ━━━━━━━━━
    ARCANE_SCHOLAR("§d§l秘術の学者",
            "古代魔法を極めた学者の装備セット",
            new int[]{2, 4, 6},
            new String[]{
                    "MaxMP +100 / MP回復 +3/秒",
                    "スキルダメージ +30% / スキルコスト -20%",
                    "「魔力解放」スキル解放: 全スキルのクールダウンリセット"
            }),

    FROST_WITCH("§3§l霜の魔女",
            "氷の魔法を操る魔女の装備セット",
            new int[]{2, 4},
            new String[]{
                    "氷属性ダメージ +35%",
                    "攻撃時15%で凍結付与 (3秒)"
            }),

    // ━━━━━━━━━ バランス型 ━━━━━━━━━
    HERO_OF_LIGHT("§e§l光の勇者",
            "光の加護を受けた勇者の装備セット",
            new int[]{2, 4, 6},
            new String[]{
                    "ATK +10% / MaxHP +100",
                    "EXP獲得量 +20% / 全ステータス +5%",
                    "「光輝爆発」スキル解放: 周囲を浄化し、味方を回復させる"
            }),

    WANDERER("§6§l旅人の知恵",
            "各地を旅した冒険者の装備セット",
            new int[]{2, 4},
            new String[]{
                    "移動速度 +15% / 落下ダメージ無効",
                    "クエストEXP報酬 +30%"
            }),

    // ━━━━━━━━━ 特殊 ━━━━━━━━━
    ANCIENT_KING("§5§l古代王の遺産",
            "失われた古代王国の王が纏った伝説の装備セット",
            new int[]{2, 4, 6},
            new String[]{
                    "全ステータス +10%",
                    "死亡時20%で復活 (HP50%回復) / コンボ倍率 +0.5",
                    "「王の威光」スキル解放: 30秒間、全パーティーメンバーの攻撃力2倍"
            });

    // ━━━━━━━━━ フィールド ━━━━━━━━━
    private final String displayName;
    private final String description;
    /** セットボーナス発動に必要な装備数（例: {2, 4, 6}） */
    private final int[]    piecesRequired;
    /** piecesRequired[i] 個装備時のボーナス説明 */
    private final String[] bonusDescriptions;

    ArtifactType(String displayName, String description,
                 int[] piecesRequired, String[] bonusDescriptions) {
        this.displayName      = displayName;
        this.description      = description;
        this.piecesRequired   = piecesRequired;
        this.bonusDescriptions = bonusDescriptions;
    }

    public String   getDisplayName()       { return displayName; }
    public String   getDescription()       { return description; }
    public int[]    getPiecesRequired()    { return piecesRequired; }
    public String[] getBonusDescriptions() { return bonusDescriptions; }

    /**
     * 装備数に応じて何段階目のボーナスが有効かを返す。
     * 0 = ボーナスなし、1 = 第1段階、...
     */
    public int getActiveTier(int equippedCount) {
        int tier = 0;
        for (int req : piecesRequired) {
            if (equippedCount >= req) tier++;
        }
        return tier;
    }
}
