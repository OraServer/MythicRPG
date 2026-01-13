package com.woxloi.mythicrpg.skill;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillCooldownManager {

    private static final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public static boolean isOnCooldown(UUID uuid, String skillId) {
        long now = System.currentTimeMillis();
        return cooldowns.containsKey(uuid)
                && cooldowns.get(uuid).containsKey(skillId)
                && cooldowns.get(uuid).get(skillId) > now;
    }

    public static long getRemaining(UUID uuid, String skillId) {
        if (!isOnCooldown(uuid, skillId)) return 0;
        return (cooldowns.get(uuid).get(skillId) - System.currentTimeMillis()) / 1000;
    }

    public static void setCooldown(UUID uuid, String skillId, long seconds) {
        cooldowns
                .computeIfAbsent(uuid, k -> new HashMap<>())
                .put(skillId, System.currentTimeMillis() + seconds * 1000);
    }
}
