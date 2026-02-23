package com.woxloi.mythicrpg.artifact;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.equipment.model.EquipSlot;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 全アーティファクトピースの定義レジストリ。
 *
 * artifacts.yml から動的に読み込む。
 * reload() でホットリロード可能。
 *
 * MythicMobs連携ドロップテーブルも管理:
 *   mobDropTable: MythicMob内部名 → List<DropEntry(pieceId, chance)>
 */
public class ArtifactRegistry {

    // ─── ピースレジストリ ───
    private static final Map<String, ArtifactPiece> registry = new LinkedHashMap<>();

    // ─── MythicMobsドロップテーブル ───
    // key: MythicMob内部名, value: ドロップエントリリスト
    private static final Map<String, List<MobDropEntry>> mobDropTable = new HashMap<>();

    /** MythicMobsドロップエントリ */
    public record MobDropEntry(String pieceId, double chance) {}

    // ─── セット定義レジストリ ───
    // YAMLから読んだセット定義（bonus-stats等）を保持
    private static final Map<String, ArtifactSetDef> setDefRegistry = new LinkedHashMap<>();

    /** セット定義（YAML由来） */
    public record ArtifactSetDef(
            String setKey,
            String display,
            String description,
            int[] piecesRequired,
            String[] bonusDescriptions,
            // tier→stat bonus map
            Map<Integer, Map<String, Object>> tierBonuses
    ) {}

    // =========================================================
    //  初期化・リロード
    // =========================================================

    /**
     * artifacts.yml を読み込んでレジストリを構築する。
     * PluginBootstrap.enable() で呼ぶ。
     */
    public static void load() {
        registry.clear();
        mobDropTable.clear();
        setDefRegistry.clear();

        File file = new File(MythicRPG.getInstance().getDataFolder(), "artifacts.yml");
        if (!file.exists()) {
            MythicRPG.getInstance().saveResource("artifacts.yml", false);
        }

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        // jar内デフォルトをフォールバックに設定
        InputStream defaultStream = MythicRPG.getInstance().getResource("artifacts.yml");
        if (defaultStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            yaml.setDefaults(defaults);
        }

        // セット定義を読み込む
        ConfigurationSection setsSection = yaml.getConfigurationSection("sets");
        if (setsSection != null) {
            for (String setKey : setsSection.getKeys(false)) {
                loadSetDef(setKey, setsSection.getConfigurationSection(setKey));
            }
        }

        // ピース定義を読み込む
        ConfigurationSection piecesSection = yaml.getConfigurationSection("pieces");
        if (piecesSection != null) {
            for (String pieceId : piecesSection.getKeys(false)) {
                loadPiece(pieceId, piecesSection.getConfigurationSection(pieceId));
            }
        }

        MythicLogger.info("アーティファクト読み込み完了: "
                + registry.size() + "ピース / "
                + setDefRegistry.size() + "セット / "
                + mobDropTable.values().stream().mapToInt(List::size).sum() + "ドロップエントリ");
    }

    private static void loadSetDef(String setKey, ConfigurationSection sec) {
        if (sec == null) return;
        try {
            String display = sec.getString("display", setKey);
            String description = sec.getString("description", "");
            List<Integer> reqList = sec.getIntegerList("pieces-required");
            int[] piecesRequired = reqList.stream().mapToInt(i -> i).toArray();
            List<String> descList = sec.getStringList("bonus-descriptions");
            String[] bonusDescriptions = descList.toArray(new String[0]);

            // tier bonus stats
            Map<Integer, Map<String, Object>> tierBonuses = new LinkedHashMap<>();
            ConfigurationSection bonusStats = sec.getConfigurationSection("bonus-stats");
            if (bonusStats != null) {
                for (String tierKey : bonusStats.getKeys(false)) {
                    int tier = Integer.parseInt(tierKey.replace("tier", ""));
                    ConfigurationSection ts = bonusStats.getConfigurationSection(tierKey);
                    if (ts == null) continue;
                    Map<String, Object> bonusMap = new LinkedHashMap<>();
                    for (String statKey : ts.getKeys(false)) {
                        bonusMap.put(statKey, ts.get(statKey));
                    }
                    tierBonuses.put(tier, bonusMap);
                }
            }

            setDefRegistry.put(setKey, new ArtifactSetDef(
                    setKey, display, description, piecesRequired, bonusDescriptions, tierBonuses));
        } catch (Exception e) {
            MythicLogger.warn("セット定義読み込み失敗 [" + setKey + "]: " + e.getMessage());
        }
    }

