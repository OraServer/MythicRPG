package com.woxloi.mythicrpg.equipment.forge;

import com.woxloi.mythicrpg.equipment.model.EquipRarity;
import com.woxloi.mythicrpg.equipment.model.EquipSlot;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 鍛冶レシピ定義。
 * YAMLから読み込み、ForgeManagerで管理される。
 */
public class ForgeRecipe {

    /** レシピID */
    private final String id;

    /** レシピ名（GUI表示用） */
    private final String name;

    /** 必要素材: アイテムIDまたはマテリアル名 -> 必要数 */
    private final Map<String, Integer> ingredients;

    /** 成果物の固定アイテムID（nullの場合はランダム生成） */
    private final String outputItemId;

    /** ランダム生成の場合のスロット */
    private final EquipSlot outputSlot;

    /** ランダム生成の場合のレアリティ */
    private final EquipRarity outputRarity;

    /** 必要プレイヤーレベル */
    private final int requiredLevel;

    /** 必要ゴールド消費量 */
    private final int goldCost;

    /** このレシピが有効かどうか */
    private final boolean enabled;

    public ForgeRecipe(String id, String name, Map<String, Integer> ingredients,
                       String outputItemId, EquipSlot outputSlot, EquipRarity outputRarity,
                       int requiredLevel, int goldCost) {
        this.id           = id;
        this.name         = name;
        this.ingredients  = new LinkedHashMap<>(ingredients);
        this.outputItemId = outputItemId;
        this.outputSlot   = outputSlot;
        this.outputRarity = outputRarity;
        this.requiredLevel = requiredLevel;
        this.goldCost     = goldCost;
        this.enabled      = true;
    }

    /* ==================== Getters ==================== */

    public String getId() { return id; }

    public String getName() { return name; }

    public Map<String, Integer> getIngredients() { return ingredients; }

    /** 固定出力アイテムIDがあるか（nullならランダム生成） */
    public boolean hasFixedOutput() { return outputItemId != null; }

    public String getOutputItemId() { return outputItemId; }

    public EquipSlot getOutputSlot() { return outputSlot; }

    public EquipRarity getOutputRarity() { return outputRarity; }

    public int getRequiredLevel() { return requiredLevel; }

    public int getGoldCost() { return goldCost; }

    public boolean isEnabled() { return enabled; }

    /**
     * 必要素材の説明文を生成（GUI tooltip用）
     */
    public java.util.List<String> getIngredientLore() {
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7--- 必要素材 ---");
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
            lore.add("§e" + entry.getKey() + " §fx" + entry.getValue());
        }
        if (goldCost > 0) {
            lore.add("§6ゴールド: §e" + goldCost + "G");
        }
        if (requiredLevel > 1) {
            lore.add("§b必要レベル: §f" + requiredLevel);
        }
        return lore;
    }

    @Override
    public String toString() {
        return "ForgeRecipe{id='" + id + "', name='" + name + "'}";
    }
}
