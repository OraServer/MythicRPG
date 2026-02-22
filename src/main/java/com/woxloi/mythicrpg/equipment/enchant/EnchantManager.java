package com.woxloi.mythicrpg.equipment.enchant;

import com.woxloi.mythicrpg.equipment.model.EquipStats;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * エンチャント付与・適用・解除を管理するマネージャー。
 * エンチャントはRpgItemのenchantフィールド（List<ItemEnchant>）に格納する。
 * RpgItemにenchantsフィールドを追加して使用する。
 */
public class EnchantManager {

    private static final Random RANDOM = new Random();

    /** 1アイテムに付与できる最大エンチャント数 */
    private static final int MAX_ENCHANTS = 3;

    private EnchantManager() {}

    /**
     * アイテムにエンチャントを付与する。
     * @return true=成功, false=スロット満杯 or 同一種類がある
     */
    public static boolean enchant(RpgItem item, EnchantType type, int rank, List<ItemEnchant> enchants) {
        if (enchants.size() >= MAX_ENCHANTS) return false;

        // 同一種類チェック
        boolean alreadyHas = enchants.stream().anyMatch(e -> e.getType() == type);
        if (alreadyHas) {
            // 上書き（ランクアップ）
            enchants.removeIf(e -> e.getType() == type);
        }

        enchants.add(new ItemEnchant(type, rank));
        return true;
    }

    /**
     * エンチャントを解除する。
     * @return true=解除成功, false=該当エンチャントなし
     */
    public static boolean removeEnchant(List<ItemEnchant> enchants, EnchantType type) {
        return enchants.removeIf(e -> e.getType() == type);
    }

    /**
     * エンチャントブックを使用してランダムエンチャントを付与する試み。
     * レアリティによって付与成功率が異なる（RANK1=100%→RANK5=20%）。
     */
    public static boolean tryApplyEnchantBook(Player player, List<ItemEnchant> enchants, EnchantType type, int rank) {
        double successRate = Math.max(0.2, 1.0 - (rank - 1) * 0.2);
        if (RANDOM.nextDouble() > successRate) {
            player.sendMessage("§c✗ エンチャント付与に失敗しました...");
            return false;
        }
        boolean applied = enchant(null, type, rank, enchants);
        if (applied) {
            player.sendMessage("§a✔ §e" + type.getColoredName(rank) + " §aを付与しました！");
        }
        return applied;
    }

    /**
     * エンチャント群からEquipStatsへのボーナスを計算して返す。
     * ベースステータスに掛け率として適用する形で使う。
     */
    public static EnchantBonuses calcBonuses(List<ItemEnchant> enchants) {
        EnchantBonuses bonuses = new EnchantBonuses();
        for (ItemEnchant e : enchants) {
            double val = e.getValue() / 100.0; // %→割合
            switch (e.getType()) {
                case POWER      -> bonuses.attackBonus     += val;
                case FORTIFY    -> bonuses.defenseBonus    += val;
                case PRECISION  -> bonuses.critRateBonus   += val;
                case ARCANE     -> bonuses.magicBonus      += val;
                case VITALITY   -> bonuses.maxHpBonus      += val;
                case WISDOM     -> bonuses.maxMpBonus      += val;
                case SWIFTNESS  -> bonuses.speedBonus      += val;
                case EXPERIENCE -> bonuses.expBonus        += val;
                case FORTUNE    -> bonuses.dropBonus       += val;
                case ALACRITY   -> bonuses.cooldownReduce  += val;
            }
        }
        return bonuses;
    }

    /**
     * ランダムなエンチャントを1つ生成する（エンチャントスクロール等で使用）。
     */
    public static ItemEnchant generateRandom() {
        EnchantType[] types = EnchantType.values();
        EnchantType type = types[RANDOM.nextInt(types.length)];
        int rank = RANDOM.nextInt(5) + 1;
        return new ItemEnchant(type, rank);
    }

    /**
     * エンチャント一覧のloreを生成する。
     */
    public static List<String> buildLore(List<ItemEnchant> enchants) {
        List<String> lore = new ArrayList<>();
        if (enchants.isEmpty()) return lore;
        lore.add("§8--- エンチャント ---");
        for (ItemEnchant e : enchants) {
            lore.add("§e" + e.getColoredName() + " §7" + e.getDescription());
        }
        return lore;
    }

    /**
     * エンチャントボーナスの集計データクラス。
     */
    public static class EnchantBonuses {
        public double attackBonus;
        public double defenseBonus;
        public double critRateBonus;
        public double magicBonus;
        public double maxHpBonus;
        public double maxMpBonus;
        public double speedBonus;
        public double expBonus;
        public double dropBonus;
        public double cooldownReduce;
    }
}
