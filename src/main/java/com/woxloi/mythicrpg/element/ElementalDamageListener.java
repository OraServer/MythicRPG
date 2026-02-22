package com.woxloi.mythicrpg.element;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

/**
 * EntityDamageByEntityEvent をフックし、属性相性倍率をダメージに適用する。
 * 攻撃側の武器属性と防御側のMob/プレイヤー属性から倍率を計算する。
 */
public class ElementalDamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim  = event.getEntity();

        // 攻撃側の武器属性を取得
        ElementType attackElement = ElementType.NONE;
        if (damager instanceof Player attacker) {
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            attackElement = ElementManager.getItemElement(weapon);
        }

        if (attackElement == ElementType.NONE) return; // 無属性は処理不要

        // 防御側の属性を取得
        ElementType defenseElement = ElementManager.getMobElement(victim);

        // プレイヤーが被攻撃の場合はそのUUID、Mobはnull
        java.util.UUID defenderUuid = (victim instanceof Player p) ? p.getUniqueId() : null;

        // 倍率計算
        double multiplier = ElementManager.calcDamageMultiplier(
                attackElement, defenderUuid, defenseElement);

        if (multiplier == 1.0) return; // 等倍は何もしない

        // ダメージ適用
        double newDamage = event.getDamage() * multiplier;
        event.setDamage(newDamage);

        // 属性効果のフィードバック表示
        if (victim instanceof LivingEntity livingVictim) {
            sendElementFeedback(damager, attackElement, defenseElement, multiplier);
        }
    }

    private void sendElementFeedback(Entity damager, ElementType atk,
                                      ElementType def, double multiplier) {
        if (!(damager instanceof Player player)) return;

        String msg;
        if (multiplier >= 2.0) {
            msg = atk.getIcon() + " §c§l弱点！§r " + atk.getDisplayName()
                    + " §7→ " + def.getDisplayName() + " §c×" + String.format("%.1f", multiplier);
        } else if (multiplier <= 0.5) {
            msg = atk.getIcon() + " §b耐性… §r" + atk.getDisplayName()
                    + " §7→ " + def.getDisplayName() + " §b×" + String.format("%.1f", multiplier);
        } else if (multiplier > 1.0) {
            msg = atk.getIcon() + " §e効果的！ §r×" + String.format("%.1f", multiplier);
        } else {
            msg = atk.getIcon() + " §7今ひとつ… §r×" + String.format("%.1f", multiplier);
        }

        player.sendActionBar(Component.text(msg));
    }
}
