package com.woxloi.mythicrpg.skill.loader;

import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import com.woxloi.mythicrpg.skill.ResourceType;
import com.woxloi.mythicrpg.skill.Skill;
import com.woxloi.mythicrpg.skill.SkillTrigger;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector; /**
 * YAMLで定義されたスキルを実行するクラス。
 * effect フィールドに応じて処理を分岐する。
 */
public class YamlSkillExecutor  extends Skill {

    // YAMLから読み込む追加パラメーター
    private final String effect;
    private final double damageMultiplier;
    private final double fixedDamage;
    private final double range;
    private final String weapon;

    public YamlSkillExecutor (
            String id, String name, int unlockLevel, long cooldown,
            SkillTrigger trigger, ResourceType resourceType, double cost,
            String effect, double damageMultiplier, double fixedDamage,
            double range, String weapon
    ) {
        super(id, name, unlockLevel, cooldown, trigger, resourceType, cost);
        this.effect          = effect;
        this.damageMultiplier = damageMultiplier;
        this.fixedDamage     = fixedDamage;
        this.range           = range;
        this.weapon          = weapon;
    }

    public String getWeapon() { return weapon; }

    @Override
    public void execute(Player player) {
        switch (effect) {
            case "basic_attack"  -> doBasicAttack(player);
            case "area_damage"   -> doAreaDamage(player);
            case "projectile"    -> doProjectile(player);
            default              -> doBasicAttack(player);
        }
    }

    /* =====================
       basic_attack
     ===================== */
    private void doBasicAttack(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        LivingEntity target = getNearestTarget(player, range);
        if (target == null) return;

        double damage = data.getAttack() * damageMultiplier;
        target.damage(damage, player);

        Vector kb = target.getLocation().toVector()
                .subtract(player.getLocation().toVector())
                .normalize().multiply(0.3);
        target.setVelocity(kb);
    }

    /* =====================
       area_damage
     ===================== */
    private void doAreaDamage(Player player) {
        player.getWorld().spawnParticle(
                Particle.SWEEP_ATTACK, player.getLocation(), 12, 0.5, 0.2, 0.5, 0);
        player.playSound(player.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);

        player.getNearbyEntities(range, range, range).stream()
                .filter(e -> e instanceof LivingEntity && e != player)
                .map(e -> (LivingEntity) e)
                .forEach(e -> e.damage(fixedDamage, player));
    }

    /* =====================
       projectile
     ===================== */
    private void doProjectile(Player player) {
        player.launchProjectile(SmallFireball.class);
    }

    /* =====================
       ヘルパー
     ===================== */
    private LivingEntity getNearestTarget(Player player, double r) {
        for (var entity : player.getNearbyEntities(r, r, r)) {
            if (entity instanceof LivingEntity living && living != player) {
                return living;
            }
        }
        return null;
    }
}
