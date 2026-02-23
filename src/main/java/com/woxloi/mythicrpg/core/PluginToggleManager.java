package com.woxloi.mythicrpg.core;

import com.woxloi.mythicrpg.MythicRPG;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * プラグインの各システムを個別にON/OFFできる管理クラス。
 *
 * toggle.yml に設定を保存・読み込みし、/mrpg toggle で操作する。
 *
 * 対応システム:
 *   combat      - RPGダメージ計算 / HP同期
 *   scoreboard  - スコアボード表示
 *   actionbar   - アクションバー表示
 *   skills      - スキルシステム
 *   artifact    - アーティファクトシステム
 *   dungeon     - ダンジョンシステム
 *   pet         - ペットシステム
 *   pvp         - PvPシステム
 *   element     - 属性ダメージシステム
 *   combo       - コンボシステム
 *   buff        - バフシステム
 *   drop        - 装備ドロップ
 *   regen       - HP/MP/SP自動回復
 */
public class PluginToggleManager {

    private static final Set<String> VALID_SYSTEMS = Set.of(
            "combat", "scoreboard", "actionbar", "skills",
            "artifact", "dungeon", "pet", "pvp",
            "element", "combo", "buff", "drop", "regen"
    );

    private static final Map<String, Boolean> states = new LinkedHashMap<>();
    private static File toggleFile;

    // =========================================================
    //  初期化
    // =========================================================

    public static void init() {
        toggleFile = new File(MythicRPG.getInstance().getDataFolder(), "toggle.yml");

        // デフォルト：全てON
        for (String sys : VALID_SYSTEMS) {
            states.put(sys, true);
        }

        // ファイルが存在すれば読み込む
        if (toggleFile.exists()) {
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(toggleFile);
            for (String sys : VALID_SYSTEMS) {
                states.put(sys, yaml.getBoolean("systems." + sys, true));
            }
        }

        MythicLogger.info("ToggleManager 初期化完了。OFF: "
                + states.entrySet().stream()
                        .filter(e -> !e.getValue())
                        .map(Map.Entry::getKey)
                        .toList());
    }

    // =========================================================
    //  公開API
    // =========================================================

    /** システムが有効かどうか */
    public static boolean isEnabled(String system) {
        return states.getOrDefault(system.toLowerCase(), true);
    }

    /** システムを有効にする */
    public static void enable(String system) {
        if (!VALID_SYSTEMS.contains(system.toLowerCase())) return;
        states.put(system.toLowerCase(), true);
        save();
        MythicLogger.info("[Toggle] " + system + " → ON");
    }

    /** システムを無効にする */
    public static void disable(String system) {
        if (!VALID_SYSTEMS.contains(system.toLowerCase())) return;
        states.put(system.toLowerCase(), false);
        save();
        MythicLogger.info("[Toggle] " + system + " → OFF");
    }

    /** トグル（ON/OFFを反転） */
    public static boolean toggle(String system) {
        String key = system.toLowerCase();
        if (!VALID_SYSTEMS.contains(key)) return false;
        boolean newState = !states.getOrDefault(key, true);
        states.put(key, newState);
        save();
        MythicLogger.info("[Toggle] " + system + " → " + (newState ? "ON" : "OFF"));
        return newState;
    }

    /** 全システムの状態マップを返す */
    public static Map<String, Boolean> allStates() {
        return Collections.unmodifiableMap(states);
    }

    /** 有効なシステム名一覧 */
    public static Set<String> validSystems() {
        return VALID_SYSTEMS;
    }

    // =========================================================
    //  永続化
    // =========================================================

    private static void save() {
        FileConfiguration yaml = new YamlConfiguration();
        states.forEach((sys, enabled) -> yaml.set("systems." + sys, enabled));
        try {
            yaml.save(toggleFile);
        } catch (IOException e) {
            MythicLogger.warn("toggle.yml 保存失敗: " + e.getMessage());
        }
    }
}
