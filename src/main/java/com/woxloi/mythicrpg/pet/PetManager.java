package com.woxloi.mythicrpg.pet;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ペットの召喚・解除・成長・スキル連動を管理する。
 * ペットデータは UUID → PetData で保持。
 */
public class PetManager {

    /** プレイヤーのペットデータ: ownerUUID → PetData */
    private static final Map<UUID, PetData> petDataMap = new ConcurrentHashMap<>();

    /** 定義済みペット: id → PetDefinition (pets.yml からロード) */
    private static Map<String, PetDefinition> definitions = new LinkedHashMap<>();

    /** pets.yml を読み込む。PluginBootstrap.enable() から呼ぶ。 */
    public static void load() {
        definitions = PetLoader.load();
    }

    private PetManager() {}

    // ─── 召喚 ────────────────────────────────────

    /**
     * ペットを召喚する。
     * @return null=成功, String=失敗理由
     */
    public static String summon(Player player, String petId) {
        PetDefinition def = definitions.get(petId);
        if (def == null) return "存在しないペットです";

        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return "プレイヤーデータがありません";
        if (data.getLevel() < def.getRequiredLevel())
            return "必要Lv " + def.getRequiredLevel() + " に達していません";

        if (!def.getAllowedJobs().isEmpty() && data.getJob() != null
                && !def.getAllowedJobs().contains(data.getJob()))
            return "このジョブはこのペットを召喚できません";

        PetData petData = petDataMap.computeIfAbsent(
                player.getUniqueId(), k -> new PetData(k, petId));

        if (petData.isSummoned()) return "すでにペットを召喚しています";

        // 既存のエンティティをクリア
        dismissEntity(player.getUniqueId());

        // MythicMobsでエンティティ召喚
        try {
            Location loc = player.getLocation().add(1, 0, 0);
            Entity entity = MythicBukkit.inst().getMobManager()
                    .spawnMob(def.getMythicMobId(), loc)
                    .getEntity().getBukkitEntity();

            petData.setSummonedEntityUuid(entity.getUniqueId());
            petData.setSummoned(true);

            MythicRPG.playerPrefixMsg(player, "§a" + def.getDisplayName() + " §7を召喚しました！");
        } catch (Exception e) {
            return "ペットの召喚に失敗しました（MythicMob ID: " + def.getMythicMobId() + "）";
        }
        return null;
    }

    // ─── 解除 ────────────────────────────────────

    public static void dismiss(Player player) {
        dismissEntity(player.getUniqueId());
        PetData petData = petDataMap.get(player.getUniqueId());
        if (petData != null) petData.setSummoned(false);
        MythicRPG.playerPrefixMsg(player, "§7ペットを帰還させました");
    }

    private static void dismissEntity(UUID ownerUuid) {
        PetData petData = petDataMap.get(ownerUuid);
        if (petData == null || petData.getSummonedEntityUuid() == null) return;

        Entity entity = org.bukkit.Bukkit.getEntity(petData.getSummonedEntityUuid());
        if (entity != null && !entity.isDead()) entity.remove();
        petData.setSummonedEntityUuid(null);
    }

    // ─── 成長 ────────────────────────────────────

    /**
     * ペットに経験値を与える（オーナーがMobを倒したとき等）。
     */
    public static void addPetExp(UUID ownerUuid, double amount) {
        PetData petData = petDataMap.get(ownerUuid);
        if (petData == null || !petData.isSummoned()) return;

        PetDefinition def = definitions.get(petData.getPetDefinitionId());
        if (def == null) return;

        boolean leveledUp = petData.addExp(def, amount);
        if (leveledUp) {
            Player player = org.bukkit.Bukkit.getPlayer(ownerUuid);
            if (player != null) {
                MythicRPG.playerPrefixMsg(player,
                    "§a§lペットレベルアップ！ " + def.getDisplayName() + " §7Lv " + petData.getLevel());
                player.playSound(player.getLocation(),
                    org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f);
            }
        }
    }

    // ─── ユーティリティ ──────────────────────────

    public static PetData getPetData(UUID uuid) { return petDataMap.get(uuid); }

    public static void loadPetData(UUID uuid, PetData data) { petDataMap.put(uuid, data); }

    public static void unloadPetData(UUID uuid) {
        dismissEntity(uuid);
        petDataMap.remove(uuid);
    }

    public static Collection<PetDefinition> getAllDefinitions() { return definitions.values(); }

    public static PetDefinition getDefinition(String id) { return definitions.get(id); }

    public static boolean hasSummonedPet(UUID uuid) {
        PetData data = petDataMap.get(uuid);
        return data != null && data.isSummoned();
    }
}
