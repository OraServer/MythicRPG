package com.woxloi.mythicrpg.dungeon;

import com.woxloi.mythicrpg.equipment.model.EquipRarity;

import java.util.List;

/**
 * ダンジョンの定義データ（YAMLまたはハードコードで登録）。
 * DungeonManagerで保持され、プレイヤーへのGUI表示・入場チェックに使われる。
 */
public class DungeonDefinition {

    private final String id;
    private final String displayName;
    private final String description;
    private final int requiredLevel;
    private final int maxPlayers;
    private final int floorCount;
    private final int timeLimitSeconds;
    private final EquipRarity rewardRarity;
    private final List<String> mobIds;     // MythicMobs ID リスト
    private final String bossId;           // 最終ボスのMythicMobs ID

    public DungeonDefinition(String id, String displayName, String description,
                              int requiredLevel, int maxPlayers, int floorCount,
                              int timeLimitSeconds, EquipRarity rewardRarity,
                              List<String> mobIds, String bossId) {
        this.id               = id;
        this.displayName      = displayName;
        this.description      = description;
        this.requiredLevel    = requiredLevel;
        this.maxPlayers       = maxPlayers;
        this.floorCount       = floorCount;
        this.timeLimitSeconds = timeLimitSeconds;
        this.rewardRarity     = rewardRarity;
        this.mobIds           = mobIds;
        this.bossId           = bossId;
    }

    public String getId()              { return id; }
    public String getDisplayName()     { return displayName; }
    public String getDescription()     { return description; }
    public int getRequiredLevel()      { return requiredLevel; }
    public int getMaxPlayers()         { return maxPlayers; }
    public int getFloorCount()         { return floorCount; }
    public int getTimeLimitSeconds()   { return timeLimitSeconds; }
    public EquipRarity getRewardRarity() { return rewardRarity; }
    public List<String> getMobIds()    { return mobIds; }
    public String getBossId()          { return bossId; }

    /** GUI lore用のテキストを生成 */
    public List<String> buildLore(int playerLevel) {
        boolean canEnter = playerLevel >= requiredLevel;
        return List.of(
            "§7" + description,
            "",
            "§8--- 情報 ---",
            "§7必要Lv: " + (canEnter ? "§a" : "§c") + requiredLevel,
            "§7最大人数: §f" + maxPlayers + "人",
            "§7フロア数: §f" + floorCount + "層",
            "§7制限時間: §f" + (timeLimitSeconds / 60) + "分",
            "§7報酬レアリティ: " + rewardRarity.color + rewardRarity.displayName,
            "",
            canEnter ? "§aクリックで入場" : "§cレベルが足りません"
        );
    }
}
