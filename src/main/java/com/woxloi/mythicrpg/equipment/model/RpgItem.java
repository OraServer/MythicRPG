package com.woxloi.mythicrpg.equipment.model;

import com.woxloi.mythicrpg.equipment.socket.SocketSlot;
import com.woxloi.mythicrpg.job.JobType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * RPGアイテム（装備品）のデータクラス。
 * ItemStackのPersistentDataContainerにシリアライズして保存する。
 */
public class RpgItem {

    /** YAMLやDB上のID */
    public String id;

    /** 表示名（レアリティカラー込み） */
    public String displayName;

    /** 装備スロット */
    public EquipSlot slot;

    /** レアリティ */
    public EquipRarity rarity;

    /** ベースステータス（強化前） */
    public EquipStats baseStats;

    /** 装備可能なジョブ（空 = 全ジョブOK） */
    public Set<JobType> allowedJobs;

    /** 装備に必要なレベル */
    public int requiredLevel;

    /** 現在の強化段階 (0〜maxEnhance) */
    public int enhanceLevel;

    /** 最大強化段階 */
    public int maxEnhance;

    /** 見た目のItemStack（Material・カスタムモデルデータ） */
    public ItemStack baseItem;

    /** 特殊効果ID（スキルと連動する場合など） */
    public String specialEffect;

    /** 精錬段階 (0〜5) */
    public int refineLevel;

    /** セットID（セット効果に使用） */
    public String setId;

    /** ソケットスロット一覧 */
    public List<SocketSlot> socketSlots;

    /** 未鑑定フラグ */
    public boolean unidentified;

    public RpgItem(String id, String displayName, EquipSlot slot, EquipRarity rarity) {
        this.id           = id;
        this.displayName  = displayName;
        this.slot         = slot;
        this.rarity       = rarity;
        this.baseStats    = new EquipStats();
        this.allowedJobs  = EnumSet.noneOf(JobType.class);
        this.requiredLevel = 1;
        this.enhanceLevel  = 0;
        this.maxEnhance    = 10;
        this.specialEffect = null;
        this.refineLevel   = 0;
        this.setId         = null;
        this.socketSlots   = new ArrayList<>();
        this.unidentified  = false;
    }

    /**
     * 強化・精錬込みの実効ステータスを計算する。
     * 強化段階ごとに base × (1 + 0.1 × enhance) を返す。
     * 精錬段階ごとにさらに +5% ずつ加算。
     */
    public EquipStats getEffectiveStats() {
        double factor = rarity.statMultiplier * (1.0 + 0.1 * enhanceLevel) * (1.0 + 0.05 * refineLevel);
        return baseStats.multiply(factor);
    }
}
