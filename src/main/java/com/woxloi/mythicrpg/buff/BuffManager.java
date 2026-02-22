package com.woxloi.mythicrpg.buff;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * バフ/デバフの付与・管理・Tick処理
 */
public class BuffManager {

    // UUID → バフリスト
    private static final Map<UUID, List<BuffEntry>> playerBuffs = new ConcurrentHashMap<>();

    // ─────────────────────────────────
    //  付与
    // ─────────────────────────────────
    /**
     * プレイヤーにバフを付与する
     */
    public static void applyBuff(Player player, BuffType type, double magnitude, int durationTicks, String source) {
        List<BuffEntry> buffs = playerBuffs.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());

        if (!type.isStackable()) {
            // スタック不可ならば既存を除去して新規付与
            buffs.removeIf(b -> b.getType() == type);
        }

        buffs.add(new BuffEntry(type, magnitude, durationTicks, source));
        applyVanillaEffect(player, type, durationTicks);
        sendBuffMessage(player, type, durationTicks / 20);
    }

    /**
     * バフを除去する
     */
    public static void removeBuff(Player player, BuffType type) {
        List<BuffEntry> buffs = playerBuffs.get(player.getUniqueId());
        if (buffs == null) return;
        buffs.removeIf(b -> b.getType() == type);
        removeVanillaEffect(player, type);
    }

    /**
     * 全バフ/デバフを除去（死亡時など）
     */
    public static void clearAll(Player player) {
        playerBuffs.remove(player.getUniqueId());
        for (BuffType type : BuffType.values()) {
            removeVanillaEffect(player, type);
        }
    }

    // ─────────────────────────────────
    //  取得
    // ─────────────────────────────────
    public static List<BuffEntry> getBuffs(Player player) {
        return playerBuffs.getOrDefault(player.getUniqueId(), Collections.emptyList());
    }

    public static boolean hasBuff(Player player, BuffType type) {
        return getBuffs(player).stream().anyMatch(b -> b.getType() == type);
    }

    /**
     * 特定バフの合計効果量を取得 (複数スタック時は合算)
     */
    public static double getTotalMagnitude(Player player, BuffType type) {
        return getBuffs(player).stream()
                .filter(b -> b.getType() == type)
                .mapToDouble(BuffEntry::getMagnitude)
                .sum();
    }

    // ─────────────────────────────────
    //  攻撃力計算用ヘルパー
    // ─────────────────────────────────
    /**
     * バフ込みの攻撃力倍率を返す (1.0 = 等倍)
     */
    public static double getAttackMultiplier(Player player) {
        double mul = 1.0;
        if (hasBuff(player, BuffType.ATK_UP))
            mul += getTotalMagnitude(player, BuffType.ATK_UP);
        if (hasBuff(player, BuffType.ATK_DOWN))
            mul -= getTotalMagnitude(player, BuffType.ATK_DOWN);
        return Math.max(0.1, mul);
    }

    /**
     * バフ込みのEXP倍率を返す
     */
    public static double getExpMultiplier(Player player) {
        if (!hasBuff(player, BuffType.EXP_BOOST)) return 1.0;
        return 1.0 + getTotalMagnitude(player, BuffType.EXP_BOOST);
    }

    /**
     * 沈黙状態かどうか (スキル使用不可)
     */
    public static boolean isSilenced(Player player) {
        return hasBuff(player, BuffType.SILENCE);
    }

    /**
     * スタン状態かどうか
     */
    public static boolean isStunned(Player player) {
        return hasBuff(player, BuffType.STUN);
    }

    // ─────────────────────────────────
    //  Tick処理 (毎秒呼び出す)
    // ─────────────────────────────────
    public static void tickAll() {
        for (Map.Entry<UUID, List<BuffEntry>> entry : playerBuffs.entrySet()) {
            UUID uuid = entry.getKey();
            Player player = Bukkit.getPlayer(uuid);
            List<BuffEntry> buffs = entry.getValue();

            Iterator<BuffEntry> it = buffs.iterator();
            while (it.hasNext()) {
                BuffEntry buff = it.next();

                // 毒ダメージ
                if (buff.getType() == BuffType.POISON && player != null) {
                    PlayerData data = PlayerDataManager.get(player);
                    if (data != null) {
                        data.addHp(-buff.getMagnitude());
                    }
                }

                // 燃焼ダメージ
                if (buff.getType() == BuffType.BURN && player != null) {
                    PlayerData data = PlayerDataManager.get(player);
                    if (data != null) {
                        data.addHp(-buff.getMagnitude() * 1.5);
                    }
                }

                // 20tick (1秒) ごとにtick
                for (int i = 0; i < 20; i++) buff.tick();

                if (buff.isExpired()) {
                    it.remove();
                    if (player != null) {
                        player.sendMessage("§7[バフ終了] §f" + buff.getType().getDisplayName());
                    }
                }
            }
        }
    }

    // ─────────────────────────────────
    //  バニラエフェクト適用
    // ─────────────────────────────────
    private static void applyVanillaEffect(Player player, BuffType type, int durationTicks) {
        switch (type) {
            case SPEED_UP    -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,      durationTicks, 1, false, false));
            case SPEED_DOWN  -> player.addPotionEffect(new PotionEffect(PotionEffectType.getByName("slow"),   durationTicks, 1, false, false));
            case REGEN_HP    -> player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, durationTicks, 0, false, false));
            case POISON      -> player.addPotionEffect(new PotionEffect(PotionEffectType.POISON,     durationTicks, 0, false, false));
            case BURN        -> player.setFireTicks(durationTicks);
            case BLIND       -> player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,  durationTicks, 0, false, false));
            case FREEZE      -> player.addPotionEffect(new PotionEffect(PotionEffectType.getByName("slow"),   durationTicks, 4, false, false));
            default          -> {}
        }
    }

    private static void removeVanillaEffect(Player player, BuffType type) {
        switch (type) {
            case SPEED_UP    -> player.removePotionEffect(PotionEffectType.SPEED);
            case SPEED_DOWN  -> player.removePotionEffect(PotionEffectType.getByName("slow"));
            case REGEN_HP    -> player.removePotionEffect(PotionEffectType.REGENERATION);
            case POISON      -> player.removePotionEffect(PotionEffectType.POISON);
            case BLIND       -> player.removePotionEffect(PotionEffectType.BLINDNESS);
            default          -> {}
        }
    }

    // ─────────────────────────────────
    //  メッセージ
    // ─────────────────────────────────
    private static void sendBuffMessage(Player player, BuffType type, int seconds) {
        String color = type.isBuff() ? "§a" : "§c";
        String label = type.isBuff() ? "[バフ]" : "[デバフ]";
        player.sendMessage(color + label + " §f" + type.getDisplayName() + " §7(" + seconds + "秒)");
    }
}
