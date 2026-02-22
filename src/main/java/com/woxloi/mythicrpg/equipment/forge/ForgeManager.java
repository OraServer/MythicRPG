package com.woxloi.mythicrpg.equipment.forge;

import com.woxloi.mythicrpg.equipment.EquipmentRegistry;
import com.woxloi.mythicrpg.equipment.model.EquipRarity;
import com.woxloi.mythicrpg.equipment.model.EquipSlot;
import com.woxloi.mythicrpg.equipment.model.RpgItem;
import com.woxloi.mythicrpg.equipment.random.RandomItemGenerator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 鍛冶（フォージ）システム。
 * 素材の組み合わせで固定レシピまたランダム装備を生成する。
 */
public class ForgeManager {

    private static final Map<String, ForgeRecipe> recipes = new HashMap<>();
    private static final Random RANDOM = new Random();

    static {
        loadDefaultRecipes();
    }

    private static void loadDefaultRecipes() {
        // ドラゴン剣レシピ
        Map<String, Integer> dragonIngredients = new HashMap<>();
        dragonIngredients.put(Material.DRAGON_BREATH.name(), 3);
        dragonIngredients.put(Material.DIAMOND.name(), 5);
        dragonIngredients.put(Material.BLAZE_ROD.name(), 2);
        ForgeRecipe dragonSword = new ForgeRecipe(
            "dragon_sword_recipe", "ドラゴンの剣",
            dragonIngredients, "dragon_sword",
            null, null, 30, 500
        );
        recipes.put(dragonSword.getId(), dragonSword);

        // 蒼天の兜レシピ
        Map<String, Integer> helmIngredients = new HashMap<>();
        helmIngredients.put(Material.FEATHER.name(), 10);
        helmIngredients.put(Material.DIAMOND_HELMET.name(), 1);
        helmIngredients.put(Material.LAPIS_LAZULI.name(), 8);
        ForgeRecipe skyHelm = new ForgeRecipe(
            "sky_helm_recipe", "蒼天の兜",
            helmIngredients, "sky_helmet",
            null, null, 20, 300
        );
        recipes.put(skyHelm.getId(), skyHelm);

        // ランダムレアリティ生成レシピ（RARE）
        Map<String, Integer> rareIngredients = new HashMap<>();
        rareIngredients.put(Material.IRON_INGOT.name(), 10);
        rareIngredients.put(Material.GOLD_INGOT.name(), 5);
        ForgeRecipe rareRandom = new ForgeRecipe(
            "random_rare_weapon", "ランダム武器（RARE）",
            rareIngredients, null,
            EquipSlot.WEAPON, EquipRarity.RARE, 15, 200
        );
        recipes.put(rareRandom.getId(), rareRandom);
    }

    /**
     * 素材をチェックしてフォージを実行。成功時にRpgItemを返す（null=失敗）。
     */
    public static RpgItem forge(Player player, String recipeId) {
        ForgeRecipe recipe = recipes.get(recipeId);
        if (recipe == null) return null;
        return craft(player, recipe) ? buildOutput(recipe) : null;
    }

    /**
     * ForgeGUIListenerからの呼び出し用。素材消費してtrueを返す。
     */
    public static boolean craft(Player player, ForgeRecipe recipe) {
        // 素材チェック
        for (Map.Entry<String, Integer> entry : recipe.getIngredients().entrySet()) {
            Material mat = Material.matchMaterial(entry.getKey());
            if (mat == null) return false;
            if (player.getInventory().contains(mat, entry.getValue())) continue;
            return false;
        }
        // 素材消費
        for (Map.Entry<String, Integer> entry : recipe.getIngredients().entrySet()) {
            Material mat = Material.matchMaterial(entry.getKey());
            if (mat == null) continue;
            player.getInventory().removeItem(new org.bukkit.inventory.ItemStack(mat, entry.getValue()));
        }
        return true;
    }

    private static RpgItem buildOutput(ForgeRecipe recipe) {
        if (recipe.hasFixedOutput()) {
            return EquipmentRegistry.get(recipe.getOutputItemId());
        } else {
            return RandomItemGenerator.generate(recipe.getOutputRarity(), new Random());
        }
    }

    /** ランダムフォージ（素材に応じてランダムアイテム生成）。 */
    public static RpgItem randomForge(EquipRarity rarity) {
        return RandomItemGenerator.generate(rarity, RANDOM);
    }

    public static Map<String, ForgeRecipe> getRecipes() { return recipes; }

    public static List<ForgeRecipe> getAllRecipes() { return new ArrayList<>(recipes.values()); }

    public static void registerRecipe(ForgeRecipe recipe) {
        recipes.put(recipe.getId(), recipe);
    }
}
