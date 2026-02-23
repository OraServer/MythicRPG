package com.woxloi.mythicrpg.equipment.drop;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.model.RpgItemSerializer;
import com.woxloi.mythicrpg.artifact.ArtifactManager;
import com.woxloi.mythicrpg.artifact.ArtifactPiece;
import com.woxloi.mythicrpg.artifact.ArtifactRegistry;
import com.woxloi.mythicrpg.artifact.ArtifactRepository;
import org.bukkit.Material;
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

        // 1. アーティファクトドロップ判定（artifacts.yml の mythicmob-drops）
        rollArtifactDrops(killer, mobId);

        // 2. 通常装備ドロップ（boss_standard テーブル等）
        String tableId = resolveMythicTable(mobId);
        if (tableId == null) return;
        DropTable table = DropTableRegistry.get(tableId);
        if (table == null) return;
        List<RpgItem> drops = table.roll(RANDOM);
        for (RpgItem item : drops) {
            giveOrDrop(killer, item);
        }
    }

    /**
     * artifacts.yml の mythicmob-drops に基づいてアーティファクトをドロップする。
     */
    private void rollArtifactDrops(Player killer, String mobId) {
        List<ArtifactRegistry.MobDropEntry> entries = ArtifactRegistry.getDropsForMob(mobId);
        if (entries.isEmpty()) return;

        for (ArtifactRegistry.MobDropEntry entry : entries) {
            if (RANDOM.nextDouble() < entry.chance()) {
                ArtifactPiece piece = ArtifactRegistry.get(entry.pieceId());
                if (piece == null) continue;

                ItemStack item = buildArtifactItemStack(piece);
                giveOrDropItem(killer, item, piece.getPieceName());

                // 取得履歴をDBに記録
                ArtifactRepository.logAcquired(killer.getUniqueId(), piece.getPieceId());
                break; // 1Mob倒すたびに最大1個のアーティファクト
            }
        }
    }

    private ItemStack buildArtifactItemStack(ArtifactPiece piece) {
        Material mat = switch (piece.getSlot()) {
            case HELMET     -> Material.DIAMOND_HELMET;
            case CHESTPLATE -> Material.DIAMOND_CHESTPLATE;
            case LEGGINGS   -> Material.DIAMOND_LEGGINGS;
            case BOOTS      -> Material.DIAMOND_BOOTS;
            case WEAPON, MAIN_HAND -> Material.DIAMOND_SWORD;
            case OFFHAND, OFF_HAND -> Material.SHIELD;
            default         -> Material.DIAMOND;
        };

        ItemStack item = new ItemStack(mat);
        var meta = item.getItemMeta();
        meta.displayName(net.kyori.adventure.text.Component.text(piece.getPieceName()));

        List<net.kyori.adventure.text.Component> lore = new java.util.ArrayList<>();
        lore.add(net.kyori.adventure.text.Component.text("§5【アーティファクト】 " + piece.getSetType().getDisplayName()));
        lore.add(net.kyori.adventure.text.Component.text("§7スロット: " + piece.getSlot().getDisplayName()));
        lore.add(net.kyori.adventure.text.Component.text(""));
        if (piece.getBonusAtk() > 0) lore.add(net.kyori.adventure.text.Component.text("§cATK §f+" + piece.getBonusAtk()));
        if (piece.getBonusDef() > 0) lore.add(net.kyori.adventure.text.Component.text("§7DEF §f+" + piece.getBonusDef()));
        if (piece.getBonusHp()  > 0) lore.add(net.kyori.adventure.text.Component.text("§aHP  §f+" + piece.getBonusHp()));
        if (piece.getBonusMp()  > 0) lore.add(net.kyori.adventure.text.Component.text("§bMP  §f+" + piece.getBonusMp()));
        meta.lore(lore);
        item.setItemMeta(meta);

        return ArtifactManager.tagItem(item, piece.getPieceId());
    }

    private void giveOrDropItem(Player player, ItemStack item, String displayName) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) player.getWorld().dropItemNaturally(player.getLocation(), item);
        MythicRPG.msg(player, ChatColor.LIGHT_PURPLE + "§l【アーティファクト入手】§r §f" + displayName + " §eをドロップしました！");
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
        // drop_tables.yml の mob-table-map から引く
        String tableId = DropTableRegistry.getTableIdForMob(mobId);
        if (tableId != null) return tableId;
        // フォールバック: BOSS_ プレフィックス
        if (mobId.toUpperCase().startsWith("BOSS")) return "boss_standard";
        return null;
    }
}
