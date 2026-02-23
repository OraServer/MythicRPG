package com.woxloi.mythicrpg.element;

import com.woxloi.mythicrpg.equipment.drop.DropTableRegistry;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * MythicMobのスポーン時に drop_tables.yml の mob-element-map に基づいて
 * EntityのPDCに属性タグを書き込む。
 *
 * これにより ElementalDamageListener が getMobElement() で正しい属性を取得できる。
 */
public class MobElementSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMythicMobSpawn(MythicMobSpawnEvent event) {
        String mobId = event.getMobType().getInternalName();
        ElementType element = DropTableRegistry.getElementForMob(mobId);

        if (element == ElementType.NONE) return; // 無属性はタグ不要

        try {
            Entity entity = event.getMob().getEntity().getBukkitEntity();
            ElementManager.setMobElement(entity, element);
        } catch (Exception ignored) {
            // getBukkitEntity() が null になるケースを無視
        }
    }
}
