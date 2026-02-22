package com.woxloi.mythicrpg.stats;

import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * StatPoint の管理・ステータスへの反映
 */
public class StatPointManager {

    private static final Map<UUID, StatPoint> cache = new HashMap<>();

    public static StatPoint get(UUID uuid) {
        return cache.computeIfAbsent(uuid, StatPoint::new);
    }

    /**
     * レベルアップ時に呼び出し
     */
    public static void onLevelUp(Player player) {
        StatPoint sp = get(player.getUniqueId());
        sp.addPoints(StatPoint.POINTS_PER_LEVEL);
        player.sendMessage("§a§l【ステータスポイント +3】 §7残り: " + sp.getFreePoints() + "pt");
        player.sendMessage("§7/mrpg stats で振り分けできます");
        applyToPlayerData(player);
    }

    /**
     * 振り分け処理
     */
    public static boolean allocate(Player player, StatType stat, int points) {
        StatPoint sp = get(player.getUniqueId());
        boolean ok = sp.allocate(stat, points);
        if (ok) {
            applyToPlayerData(player);
            player.sendMessage("§a" + stat.getDisplayName() + " §aに " + points + "pt 振り分けました");
            player.sendMessage("§7残りポイント: " + sp.getFreePoints() + "pt");
        } else {
            player.sendMessage("§cポイントが不足しています (残り: " + sp.getFreePoints() + "pt)");
        }
        return ok;
    }

    /**
     * リセット
     */
    public static void reset(Player player) {
        StatPoint sp = get(player.getUniqueId());
        sp.reset();
        applyToPlayerData(player);
        player.sendMessage("§eステータスポイントをリセットしました (残り: " + sp.getFreePoints() + "pt)");
    }

    /**
     * StatPoint の内容を PlayerData に反映する
     */
    public static void applyToPlayerData(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;
        StatPoint sp = get(player.getUniqueId());

        // ベースステータスにボーナスを上乗せ (ジョブベース + ポイントボーナス)
        double baseHp  = data.getMaxHp()  - sp.getBonusMaxHp();  // 現在のボーナス分を除いてベースを推測
        double baseMp  = data.getMaxMp()  - sp.getBonusMaxMp();
        double baseSp  = data.getMaxSp()  - sp.getBonusMaxSp();
        double baseAtk = data.getAttack() - sp.getBonusAttack();

        // 負にならないようにclamp
        data.setMaxHp(Math.max(10, baseHp  + sp.getBonusMaxHp()));
        data.setMaxMp(Math.max(0,  baseMp  + sp.getBonusMaxMp()));
        data.setMaxSp(Math.max(0,  baseSp  + sp.getBonusMaxSp()));
        data.setAttack(Math.max(1, baseAtk + sp.getBonusAttack()));
    }

    // ─── DB連携 ───
    public static void loadFromDb(UUID uuid, int free, int str, int vit, int intel, int agi) {
        StatPoint sp = get(uuid);
        sp.setFreePoints(free);
        sp.setStrPoints(str);
        sp.setVitPoints(vit);
        sp.setIntPoints(intel);
        sp.setAgiPoints(agi);
    }

    public static void unload(UUID uuid) {
        cache.remove(uuid);
    }
}