    private static void loadPiece(String pieceId, ConfigurationSection sec) {
        if (sec == null) return;
        try {
            String setKey = sec.getString("set", "");
            // ArtifactTypeはenumなので変換（YAMLのsetKeyがenum名に一致すること前提）
            ArtifactType setType;
            try {
                setType = ArtifactType.valueOf(setKey.toUpperCase());
            } catch (IllegalArgumentException e) {
                MythicLogger.warn("ピース [" + pieceId + "] の set値 '" + setKey + "' が不明なArtifactTypeです");
                return;
            }

            String name = sec.getString("name", pieceId);
            EquipSlot slot;
            try {
                slot = EquipSlot.valueOf(sec.getString("slot", "WEAPON").toUpperCase());
            } catch (IllegalArgumentException e) {
                slot = EquipSlot.WEAPON;
            }

            int atk = sec.getInt("atk", 0);
            int def = sec.getInt("def", 0);
            int hp  = sec.getInt("hp",  0);
            int mp  = sec.getInt("mp",  0);

            registry.put(pieceId, new ArtifactPiece(setType, slot, pieceId, name, atk, def, hp, mp));

            // MythicMobsドロップテーブル登録
            ConfigurationSection drops = sec.getConfigurationSection("mythicmob-drops");
            if (drops != null) {
                for (String mobId : drops.getKeys(false)) {
                    double chance = drops.getDouble(mobId, 0.0);
                    mobDropTable.computeIfAbsent(mobId, k -> new ArrayList<>())
                                .add(new MobDropEntry(pieceId, chance));
                }
            }
        } catch (Exception e) {
            MythicLogger.warn("ピース読み込み失敗 [" + pieceId + "]: " + e.getMessage());
        }
    }

    // =========================================================
    //  公開API — ピース
    // =========================================================

    public static ArtifactPiece get(String id) {
        return registry.get(id);
    }

    public static Collection<ArtifactPiece> all() {
        return Collections.unmodifiableCollection(registry.values());
    }

    public static List<ArtifactPiece> ofSet(ArtifactType type) {
        List<ArtifactPiece> list = new ArrayList<>();
        for (ArtifactPiece p : registry.values()) {
            if (p.getSetType() == type) list.add(p);
        }
        return list;
    }

    public static boolean exists(String id) {
        return registry.containsKey(id);
    }

    // =========================================================
    //  公開API — セット定義
    // =========================================================

    public static ArtifactSetDef getSetDef(String setKey) {
        return setDefRegistry.get(setKey);
    }

    public static Collection<ArtifactSetDef> allSetDefs() {
        return Collections.unmodifiableCollection(setDefRegistry.values());
    }

    // =========================================================
    //  公開API — MythicMobsドロップ
    // =========================================================

    /**
     * 指定MythicMob内部名に対するドロップエントリ一覧を返す。
     * 存在しない場合は空リスト。
     */
    public static List<MobDropEntry> getDropsForMob(String mythicMobId) {
        return mobDropTable.getOrDefault(mythicMobId, Collections.emptyList());
    }

    /**
     * ドロップテーブルが登録されている全MythicMob内部名を返す。
     */
    public static Set<String> registeredMobIds() {
        return Collections.unmodifiableSet(mobDropTable.keySet());
    }

    /**
     * 件数サマリ（デバッグ用）
     */
    public static String summary() {
        return "pieces=" + registry.size()
                + " sets=" + setDefRegistry.size()
                + " mobDropEntries=" + mobDropTable.values().stream().mapToInt(List::size).sum();
    }
}
