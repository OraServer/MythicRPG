package com.woxloi.mythicrpg.title;

import com.woxloi.mythicrpg.job.JobType;

/**
 * 称号の定義
 * 条件: レベル・ジョブ・クエスト完了数・Mob討伐数 など
 */
public enum TitleDefinition {

    // ─── 初心者 ───
    NEWCOMER("§7[初心者]",     "冒険を始めた者",     TitleCondition.LEVEL, 1),
    ADVENTURER("§a[冒険者]",   "レベル10到達",        TitleCondition.LEVEL, 10),
    VETERAN("§b[ベテラン]",    "レベル30到達",        TitleCondition.LEVEL, 30),
    HERO("§6[英雄]",           "レベル50到達",        TitleCondition.LEVEL, 50),
    LEGEND("§5[伝説]",         "レベル99到達",        TitleCondition.LEVEL, 99),

    // ─── 職業 ───
    WARRIOR_TITLE("§c[剣士]",       "剣士を選択",          TitleCondition.JOB, 0),
    MAGE_TITLE("§9[魔法使い]",      "魔法使いを選択",      TitleCondition.JOB, 0),
    ARCHER_TITLE("§2[弓使い]",      "弓使いを選択",        TitleCondition.JOB, 0),

    // ─── 討伐数 ───
    SLAYER_10("§e[スレイヤー]",     "Mobを10体討伐",       TitleCondition.MOB_KILL, 10),
    SLAYER_100("§6[百殺者]",        "Mobを100体討伐",      TitleCondition.MOB_KILL, 100),
    SLAYER_1000("§c[千殺者]",       "Mobを1000体討伐",     TitleCondition.MOB_KILL, 1000),

    // ─── クエスト ───
    QUEST_NOVICE("§7[クエスト初心者]", "クエストを1回完了",  TitleCondition.QUEST_COMPLETE, 1),
    QUEST_MASTER("§6[クエストマスター]","クエストを50回完了",TitleCondition.QUEST_COMPLETE, 50),

    // ─── 特殊 ───
    RICH("§e[大富豪]",          "所持金1,000,000達成", TitleCondition.MONEY,   1_000_000),
    CRAFTER("§a[職人]",         "アイテムを100個クラフト", TitleCondition.CRAFT, 100);

    private final String displayTag;
    private final String description;
    private final TitleCondition condition;
    private final long threshold;

    TitleDefinition(String displayTag, String description, TitleCondition condition, long threshold) {
        this.displayTag  = displayTag;
        this.description = description;
        this.condition   = condition;
        this.threshold   = threshold;
    }

    public String getDisplayTag()   { return displayTag; }
    public String getDescription()  { return description; }
    public TitleCondition getCondition() { return condition; }
    public long getThreshold()      { return threshold; }
}
