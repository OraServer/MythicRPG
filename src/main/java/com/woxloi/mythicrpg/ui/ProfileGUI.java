package com.woxloi.mythicrpg.ui;

import com.woxloi.mythicrpg.buff.BuffManager;
import com.woxloi.mythicrpg.combo.ComboRepository;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import com.woxloi.mythicrpg.stats.StatPoint;
import com.woxloi.mythicrpg.stats.StatPointManager;
import com.woxloi.mythicrpg.title.TitleManager;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * /mrpg profile で開く総合プロフィールGUI
 */
public class ProfileGUI implements Listener {

    private static final String TITLE = "§d§lキャラクタープロフィール";

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(TITLE));
        PlayerData data = PlayerDataManager.get(player);
        StatPoint sp    = StatPointManager.get(player.getUniqueId());
        if (data == null) return;

        // ─── スカルアイテム (中央上) ───
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) skull.getItemMeta();
        sm.setOwningPlayer(player);
        String titleTag = TitleManager.getDisplayTag(player.getUniqueId());
        sm.displayName(Component.text("§e§l" + titleTag + player.getName()));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7ジョブ: §f" + (data.hasJob() ? data.getJob().getDisplayName() : "未選択")));
        lore.add(Component.text("§7レベル: §a" + data.getLevel()));
        lore.add(Component.text("§7EXP: §f" + (int) data.getExp()));
        sm.lore(lore);
        skull.setItemMeta(sm);
        inv.setItem(4, skull);

        // ─── ステータス ───
        inv.setItem(19, makeInfo(Material.RED_DYE,      "§cHP",     "§f" + (int) data.getHp() + " / " + (int) data.getMaxHp()));
        inv.setItem(20, makeInfo(Material.BLUE_DYE,     "§9MP",     "§f" + (int) data.getMp() + " / " + (int) data.getMaxMp()));
        inv.setItem(21, makeInfo(Material.YELLOW_DYE,   "§eSP",     "§f" + (int) data.getSp() + " / " + (int) data.getMaxSp()));
        inv.setItem(22, makeInfo(Material.DIAMOND_SWORD,"§c攻撃力", "§f" + (int) data.getAttack()));

        // ─── ポイント振り分け ───
        inv.setItem(28, makeInfo(Material.DIAMOND_SWORD, "§c筋力(STR)", sp.getStrPoints() + "pt  →  ATK +" + (int) sp.getBonusAttack()));
        inv.setItem(29, makeInfo(Material.APPLE,          "§a体力(VIT)", sp.getVitPoints() + "pt  →  MaxHP +" + (int) sp.getBonusMaxHp()));
        inv.setItem(30, makeInfo(Material.LAPIS_LAZULI,   "§9知力(INT)", sp.getIntPoints() + "pt  →  MaxMP +" + (int) sp.getBonusMaxMp()));
        inv.setItem(31, makeInfo(Material.FEATHER,        "§e俊敏(AGI)", sp.getAgiPoints() + "pt  →  MaxSP +" + (int) sp.getBonusMaxSp()));
        inv.setItem(32, makeInfo(Material.EXPERIENCE_BOTTLE, "§a未割振りポイント", sp.getFreePoints() + "pt  (クリック→振り分けGUI)"));

        // ─── バフ一覧 ───
        var buffs = BuffManager.getBuffs(player);
        inv.setItem(37, makeInfo(Material.POTION, "§aアクティブバフ/デバフ",
                buffs.isEmpty() ? "§7なし" : buffs.stream().map(b -> b.getType().getDisplayName()).toList().toString()));

        // ─── コンボ最高記録 ───
        int maxCombo = ComboRepository.getMaxCombo(player.getUniqueId());
        inv.setItem(38, makeInfo(Material.BLAZE_ROD, "§6最高コンボ数", maxCombo + " HIT"));

        // ─── 称号数 ───
        inv.setItem(39, makeInfo(Material.NETHER_STAR, "§e解放済み称号",
                TitleManager.getUnlocked(player).size() + " / " + com.woxloi.mythicrpg.title.TitleDefinition.values().length));

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().title().equals(Component.text(TITLE))) return;
        event.setCancelled(true);

        // ポイント振り分けGUIへ
        if (event.getRawSlot() == 32) {
            com.woxloi.mythicrpg.stats.StatGUI.open(player);
        }
    }

    private static ItemStack makeInfo(Material mat, String name, String value) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(List.of(Component.text("§f" + value)));
        item.setItemMeta(meta);
        return item;
    }
}
