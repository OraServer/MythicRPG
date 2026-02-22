package com.woxloi.mythicrpg.equipment.forge;

import com.woxloi.mythicrpg.MythicRPG;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 鍛冶台GUI（54スロット）。
 * 上段にレシピ一覧を表示し、クリックで素材確認・製作実行へ進む。
 */
public class ForgeGUI {

    private static final String TITLE = "§8⚒ §6鍛冶台";
    private static final int SIZE = 54;

    private ForgeGUI() {}

    /**
     * プレイヤーに鍛冶台GUIを開く。
     */
    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);

        List<ForgeRecipe> recipes = ForgeManager.getAllRecipes();
        for (int i = 0; i < Math.min(recipes.size(), 45); i++) {
            inv.setItem(i, buildRecipeIcon(recipes.get(i)));
        }

        // ボーダー装飾（下段）
        ItemStack border = buildBorder();
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, border);
        }

        player.openInventory(inv);
    }

    /**
     * レシピアイコンを生成。
     */
    private static ItemStack buildRecipeIcon(ForgeRecipe recipe) {
        ItemStack icon = new ItemStack(Material.ANVIL);
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) return icon;

        meta.setDisplayName("§6" + recipe.getName());

        List<String> lore = new ArrayList<>(recipe.getIngredientLore());
        lore.add("");
        if (recipe.hasFixedOutput()) {
            lore.add("§a成果物: §f" + recipe.getOutputItemId());
        } else {
            lore.add("§a成果物: §fランダム生成");
            lore.add("§7スロット: §f" + recipe.getOutputSlot().name());
            lore.add("§7レアリティ: §f" + recipe.getOutputRarity().name());
        }
        lore.add("");
        lore.add("§eクリックして製作");

        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    /**
     * ボーダー用装飾アイテム。
     */
    private static ItemStack buildBorder() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    /** GUIタイトルを返す（リスナーで判定用） */
    public static String getTitle() {
        return TITLE;
    }
}
