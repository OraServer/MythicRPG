package com.woxloi.mythicrpg.equipment.model;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.equipment.EquipmentRegistry;
import com.woxloi.mythicrpg.job.JobType;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * RpgItem ↔ ItemStack の変換ユーティリティ。
 * PersistentDataContainer にIDと強化値を保存し、ロア表示はここで生成する。
 */
public class RpgItemSerializer {

    public static final NamespacedKey KEY_ITEM_ID       = new NamespacedKey(MythicRPG.getInstance(), "rpg_item_id");
    public static final NamespacedKey KEY_ENHANCE_LEVEL = new NamespacedKey(MythicRPG.getInstance(), "rpg_enhance");

    /**
     * RpgItem → 表示用ItemStack（Loreなど全て付与済み）
     */
    public static ItemStack toItemStack(RpgItem rpgItem) {
        ItemStack stack = rpgItem.baseItem.clone();
        ItemMeta meta = stack.getItemMeta();

        // 表示名
        String name = rpgItem.rarity.color + rpgItem.displayName;
        if (rpgItem.enhanceLevel > 0) name += " §e+" + rpgItem.enhanceLevel;
        meta.displayName(Component.text(name));

        // Lore
        EquipStats eff = rpgItem.getEffectiveStats();
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(rpgItem.rarity.displayName + " §7| " + rpgItem.slot.icon + " " + rpgItem.slot.displayName));
        lore.add(Component.text("§8─────────────"));
        if (eff.attack    > 0) lore.add(Component.text("§c攻撃力: §f+" + (int) eff.attack));
        if (eff.defense   > 0) lore.add(Component.text("§7防御力: §f+" + (int) eff.defense));
        if (eff.maxHpBonus> 0) lore.add(Component.text("§a最大HP: §f+" + (int) eff.maxHpBonus));
        if (eff.maxMpBonus> 0) lore.add(Component.text("§bMP上限: §f+" + (int) eff.maxMpBonus));
        if (eff.maxSpBonus> 0) lore.add(Component.text("§6SP上限: §f+" + (int) eff.maxSpBonus));
        if (eff.critRate  > 0) lore.add(Component.text("§e会心率: §f+" + String.format("%.1f%%", eff.critRate * 100)));
        if (eff.critDamage> 0) lore.add(Component.text("§e会心倍率: §f+" + String.format("%.0f%%", eff.critDamage * 100)));
        if (eff.magicPower> 0) lore.add(Component.text("§d魔力: §f+" + (int) eff.magicPower));
        if (eff.speed     > 0) lore.add(Component.text("§a速度: §f+" + String.format("%.2f", eff.speed)));
        lore.add(Component.text("§8─────────────"));
        lore.add(Component.text("§7必要Lv: §f" + rpgItem.requiredLevel));
        if (!rpgItem.allowedJobs.isEmpty()) {
            StringBuilder jobs = new StringBuilder("§7対応ジョブ: ");
            for (JobType j : rpgItem.allowedJobs) jobs.append("§e").append(j.name()).append("§7 ");
            lore.add(Component.text(jobs.toString().trim()));
        }
        if (rpgItem.enhanceLevel > 0) {
            lore.add(Component.text("§e強化: §f+" + rpgItem.enhanceLevel + " §7/ +" + rpgItem.maxEnhance));
        }
        if (rpgItem.specialEffect != null) {
            lore.add(Component.text("§d特殊: §f" + rpgItem.specialEffect));
        }
        meta.lore(lore);

        // PDC
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY_ITEM_ID,       PersistentDataType.STRING,  rpgItem.id);
        pdc.set(KEY_ENHANCE_LEVEL, PersistentDataType.INTEGER, rpgItem.enhanceLevel);

        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * ItemStack から RpgItem を復元する。
     * RegistryにIDが登録されていない場合は null を返す。
     */
    public static RpgItem fromItemStack(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
        String id = pdc.get(KEY_ITEM_ID, PersistentDataType.STRING);
        if (id == null) return null;

        RpgItem template = EquipmentRegistry.get(id);
        if (template == null) return null;

        // 強化レベルを上書き（インスタンスをコピーして独立させる）
        RpgItem instance = deepCopy(template);
        Integer enhance = pdc.get(KEY_ENHANCE_LEVEL, PersistentDataType.INTEGER);
        if (enhance != null) instance.enhanceLevel = enhance;
        return instance;
    }

    public static boolean isRpgItem(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return false;
        return stack.getItemMeta().getPersistentDataContainer()
                .has(KEY_ITEM_ID, PersistentDataType.STRING);
    }

    // ─── 後方互換エイリアス ───────────────────────────
    /** {@link #toItemStack(RpgItem)} の別名 */
    public static ItemStack serialize(RpgItem rpgItem) { return toItemStack(rpgItem); }
    /** {@link #fromItemStack(ItemStack)} の別名 */
    public static RpgItem deserialize(ItemStack stack) { return fromItemStack(stack); }

    /** シャローコピー（stats は新オブジェクト） */
    private static RpgItem deepCopy(RpgItem src) {
        RpgItem copy = new RpgItem(src.id, src.displayName, src.slot, src.rarity);
        copy.baseStats      = src.baseStats.multiply(1.0);
        copy.allowedJobs    = java.util.EnumSet.copyOf(src.allowedJobs.isEmpty()
                ? java.util.EnumSet.noneOf(JobType.class) : src.allowedJobs);
        copy.requiredLevel  = src.requiredLevel;
        copy.enhanceLevel   = src.enhanceLevel;
        copy.maxEnhance     = src.maxEnhance;
        copy.baseItem       = src.baseItem.clone();
        copy.specialEffect  = src.specialEffect;
        return copy;
    }
}
