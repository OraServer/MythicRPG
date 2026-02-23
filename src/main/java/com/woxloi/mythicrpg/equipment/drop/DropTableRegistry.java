package com.woxloi.mythicrpg.equipment.drop;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.element.ElementType;
import com.woxloi.mythicrpg.equipment.model.EquipRarity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ドロップテーブルのレジストリ。
 * drop_tables.yml から読み込む。
 *
 * 追加機能:
 *   mob-table-map  → MythicMob内部名からテーブルIDを引く
 *   mob-element-map → MythicMob内部名からElementTypeを引く（スポーン時に付与）
 */
public class DropTableRegistry {

    /** テーブルID → DropTable */
    private static final Map<String, DropTable> tables = new HashMap<>();

    /** MythicMob内部名 → テーブルID */
    private static final Map<String, String> mobTableMap = new HashMap<>();

    /** MythicMob内部名 → ElementType */
    private static final Map<String, ElementType> mobElementMap = new HashMap<>();

    // ────────────────────────────────────────────────
    //  ロード
    // ────────────────────────────────────────────────

    public static void load() {
        tables.clear();
        mobTableMap.clear();
        mobElementMap.clear();

        File file = new File(MythicRPG.getInstance().getDataFolder(), "drop_tables.yml");
        if (!file.exists()) MythicRPG.getInstance().saveResource("drop_tables.yml", false);

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // 1. mob-table-map
        ConfigurationSection mobMap = config.getConfigurationSection("mob-table-map");
        if (mobMap != null) {
            for (String mobId : mobMap.getKeys(false)) {
                mobTableMap.put(mobId, mobMap.getString(mobId, ""));
            }
        }

        // 2. mob-element-map
        ConfigurationSection elemMap = config.getConfigurationSection("mob-element-map");
        if (elemMap != null) {
            for (String mobId : elemMap.getKeys(false)) {
                String elemStr = elemMap.getString(mobId, "NONE");
                try {
                    mobElementMap.put(mobId, ElementType.valueOf(elemStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    MythicLogger.warn("mob-element-map: 不明な属性 [" + mobId + "=" + elemStr + "]");
                }
            }
        }

        // 3. tables
        ConfigurationSection root = config.getConfigurationSection("tables");
        if (root != null) {
            for (String tableId : root.getKeys(false)) {
                ConfigurationSection sec = root.getConfigurationSection(tableId);
                if (sec == null) continue;

                // drop-chance または drop_chance の両方に対応
                double chance = sec.contains("drop-chance")
                        ? sec.getDouble("drop-chance", 0.1)
                        : sec.getDouble("drop_chance", 0.1);

                DropTable table = new DropTable(tableId, chance);

                ConfigurationSection entries = sec.getConfigurationSection("entries");
                if (entries != null) {
                    for (String key : entries.getKeys(false)) {
                        ConfigurationSection e = entries.getConfigurationSection(key);
                        if (e == null) continue;
                        double weight = e.getDouble("weight", 1.0);
                        String fixedId  = e.getString("item-id",  e.getString("item_id"));
                        String rarityStr = e.getString("rarity");
                        if (fixedId != null) {
                            table.addEntry(new DropEntry(weight, fixedId));
                        } else if (rarityStr != null) {
                            try {
                                table.addEntry(new DropEntry(weight,
                                        EquipRarity.valueOf(rarityStr.toUpperCase())));
                            } catch (IllegalArgumentException ex) {
                                MythicLogger.warn("DropTable[" + tableId + "] 不明なrarity: " + rarityStr);
                            }
                        }
                    }
                }
                tables.put(tableId, table);
            }
        }

        MythicLogger.info("DropTable: " + tables.size() + "テーブル / "
                + mobTableMap.size() + "Mobマップ / "
                + mobElementMap.size() + "属性マップ 読み込み完了");
    }

    // ────────────────────────────────────────────────
    //  公開API
    // ────────────────────────────────────────────────

    public static DropTable get(String id) { return tables.get(id); }
    public static Map<String, DropTable> getAll() { return Collections.unmodifiableMap(tables); }

    /**
     * MythicMob内部名からドロップテーブルIDを返す。
     * 未登録の場合は null。
     */
    public static String getTableIdForMob(String mythicMobId) {
        return mobTableMap.get(mythicMobId);
    }

    /**
     * MythicMob内部名から ElementType を返す。
     * 未登録の場合は ElementType.NONE。
     */
    public static ElementType getElementForMob(String mythicMobId) {
        return mobElementMap.getOrDefault(mythicMobId, ElementType.NONE);
    }
}
