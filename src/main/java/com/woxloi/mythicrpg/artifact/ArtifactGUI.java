package com.woxloi.mythicrpg.artifact;

import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * /mrpg artifact で開くアーティファクトセット確認GUI。
 *
 * 54スロット構成:
 *  0-8  行:  セット一覧 (最大9セット、現在10種)
 *  各スロット: セット名・説明・装備数・発動状況を表示
 */
public class ArtifactGUI implements Listener {

    private static final String TITLE = "§5§lアーティファクトセット";

    /** GUIを開く */
    public static void open(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        // 現在の装備枚数を取得
        Map<ArtifactType, Integer> counts = ArtifactManager.countEquipped(player);

        Inventory inv = Bukkit.createInventory(null, 54, Component.text(TITLE));

        // フィラー
        ItemStack filler = fillerItem();
        for (int i = 0; i < 54; i++) inv.setItem(i, filler);

        // セット一覧 (上段: 0~8、下段: 9~17)
        ArtifactType[] types = ArtifactType.values();
        for (int i = 0; i < types.length && i < 18; i++) {
            ArtifactType type  = types[i];
            int          count = counts.getOrDefault(type, 0);
            inv.setItem(i, buildSetItem(type, count));
        }

        // 下段: 装備中のアーティファクトアイテム表示
        int slot = 27;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (ArtifactManager.isArtifact(armor)) {
                inv.setItem(slot++, highlightItem(armor));
            }
        }
        ItemStack main = player.getInventory().getItemInMainHand();
        if (ArtifactManager.isArtifact(main)) inv.setItem(slot++, highlightItem(main));
        ItemStack off = player.getInventory().getItemInOffHand();
        if (ArtifactManager.isArtifact(off)) inv.setItem(slot, highlightItem(off));

        // 情報アイテム (スロット 49)
        inv.setItem(49, infoItem(counts));

        player.openInventory(inv);
    }

    /** セット1つのアイテム */
    private static ItemStack buildSetItem(ArtifactType type, int count) {
        Material mat = switch (type) {
            case DRAGON_SLAYER    -> Material.DRAGON_EGG;
            case SHADOW_ASSASSIN  -> Material.ENDER_EYE;
            case STORM_ARCHER     -> Material.BOW;
            case IRON_FORTRESS    -> Material.IRON_BLOCK;
            case EARTH_GUARDIAN   -> Material.OAK_LOG;
            case ARCANE_SCHOLAR   -> Material.ENCHANTING_TABLE;
            case FROST_WITCH      -> Material.PACKED_ICE;
            case HERO_OF_LIGHT    -> Material.BEACON;
            case WANDERER         -> Material.COMPASS;
            case ANCIENT_KING     -> Material.NETHER_STAR;
        };

        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        int tier = type.getActiveTier(count);

        meta.displayName(Component.text(type.getDisplayName()
                + (tier > 0 ? " §a§l[" + tier + "段階発動中]" : "")));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7" + type.getDescription()));
        lore.add(Component.text(""));
        lore.add(Component.text("§e装備数: §f" + count + "/" + totalPieces(type) + " ピース"));
        lore.add(Component.text(""));

        int[] reqs = type.getPiecesRequired();
        String[] descs = type.getBonusDescriptions();
        for (int i = 0; i < reqs.length; i++) {
            boolean active = count >= reqs[i];
            String prefix = active ? "§a✔ §f" : "§8● §7";
            lore.add(Component.text(prefix + reqs[i] + "セット: " + descs[i]));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static int totalPieces(ArtifactType type) {
        return ArtifactRegistry.ofSet(type).size();
    }

    /** 装備中アイテムをハイライト */
    private static ItemStack highlightItem(ItemStack original) {
        if (original == null) return null;
        ItemStack copy = original.clone();
        ItemMeta  meta = copy.getItemMeta();
        if (meta == null) return copy;

        String id = ArtifactManager.getArtifactId(original);
        ArtifactPiece piece = id != null ? ArtifactRegistry.get(id) : null;

        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(Component.text(""));
        if (piece != null) {
            lore.add(Component.text("§5[アーティファクト] §7" + piece.getSetType().getDisplayName()));
        }
        meta.lore(lore);
        copy.setItemMeta(meta);
        return copy;
    }

    /** 総合情報アイテム */
    private static ItemStack infoItem(Map<ArtifactType, Integer> counts) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(Component.text("§e§l装備中セット情報"));

        List<Component> lore = new ArrayList<>();
        boolean any = false;
        for (Map.Entry<ArtifactType, Integer> e : counts.entrySet()) {
            int tier = e.getKey().getActiveTier(e.getValue());
            if (tier > 0) {
                lore.add(Component.text("§a" + e.getKey().getDisplayName()
                        + " §7(" + e.getValue() + "ピース / " + tier + "段階)"));
                any = true;
            }
        }
        if (!any) lore.add(Component.text("§7セットボーナスは未発動です"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack fillerItem() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player)) return;

        // タイトル一致確認（Adventure対応）
        if (!e.getView().title().equals(Component.text(TITLE))) return;

        if (e.getClickedInventory() == null) return;

        // 上部GUIは完全キャンセル
        if (e.getClickedInventory().equals(e.getView().getTopInventory())) {
            e.setCancelled(true);
        }

        // Shiftクリック対策（GUIへ流入防止）
        if (e.isShiftClick()) {
            e.setCancelled(true);
        }
    }
}
