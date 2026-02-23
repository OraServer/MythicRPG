package com.woxloi.mythicrpg.dungeon;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.element.ElementType;
import com.woxloi.mythicrpg.equipment.model.EquipRarity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * dungeons.yml を読み込んで DungeonDefinition のリストを返すローダー。
 */
public class DungeonLoader {

    private DungeonLoader() {}

    public static Map<String, DungeonDefinition> load() {
        Map<String, DungeonDefinition> result = new LinkedHashMap<>();

        File file = new File(MythicRPG.getInstance().getDataFolder(), "dungeons.yml");
        if (!file.exists()) {
            MythicRPG.getInstance().saveResource("dungeons.yml", false);
        }

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        InputStream def = MythicRPG.getInstance().getResource("dungeons.yml");
        if (def != null) {
            yaml.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(def, StandardCharsets.UTF_8)));
        }

        ConfigurationSection dungeons = yaml.getConfigurationSection("dungeons");
        if (dungeons == null) { MythicLogger.warn("dungeons.yml に dungeons セクションがありません"); return result; }

        for (String id : dungeons.getKeys(false)) {
            ConfigurationSection sec = dungeons.getConfigurationSection(id);
            if (sec == null) continue;
            try {
                result.put(id, parseDungeon(id, sec));
            } catch (Exception e) {
                MythicLogger.warn("ダンジョン [" + id + "] の読み込みに失敗: " + e.getMessage());
            }
        }

        MythicLogger.info("ダンジョン読み込み完了: " + result.size() + "件");
        return result;
    }

    private static DungeonDefinition parseDungeon(String id, ConfigurationSection sec) {
        String display      = sec.getString("display", id);
        String description  = sec.getString("description", "");
        int requiredLevel   = sec.getInt("required-level", 1);
        int maxPlayers      = sec.getInt("max-players", 4);
        int floorCount      = sec.getInt("floor-count", 5);
        int timeLimit       = sec.getInt("time-limit", 600);
        int expReward       = sec.getInt("exp-reward", 100);

        EquipRarity rewardRarity = EquipRarity.UNCOMMON;
        try { rewardRarity = EquipRarity.valueOf(sec.getString("reward-rarity", "UNCOMMON").toUpperCase()); }
        catch (Exception ignored) {}

        ElementType mobElement = ElementType.NONE;
        try { mobElement = ElementType.valueOf(sec.getString("mob-element", "NONE").toUpperCase()); }
        catch (Exception ignored) {}

        ElementType bossElement = ElementType.NONE;
        try { bossElement = ElementType.valueOf(sec.getString("boss-element", "NONE").toUpperCase()); }
        catch (Exception ignored) {}

        String bossId = sec.getString("boss-id", "");

        // フロアごとのMobリスト
        Map<Integer, List<String>> floorMobs = new LinkedHashMap<>();
        ConfigurationSection floorSec = sec.getConfigurationSection("floor-mobs");
        if (floorSec != null) {
            for (String floorKey : floorSec.getKeys(false)) {
                try {
                    int floor = Integer.parseInt(floorKey);
                    floorMobs.put(floor, floorSec.getStringList(floorKey));
                } catch (NumberFormatException ignored) {}
            }
        }

        // スポーンオフセット
        List<int[]> spawnOffsets = new ArrayList<>();
        List<?> offsets = sec.getList("spawn-offsets");
        if (offsets != null) {
            for (Object o : offsets) {
                if (o instanceof List<?> coords && coords.size() >= 3) {
                    try {
                        spawnOffsets.add(new int[]{
                            ((Number) coords.get(0)).intValue(),
                            ((Number) coords.get(1)).intValue(),
                            ((Number) coords.get(2)).intValue()
                        });
                    } catch (Exception ignored) {}
                }
            }
        }
        if (spawnOffsets.isEmpty()) {
            spawnOffsets = List.of(
                new int[]{3,0,0}, new int[]{-3,0,0},
                new int[]{0,0,3}, new int[]{0,0,-3}
            );
        }

        return new DungeonDefinition(id, display, description, requiredLevel, maxPlayers,
                floorCount, timeLimit, rewardRarity, floorMobs, bossId,
                mobElement, bossElement, spawnOffsets, expReward);
    }
}
