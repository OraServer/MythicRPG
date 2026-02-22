package com.woxloi.mythicrpg.pet;

import com.woxloi.mythicrpg.MythicRPG;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * ペットに関連するイベント処理。
 * - ペットMobの死亡検知
 * - オーナーがMobを倒したときのペット経験値
 * - ログアウト時のペット解除
 */
public class PetListener implements Listener {

    /** MythicMobの死亡 → ペット経験値付与 */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        if (!(event.getKiller() instanceof Player killer)) return;

        // ペットが召喚中なら経験値の30%をペットに分配
        if (PetManager.hasSummonedPet(killer.getUniqueId())) {
            double mobLevel = event.getMob().getLevel();
            PetManager.addPetExp(killer.getUniqueId(), mobLevel * 3.0);
        }

        // 倒されたMobがペット自身かチェック
        checkIfPetDied(event.getMob().getEntity().getBukkitEntity().getUniqueId());
    }

    /** バニラMob死亡 → ペット経験値付与 */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        // MythicMobはMythicMobDeathEvent で処理するのでスキップ
        if (MythicBukkit.inst().getMobManager()
                .isActiveMob(event.getEntity().getUniqueId())) return;

        if (PetManager.hasSummonedPet(killer.getUniqueId())) {
            PetManager.addPetExp(killer.getUniqueId(), 5.0);
        }

        // ペット自身が倒されたかチェック
        checkIfPetDied(event.getEntity().getUniqueId());
    }

    /** ログアウト時にペットを強制解除 */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PetManager.unloadPetData(event.getPlayer().getUniqueId());
    }

    /** エンティティがいずれかのペットか判定し、死亡処理を行う */
    private void checkIfPetDied(UUID entityUuid) {
        for (UUID ownerUuid : getAllOwners()) {
            PetData petData = PetManager.getPetData(ownerUuid);
            if (petData == null || !entityUuid.equals(petData.getSummonedEntityUuid())) continue;

            petData.setSummoned(false);
            petData.setSummonedEntityUuid(null);

            Player owner = org.bukkit.Bukkit.getPlayer(ownerUuid);
            if (owner != null) {
                PetDefinition def = PetManager.getDefinition(petData.getPetDefinitionId());
                String name = def != null ? def.getDisplayName() : "ペット";
                MythicRPG.playerPrefixMsg(owner, "§c" + name + " §7が倒されました…");
            }
            break;
        }
    }

    /** 全オーナーUUIDのスナップショットを返す */
    private Iterable<UUID> getAllOwners() {
        return org.bukkit.Bukkit.getOnlinePlayers().stream()
                .map(Entity::getUniqueId)
                .filter(uuid -> PetManager.getPetData(uuid) != null)
                .toList();
    }
}
