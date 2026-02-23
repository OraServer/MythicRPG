package com.woxloi.mythicrpg.dungeon;

import com.woxloi.mythicrpg.element.ElementType;
import com.woxloi.mythicrpg.equipment.model.EquipRarity;

import java.util.List;
import java.util.Map;

/**
 * ダンジョンの定義データ。dungeons.yml から DungeonLoader が生成する。
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

    // フロアごとのMythicMob IDリスト (floor番号 → Mob IDリスト)
    private final Map<Integer, List<String>> floorMobs;
    // 最終ボスのMythicMob ID
    private final String bossId;
    // ダンジョン内Mobのデフォルト属性
    private final ElementType mobElement;
    private final ElementType bossElement;
    // スポーンオフセット (プレイヤー座標からの相対値)
    private final List<int[]> spawnOffsets;
    private final int expReward;

    public DungeonDefinition(String id, String displayName, String description,
                              int requiredLevel, int maxPlayers, int floorCount,
                              int timeLimitSeconds, EquipRarity rewardRarity,
                              Map<Integer, List<String>> floorMobs, String bossId,
                              ElementType mobElement, ElementType bossElement,
                              List<int[]> spawnOffsets, int expReward) {
        this.id               = id;
        this.displayName      = displayName;
        this.description      = description;
        this.requiredLevel    = requiredLevel;
        this.maxPlayers       = maxPlayers;
        this.floorCount       = floorCount;
        this.timeLimitSeconds = timeLimitSeconds;
        this.rewardRarity     = rewardRarity;
        this.floorMobs        = floorMobs;
        this.bossId           = bossId;
        this.mobElement       = mobElement;
        this.bossElement      = bossElement;
        this.spawnOffsets     = spawnOffsets;
        this.expReward        = expReward;
    }

    public String getId()                    { return id; }
    public String getDisplayName()           { return displayName; }
    public String getDescription()           { return description; }
    public int getRequiredLevel()            { return requiredLevel; }
    public int getMaxPlayers()               { return maxPlayers; }
    public int getFloorCount()               { return floorCount; }
    public int getTimeLimitSeconds()         { return timeLimitSeconds; }
    public EquipRarity getRewardRarity()     { return rewardRarity; }
    public Map<Integer, List<String>> getFloorMobs() { return floorMobs; }
    public String getBossId()                { return bossId; }
    public ElementType getMobElement()       { return mobElement; }
    public ElementType getBossElement()      { return bossElement; }
    public List<int[]> getSpawnOffsets()     { return spawnOffsets; }
    public int getExpReward()                { return expReward; }

    /** フロア番号に対応するMob IDリストを返す（未定義は空リスト） */
    public List<String> getMobsForFloor(int floor) {
        return floorMobs.getOrDefault(floor, List.of());
    }

    /** GUI lore用テキスト */
    public List<String> buildLore(int playerLevel) {
        boolean canEnter = playerLevel >= requiredLevel;
        return List.of(
            "§7" + description.replace("\\n", "\n§7"),
            "",
            "§8--- 情報 ---",
            "§7必要Lv: " + (canEnter ? "§a" : "§c") + requiredLevel,
            "§7最大人数: §f" + maxPlayers + "人",
            "§7フロア数: §f" + floorCount + "層",
            "§7制限時間: §f" + (timeLimitSeconds / 60) + "分",
            "§7報酬レアリティ: " + rewardRarity.color + rewardRarity.displayName,
            "§7Mob属性: " + mobElement.getTagged(),
            "",
            canEnter ? "§aクリックで入場" : "§cレベルが足りません"
        );
    }
}
