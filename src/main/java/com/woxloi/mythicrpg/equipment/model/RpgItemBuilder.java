package com.woxloi.mythicrpg.equipment.model;

import com.woxloi.mythicrpg.job.JobType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * RpgItemをFluentに組み立てるビルダー。
 *
 * 使用例:
 * RpgItem item = RpgItemBuilder.of("iron_sword_1", "§9鉄の剣", EquipSlot.WEAPON, EquipRarity.RARE)
 *     .attack(12).defense(2).maxHpBonus(20)
 *     .requiredLevel(10).allowJob(JobType.WARRIOR)
 *     .material(Material.IRON_SWORD).maxEnhance(10)
 *     .build();
 */
public class RpgItemBuilder {

    private final RpgItem item;

    private RpgItemBuilder(String id, String displayName, EquipSlot slot, EquipRarity rarity) {
        this.item = new RpgItem(id, displayName, slot, rarity);
    }

    public static RpgItemBuilder of(String id, String displayName, EquipSlot slot, EquipRarity rarity) {
        return new RpgItemBuilder(id, displayName, slot, rarity);
    }

    public RpgItemBuilder attack(double v)      { item.baseStats.attack     += v; return this; }
    public RpgItemBuilder defense(double v)     { item.baseStats.defense    += v; return this; }
    public RpgItemBuilder maxHpBonus(double v)  { item.baseStats.maxHpBonus += v; return this; }
    public RpgItemBuilder maxMpBonus(double v)  { item.baseStats.maxMpBonus += v; return this; }
    public RpgItemBuilder maxSpBonus(double v)  { item.baseStats.maxSpBonus += v; return this; }
    public RpgItemBuilder critRate(double v)    { item.baseStats.critRate   += v; return this; }
    public RpgItemBuilder critDamage(double v)  { item.baseStats.critDamage += v; return this; }
    public RpgItemBuilder magicPower(double v)  { item.baseStats.magicPower += v; return this; }
    public RpgItemBuilder speed(double v)       { item.baseStats.speed      += v; return this; }

    public RpgItemBuilder requiredLevel(int lv) { item.requiredLevel = lv;  return this; }
    public RpgItemBuilder maxEnhance(int max)   { item.maxEnhance    = max; return this; }
    public RpgItemBuilder specialEffect(String e){ item.specialEffect = e;  return this; }

    public RpgItemBuilder allowJob(JobType job) {
        item.allowedJobs.add(job);
        return this;
    }

    public RpgItemBuilder material(Material mat) {
        item.baseItem = new ItemStack(mat);
        return this;
    }

    public RpgItemBuilder baseItem(ItemStack stack) {
        item.baseItem = stack.clone();
        return this;
    }

    public RpgItem build() {
        if (item.baseItem == null) item.baseItem = new ItemStack(Material.STICK);
        return item;
    }
}
