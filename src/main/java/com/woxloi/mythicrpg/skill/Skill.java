package com.woxloi.mythicrpg.skill;

import org.bukkit.entity.Player;

public abstract class Skill {

    /* =====================
       基本情報
     ===================== */
    private final String id;
    private final String name;
    private final int unlockLevel;
    private final long cooldown; // 秒
    private final SkillTrigger trigger;

    /* =====================
       リソース消費
     ===================== */
    private final ResourceType resourceType;
    private final double cost;

    /* =====================
       コンストラクタ
     ===================== */
    public Skill(
            String id,
            String name,
            int unlockLevel,
            long cooldown,
            SkillTrigger trigger,
            ResourceType resourceType,
            double cost
    ) {
        this.id = id;
        this.name = name;
        this.unlockLevel = unlockLevel;
        this.cooldown = cooldown;
        this.trigger = trigger;
        this.resourceType = resourceType;
        this.cost = cost;
    }

    /* =====================
       Getter
     ===================== */
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getUnlockLevel() {
        return unlockLevel;
    }

    public long getCooldown() {
        return cooldown;
    }

    public SkillTrigger getTrigger() {
        return trigger;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public double getCost() {
        return cost;
    }

    /* =====================
       スキル効果本体
     ===================== */
    public abstract void execute(Player player);
}
