package com.woxloi.mythicrpg.equipment;

import com.woxloi.mythicrpg.core.MythicLogger;
import com.woxloi.mythicrpg.equipment.model.*;
import com.woxloi.mythicrpg.job.JobType;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

/**
 * 全RpgItemのマスターデータ置き場。
 * 起動時に items/*.yml を読み込んで登録する。
 * コードでハードコーディングしたデフォルト装備も登録する。
 */
public class EquipmentRegistry {

    private static final Map<String, RpgItem> registry = new LinkedHashMap<>();

    /* =====================
       初期化
     ===================== */
    public static void init(File dataFolder) {
        registry.clear();

        // デフォルト装備をコードで登録
        registerDefaults();

        // YAML定義を読み込んで上書き or 追加
        File itemsDir = new File(dataFolder, "items");
        if (itemsDir.exists()) {
            File[] files = itemsDir.listFiles((d, n) -> n.endsWith(".yml"));
            if (files != null) {
                for (File f : files) loadFromYaml(f);
            }
        }

        MythicLogger.info("装備レジストリ初期化完了: " + registry.size() + "種");
    }

    /* =====================
       デフォルト装備
     ===================== */
    private static void registerDefaults() {
        // ── 戦士武器 ──
        register(RpgItemBuilder.of("iron_sword_1", "鉄の剣", EquipSlot.WEAPON, EquipRarity.COMMON)
                .attack(8).defense(1)
                .requiredLevel(1).allowJob(JobType.WARRIOR)
                .material(Material.IRON_SWORD).maxEnhance(10).build());

        register(RpgItemBuilder.of("steel_sword_1", "鋼鉄の剣", EquipSlot.WEAPON, EquipRarity.UNCOMMON)
                .attack(16).defense(3).critRate(0.05)
                .requiredLevel(15).allowJob(JobType.WARRIOR)
                .material(Material.IRON_SWORD).maxEnhance(10).build());

        register(RpgItemBuilder.of("flame_sword_1", "炎の剣", EquipSlot.WEAPON, EquipRarity.RARE)
                .attack(28).defense(5).critRate(0.08).critDamage(0.3)
                .requiredLevel(30).allowJob(JobType.WARRIOR)
                .material(Material.BLAZE_ROD).maxEnhance(15)
                .specialEffect("攻撃時に炎上付与").build());

        register(RpgItemBuilder.of("dragon_blade", "竜牙の剣", EquipSlot.WEAPON, EquipRarity.EPIC)
                .attack(50).defense(10).critRate(0.12).critDamage(0.5).maxHpBonus(30)
                .requiredLevel(50).allowJob(JobType.WARRIOR)
                .material(Material.NETHERITE_SWORD).maxEnhance(15).build());

        register(RpgItemBuilder.of("excalibur", "エクスカリバー", EquipSlot.WEAPON, EquipRarity.LEGENDARY)
                .attack(90).defense(20).critRate(0.18).critDamage(0.8).maxHpBonus(60).speed(0.02)
                .requiredLevel(80).allowJob(JobType.WARRIOR)
                .material(Material.NETHERITE_SWORD).maxEnhance(20)
                .specialEffect("聖光の一撃: 範囲ダメージ+バフ").build());

        // ── 魔法使い武器 ──
        register(RpgItemBuilder.of("wooden_staff", "木の杖", EquipSlot.WEAPON, EquipRarity.COMMON)
                .attack(5).magicPower(10).maxMpBonus(20)
                .requiredLevel(1).allowJob(JobType.MAGE)
                .material(Material.STICK).maxEnhance(10).build());

        register(RpgItemBuilder.of("crystal_staff", "水晶の杖", EquipSlot.WEAPON, EquipRarity.RARE)
                .attack(8).magicPower(30).maxMpBonus(60).critRate(0.07)
                .requiredLevel(25).allowJob(JobType.MAGE)
                .material(Material.BLAZE_ROD).maxEnhance(15)
                .specialEffect("MP回復速度+30%").build());

        register(RpgItemBuilder.of("arcane_tome", "秘術の書", EquipSlot.WEAPON, EquipRarity.EPIC)
                .attack(6).magicPower(55).maxMpBonus(100).critRate(0.1).critDamage(0.6)
                .requiredLevel(45).allowJob(JobType.MAGE)
                .material(Material.ENCHANTED_BOOK).maxEnhance(15).build());

        // ── 弓使い武器 ──
        register(RpgItemBuilder.of("short_bow", "短弓", EquipSlot.WEAPON, EquipRarity.COMMON)
                .attack(7).critRate(0.04).maxSpBonus(10)
                .requiredLevel(1).allowJob(JobType.ARCHER)
                .material(Material.BOW).maxEnhance(10).build());

        register(RpgItemBuilder.of("hunters_bow", "狩人の弓", EquipSlot.WEAPON, EquipRarity.RARE)
                .attack(18).critRate(0.10).critDamage(0.25).maxSpBonus(30)
                .requiredLevel(20).allowJob(JobType.ARCHER)
                .material(Material.BOW).maxEnhance(15).build());

        // ── 鎧 ──
        register(RpgItemBuilder.of("leather_armor", "革の鎧", EquipSlot.CHESTPLATE, EquipRarity.COMMON)
                .defense(5).maxHpBonus(10)
                .requiredLevel(1).material(Material.LEATHER_CHESTPLATE).maxEnhance(10).build());

        register(RpgItemBuilder.of("iron_armor", "鉄の鎧", EquipSlot.CHESTPLATE, EquipRarity.UNCOMMON)
                .defense(12).maxHpBonus(25)
                .requiredLevel(10).material(Material.IRON_CHESTPLATE).maxEnhance(10).build());

        register(RpgItemBuilder.of("diamond_armor", "ダイヤの鎧", EquipSlot.CHESTPLATE, EquipRarity.RARE)
                .defense(22).maxHpBonus(50).speed(0.01)
                .requiredLevel(25).material(Material.DIAMOND_CHESTPLATE).maxEnhance(15).build());

        register(RpgItemBuilder.of("dragon_armor", "竜鱗の鎧", EquipSlot.CHESTPLATE, EquipRarity.EPIC)
                .defense(40).maxHpBonus(80).critRate(0.03)
                .requiredLevel(50).material(Material.NETHERITE_CHESTPLATE).maxEnhance(15).build());

        // ── 兜 ──
        register(RpgItemBuilder.of("iron_helmet", "鉄の兜", EquipSlot.HELMET, EquipRarity.UNCOMMON)
                .defense(8).maxHpBonus(15)
                .requiredLevel(10).material(Material.IRON_HELMET).maxEnhance(10).build());

        register(RpgItemBuilder.of("mage_hood", "魔法使いのフード", EquipSlot.HELMET, EquipRarity.RARE)
                .defense(4).maxMpBonus(40).magicPower(10)
                .requiredLevel(20).allowJob(JobType.MAGE)
                .material(Material.LEATHER_HELMET).maxEnhance(10).build());

        // ── 脚当て ──
        register(RpgItemBuilder.of("iron_legs", "鉄の脚当て", EquipSlot.LEGGINGS, EquipRarity.UNCOMMON)
                .defense(10).maxHpBonus(12)
                .requiredLevel(10).material(Material.IRON_LEGGINGS).maxEnhance(10).build());

        // ── 靴 ──
        register(RpgItemBuilder.of("iron_boots", "鉄の靴", EquipSlot.BOOTS, EquipRarity.UNCOMMON)
                .defense(6).speed(0.01)
                .requiredLevel(10).material(Material.IRON_BOOTS).maxEnhance(10).build());

        register(RpgItemBuilder.of("swift_boots", "疾風の靴", EquipSlot.BOOTS, EquipRarity.RARE)
                .defense(4).speed(0.04).maxSpBonus(20)
                .requiredLevel(20).material(Material.LEATHER_BOOTS).maxEnhance(10)
                .specialEffect("移動速度+10%").build());

        // ── アクセサリー ──
        register(RpgItemBuilder.of("ruby_ring", "ルビーの指輪", EquipSlot.RING_L, EquipRarity.UNCOMMON)
                .attack(5).critRate(0.03)
                .requiredLevel(10).material(Material.RED_DYE).maxEnhance(5).build());

        register(RpgItemBuilder.of("sapphire_ring", "サファイアの指輪", EquipSlot.RING_R, EquipRarity.UNCOMMON)
                .maxMpBonus(30).magicPower(8)
                .requiredLevel(10).material(Material.BLUE_DYE).maxEnhance(5).build());

        register(RpgItemBuilder.of("gold_necklace", "金の首飾り", EquipSlot.NECKLACE, EquipRarity.UNCOMMON)
                .maxHpBonus(20).defense(3)
                .requiredLevel(5).material(Material.GOLD_NUGGET).maxEnhance(5).build());

        register(RpgItemBuilder.of("dragon_relic", "竜の遺物", EquipSlot.RELIC, EquipRarity.EPIC)
                .attack(10).magicPower(10).maxHpBonus(30).critRate(0.05).critDamage(0.2)
                .requiredLevel(50).material(Material.DRAGON_BREATH).maxEnhance(10)
                .specialEffect("全ステータス+5%").build());
    }

