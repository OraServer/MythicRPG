package com.woxloi.mythicrpg.equipment.enhancer;

import com.woxloi.mythicrpg.equipment.model.RpgItem;
import org.bukkit.entity.Player;

import java.util.Random;

/**
 * 装備強化システム。
 * 強化段階ごとに成功率が下がり、失敗でもアイテムは破壊されない（ダウングレード方式）。
 *
 * 成功率:
 *  +0〜+5  : 100%
 *  +6      : 70%
 *  +7      : 60%
 *  +8      : 50%
 *  +9      : 40%
 *  +10     : 30%
 *  +11〜+15: 20%
 *  +16〜+20: 10%
 *
 * 失敗時: +5以上は -1（ダウングレード）、それ未満は変化なし。
 */
public class EnhanceManager {

    private static final Random RNG = new Random();

    /**
     * 強化を試みる。
     * @return null = 成功、非null = 失敗理由メッセージ
     */
    public static String tryEnhance(Player player, RpgItem item) {
        if (item.enhanceLevel >= item.maxEnhance) {
            return "既に最大強化段階です（+" + item.maxEnhance + "）";
        }

        // 素材（エメラルド）チェック
        int costEmerald = enhanceCost(item.enhanceLevel);
        int held = countEmerald(player);
        if (held < costEmerald) {
            return "強化素材が不足しています（エメラルド×" + costEmerald + "が必要、所持:" + held + "）";
        }

        // 素材消費
        removeEmerald(player, costEmerald);

        // 成功判定
        double successRate = successRate(item.enhanceLevel);
        if (RNG.nextDouble() <= successRate) {
            item.enhanceLevel++;
            return null;  // 成功
        } else {
            // 失敗
            if (item.enhanceLevel >= 5) {
                item.enhanceLevel--;
                player.sendMessage("§c§l強化失敗！ §7段階が下がりました（+" + item.enhanceLevel + "）");
            } else {
                player.sendMessage("§c§l強化失敗！ §7段階は変わりません");
            }
            return "FAIL_SILENT"; // GUIに"失敗"と表示させるための特殊値
        }
    }

    /** 成功率 (0.0〜1.0) */
    public static double successRate(int currentLevel) {
        if (currentLevel < 6)  return 1.0;
        if (currentLevel < 7)  return 0.70;
        if (currentLevel < 8)  return 0.60;
        if (currentLevel < 9)  return 0.50;
        if (currentLevel < 10) return 0.40;
        if (currentLevel < 11) return 0.30;
        if (currentLevel < 16) return 0.20;
        return 0.10;
    }

    /** 強化コスト（エメラルド個数） */
    public static int enhanceCost(int currentLevel) {
        if (currentLevel < 5)  return 5;
        if (currentLevel < 10) return 15;
        if (currentLevel < 15) return 30;
        return 50;
    }

    private static int countEmerald(Player player) {
        int count = 0;
        for (var item : player.getInventory().getContents()) {
            if (item != null && item.getType() == org.bukkit.Material.EMERALD) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private static void removeEmerald(Player player, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            var item = player.getInventory().getItem(i);
            if (item == null || item.getType() != org.bukkit.Material.EMERALD) continue;
            if (item.getAmount() <= remaining) {
                remaining -= item.getAmount();
                player.getInventory().setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - remaining);
                remaining = 0;
            }
            if (remaining <= 0) break;
        }
    }
}
