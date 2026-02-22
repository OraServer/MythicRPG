package com.woxloi.mythicrpg.equipment.socket;

import com.woxloi.mythicrpg.equipment.model.RpgItem;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * ソケット操作のロジックを提供するマネージャー。
 * RpgItemにはSocketSlotのリストが付属しており、
 * このクラスがGemの挿入・取り外しを管理する。
 */
public class SocketManager {

    /**
     * アイテムに宝石を挿入する。
     * @return null = 成功, それ以外 = エラーメッセージ
     */
    public static String insertGem(Player player, RpgItem item, int slotIndex, GemType gem) {
        List<SocketSlot> slots = item.socketSlots;
        if (slots == null || slotIndex < 0 || slotIndex >= slots.size()) {
            return "指定したソケットスロットが存在しません";
        }
        SocketSlot slot = slots.get(slotIndex);
        if (!slot.isEmpty()) {
            return "このスロットには既に宝石が嵌まっています。取り外してから挿入してください";
        }
        slot.setGem(gem);
        return null;
    }

    /**
     * アイテムから宝石を取り外す（宝石は消滅）。
     * @return null = 成功, それ以外 = エラーメッセージ
     */
    public static String removeGem(Player player, RpgItem item, int slotIndex) {
        List<SocketSlot> slots = item.socketSlots;
        if (slots == null || slotIndex < 0 || slotIndex >= slots.size()) {
            return "指定したソケットスロットが存在しません";
        }
        SocketSlot slot = slots.get(slotIndex);
        if (slot.isEmpty()) {
            return "このスロットには宝石が嵌まっていません";
        }
        slot.removeGem();
        return null;
    }

    /**
     * アイテムのソケット合計ボーナスを計算する。
     */
    public static SocketBonus calcBonus(RpgItem item) {
        SocketBonus bonus = new SocketBonus();
        if (item.socketSlots == null) return bonus;
        for (SocketSlot slot : item.socketSlots) {
            if (!slot.isEmpty()) {
                GemType g = slot.getGem();
                bonus.bonusMaxHp += g.bonusMaxHp;
                bonus.bonusAtk   += g.bonusAtk;
                bonus.bonusMp    += g.bonusMp;
                bonus.bonusDef   += g.bonusDef;
                bonus.bonusSp    += g.bonusSp;
            }
        }
        return bonus;
    }

    /**
     * 指定したスロット数のリストを初期化する。
     */
    public static List<SocketSlot> createSlots(int count) {
        List<SocketSlot> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new SocketSlot());
        }
        return list;
    }

    /** ソケットボーナス集計クラス */
    public static class SocketBonus {
        public int bonusMaxHp = 0;
        public int bonusAtk   = 0;
        public int bonusMp    = 0;
        public int bonusDef   = 0;
        public int bonusSp    = 0;
    }
}