    /* =====================
       YAML読み込み
     ===================== */
    private static void loadFromYaml(File file) {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        var root = yml.getConfigurationSection("items");
        if (root == null) return;

        for (String id : root.getKeys(false)) {
            var s = root.getConfigurationSection(id);
            if (s == null) continue;
            try {
                EquipSlot   slot   = EquipSlot.valueOf(s.getString("slot",   "WEAPON"));
                EquipRarity rarity = EquipRarity.valueOf(s.getString("rarity", "COMMON"));
                Material    mat    = Material.valueOf(s.getString("material", "STICK"));

                RpgItemBuilder b = RpgItemBuilder.of(id, s.getString("name", id), slot, rarity)
                        .material(mat)
                        .requiredLevel(s.getInt("required-level", 1))
                        .maxEnhance(s.getInt("max-enhance", 10))
                        .attack(s.getDouble("attack", 0))
                        .defense(s.getDouble("defense", 0))
                        .maxHpBonus(s.getDouble("max-hp", 0))
                        .maxMpBonus(s.getDouble("max-mp", 0))
                        .maxSpBonus(s.getDouble("max-sp", 0))
                        .critRate(s.getDouble("crit-rate", 0))
                        .critDamage(s.getDouble("crit-damage", 0))
                        .magicPower(s.getDouble("magic-power", 0))
                        .speed(s.getDouble("speed", 0));

                if (s.contains("special")) b.specialEffect(s.getString("special"));

                for (String job : s.getStringList("allowed-jobs")) {
                    try { b.allowJob(JobType.valueOf(job.toUpperCase())); } catch (Exception ignored) {}
                }
                register(b.build());
            } catch (Exception e) {
                MythicLogger.warn("装備YAML読み込み失敗 [" + id + "]: " + e.getMessage());
            }
        }
    }

    /* =====================
       公開API
     ===================== */
    public static void register(RpgItem item) {
        registry.put(item.id, item);
    }

    public static RpgItem get(String id) {
        return registry.get(id);
    }

    public static Collection<RpgItem> all() {
        return Collections.unmodifiableCollection(registry.values());
    }

    public static List<RpgItem> bySlot(EquipSlot slot) {
        List<RpgItem> result = new ArrayList<>();
        for (RpgItem i : registry.values()) if (i.slot == slot) result.add(i);
        return result;
    }
}
