package com.woxloi.mythicrpg.equipment.drop;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.equipment.model.EquipRarity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * ドロップテーブルのレジストリ。
 * drop_tables.yml から読み込んで管理する。
 */
public class DropTableRegistry {

    private static final Map<String, DropTable> tables = new HashMap<>();

    /**
     * drop_tables.yml を読み込む。
     */
    public static void load() {
        tables.clear();
        File file = new File(MythicRPG.getInstance().getDataFolder(), "drop_tables.yml");
        if (!file.exists()) {
            MythicRPG.getInstance().saveResource("drop_tables.yml", false);
            if (!file.exists()) {
                createDefault();
                return;
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = config.getConfigurationSection("tables");
        if (root == null) return;

        for (String tableId : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(tableId);
            if (sec == null) continue;
            double chance = sec.getDouble("drop_chance", 0.1);
            DropTable table = new DropTable(tableId, chance);

            ConfigurationSection entries = sec.getConfigurationSection("entries");
            if (entries != null) {
                for (String key : entries.getKeys(false)) {
                    ConfigurationSection e = entries.getConfigurationSection(key);
                    if (e == null) continue;
                    double weight = e.getDouble("weight", 1.0);
                    String fixedId = e.getString("item_id");
                    String rarityStr = e.getString("rarity");
                    if (fixedId != null) {
                        table.addEntry(new DropEntry(weight, fixedId));
                    } else if (rarityStr != null) {
                        try {
                            table.addEntry(new DropEntry(weight, EquipRarity.valueOf(rarityStr.toUpperCase())));
                        } catch (IllegalArgumentException ex) {
                            MythicLogger.warn("DropTable[" + tableId + "] 不明なrarity: " + rarityStr);
                        }
                    }
                }
            }
            tables.put(tableId, table);
        }
        MythicLogger.info("DropTable: " + tables.size() + "テーブルを読み込みました");
    }

    public static DropTable get(String id) {
        return tables.get(id);
    }

    public static Map<String, DropTable> getAll() {
        return tables;
    }

    private static void createDefault() {
        File file = new File(MythicRPG.getInstance().getDataFolder(), "drop_tables.yml");
        FileConfiguration config = new YamlConfiguration();
        // 基本テーブル
        config.set("tables.zombie_basic.drop_chance", 0.15);
        config.set("tables.zombie_basic.entries.common.weight", 70.0);
        config.set("tables.zombie_basic.entries.common.rarity", "COMMON");
        config.set("tables.zombie_basic.entries.uncommon.weight", 25.0);
        config.set("tables.zombie_basic.entries.uncommon.rarity", "UNCOMMON");
        config.set("tables.zombie_basic.entries.rare.weight", 5.0);
        config.set("tables.zombie_basic.entries.rare.rarity", "RARE");
        // ボステーブル
        config.set("tables.boss_standard.drop_chance", 1.0);
        config.set("tables.boss_standard.entries.epic.weight", 60.0);
        config.set("tables.boss_standard.entries.epic.rarity", "EPIC");
        config.set("tables.boss_standard.entries.legendary.weight", 40.0);
        config.set("tables.boss_standard.entries.legendary.rarity", "LEGENDARY");
        try { config.save(file); } catch (Exception e) { e.printStackTrace(); }
        load();
    }
}
