package com.woxloi.mythicrpg.equipment.drop;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.model.RpgItemSerializer;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Mob討伐時に装備をドロップするリスナー。
 * MythicMobsのMobIDとバニラEntityTypeの両方に対応。
 */
public class EquipDropListener implements Listener {

    private static final Random RANDOM = new Random();

    /** バニラEntityType → DropTable IDのマッピング */
    private static final Map<EntityType, String> VANILLA_TABLE_MAP = new HashMap<>();

    static {
        VANILLA_TABLE_MAP.put(EntityType.ZOMBIE,         "zombie_basic");
        VANILLA_TABLE_MAP.put(EntityType.SKELETON,       "zombie_basic");
        VANILLA_TABLE_MAP.put(EntityType.SPIDER,         "zombie_basic");
        VANILLA_TABLE_MAP.put(EntityType.CAVE_SPIDER,    "zombie_basic");
        VANILLA_TABLE_MAP.put(EntityType.CREEPER,        "zombie_basic");
        VANILLA_TABLE_MAP.put(EntityType.ENDERMAN,       "zombie_basic");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        String tableId = VANILLA_TABLE_MAP.get(event.getEntity().getType());
        if (tableId == null) return;

        rollAndDrop(killer, tableId, event);
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        if (!(event.getKiller() instanceof Player)) return;
        Player killer = (Player) event.getKiller();

        String mobId = event.getMobType().getInternalName();
        // MythicMob IDをキー名としてdroptableを引く（例: "BOSS_GOLEM" → "boss_standard"）
        String tableId = resolveMythicTable(mobId);
        if (tableId == null) return;

        // バニラEntityDeathEventが同時に発火するため、ここはMythicMob専用テーブルのみ対象
        DropTable table = DropTableRegistry.get(tableId);
        if (table == null) return;

        List<RpgItem> drops = table.roll(RANDOM);
        for (RpgItem item : drops) {
            giveOrDrop(killer, item);
        }
    }

    private void rollAndDrop(Player killer, String tableId, EntityDeathEvent event) {
        DropTable table = DropTableRegistry.get(tableId);
        if (table == null) return;

        List<RpgItem> drops = table.roll(RANDOM);
        for (RpgItem item : drops) {
            giveOrDrop(killer, item);
        }
    }

    private void giveOrDrop(Player player, RpgItem item) {
        ItemStack is = RpgItemSerializer.serialize(item);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(is);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), is);
        }
        MythicRPG.msg(player, ChatColor.GOLD + "【装備ドロップ】" + item.displayName + " を入手しました！");
    }

    private String resolveMythicTable(String mobId) {
        // ボスMobは大文字の接頭辞BOSS_を使う規則とする
        if (mobId.toUpperCase().startsWith("BOSS")) return "boss_standard";
        return null;
    }
}
