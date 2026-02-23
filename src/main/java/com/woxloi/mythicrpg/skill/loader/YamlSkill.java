package com.woxloi.mythicrpg.skill.loader;

import com.woxloi.mythicrpg.MythicRPG;
import com.woxloi.mythicrpg.player.PlayerData;
import com.woxloi.mythicrpg.player.PlayerDataManager;
import com.woxloi.mythicrpg.skill.ResourceType;
import com.woxloi.mythicrpg.skill.Skill;
import com.woxloi.mythicrpg.skill.SkillTrigger;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.WitherSkull;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.List;

/**
 * YAMLで定義されたスキルを実行するクラス。
 * effect フィールドに応じて処理を分岐する。
 *
 * 対応エフェクト:
 *   basic_attack   - 最近傍ターゲットに攻撃力×倍率
 *   area_damage    - 範囲ダメージ（固定値）
 *   projectile     - 飛び道具発射
 *   chain_attack   - 複数ターゲットへの連鎖攻撃
 *   self_heal      - 自己HP回復
 *   buff_speed     - 移動速度バフ付与
 *   buff_strength  - 攻撃バフ付与
 *   dash           - 前方ダッシュ + 衝突ダメージ
 *   thunder        - 落雷召喚
 *   ice_arrow      - 氷矢（減速付与）
 *   mp_regen       - MP即時回復
 *   taunt          - 周囲Mobのターゲットを自分に
 */
public class YamlSkill extends Skill {

    private final String effect;
    private final double damageMultiplier;
    private final double fixedDamage;
    private final double range;
    private final String weapon;

    public YamlSkill(
            String id, String name, int unlockLevel, long cooldown,
            SkillTrigger trigger, ResourceType resourceType, double cost,
            String effect, double damageMultiplier, double fixedDamage,
            double range, String weapon
    ) {
        super(id, name, unlockLevel, cooldown, trigger, resourceType, cost);
        this.effect           = effect;
        this.damageMultiplier = damageMultiplier;
        this.fixedDamage      = fixedDamage;
        this.range            = range;
        this.weapon           = weapon;
    }

    public String getWeapon() { return weapon; }

    @Override
    public void execute(Player player) {
        switch (effect) {
            case "basic_attack"  -> doBasicAttack(player);
            case "area_damage"   -> doAreaDamage(player);
            case "projectile"    -> doProjectile(player);
            case "chain_attack"  -> doChainAttack(player);
            case "self_heal"     -> doSelfHeal(player);
            case "buff_speed"    -> doBuffSpeed(player);
            case "buff_strength" -> doBuffStrength(player);
            case "dash"          -> doDash(player);
            case "thunder"       -> doThunder(player);
            case "ice_arrow"     -> doIceArrow(player);
            case "mp_regen"      -> doMpRegen(player);
            case "taunt"         -> doTaunt(player);
            default              -> doBasicAttack(player);
        }
    }

    // ──────────────────────────────────────────
    //  basic_attack: 最近傍1体に攻撃力×倍率
    // ──────────────────────────────────────────
    private void doBasicAttack(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        LivingEntity target = getNearestTarget(player, range);
        if (target == null) { noTarget(player); return; }

        double damage = (data.getAttack() + data.getEquipAttackBonus()) * damageMultiplier;
        target.damage(damage, player);
        knockback(target, player, 0.3);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 1f);
    }

    // ──────────────────────────────────────────
    //  area_damage: 範囲内全員に固定ダメージ
    // ──────────────────────────────────────────
    private void doAreaDamage(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation(), 20, 1.0, 0.5, 1.0, 0);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.8f);

        double damage = fixedDamage > 0 ? fixedDamage
                : (data.getAttack() + data.getEquipAttackBonus()) * damageMultiplier;

        getTargetsInRange(player, range).forEach(e -> {
            e.damage(damage, player);
            knockback(e, player, 0.5);
        });
    }

    // ──────────────────────────────────────────
    //  projectile: 飛び道具
    // ──────────────────────────────────────────
    private void doProjectile(Player player) {
        player.launchProjectile(SmallFireball.class);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1.2f);
        player.getWorld().spawnParticle(Particle.FLAME, player.getEyeLocation(), 10, 0.1, 0.1, 0.1, 0.05);
    }

    // ──────────────────────────────────────────
    //  chain_attack: 連鎖攻撃（最大5体）
    // ──────────────────────────────────────────
    private void doChainAttack(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        List<LivingEntity> targets = getTargetsInRange(player, range);
        if (targets.isEmpty()) { noTarget(player); return; }

        double damage = (data.getAttack() + data.getEquipAttackBonus()) * damageMultiplier;
        double chainDamage = damage;
        int count = 0;

        for (LivingEntity target : targets) {
            if (count >= 5) break;
            target.damage(chainDamage, player);
            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation(), 8, 0.3, 0.3, 0.3, 0.1);
            chainDamage *= 0.75; // 連鎖するたびにダメージ75%に減衰
            count++;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1f);
        MythicRPG.msg(player, "§e" + count + "体に連鎖攻撃！");
    }

    // ──────────────────────────────────────────
    //  self_heal: 自己HP回復
    // ──────────────────────────────────────────
    private void doSelfHeal(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        double healAmount = fixedDamage > 0 ? fixedDamage
                : data.getMaxHp() * damageMultiplier;

        data.addHp(healAmount);
        // Bukkit HPも同期
        double ratio = data.getHp() / Math.max(1, data.getMaxHp());
        double maxBukkit = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxBukkit, maxBukkit * ratio));

        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 12, 0.5, 0.5, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        MythicRPG.msg(player, "§a+" + (int)healAmount + " HP 回復！");
    }

    // ──────────────────────────────────────────
    //  buff_speed: 移動速度バフ（10秒）
    // ──────────────────────────────────────────
    private void doBuffSpeed(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, false, true, true));
        player.getWorld().spawnParticle(Particle.SPELL_INSTANT, player.getLocation(), 20, 0.5, 1, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
        MythicRPG.msg(player, "§a疾風！移動速度アップ (10秒)");
    }

    // ──────────────────────────────────────────
    //  buff_strength: 攻撃力バフ（10秒）
    // ──────────────────────────────────────────
    private void doBuffStrength(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 0, false, true, true));
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation(), 20, 0.5, 1, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 2f);
        MythicRPG.msg(player, "§c闘気！攻撃力アップ (10秒)");
    }

    // ──────────────────────────────────────────
    //  dash: 前方ダッシュ + 衝突ダメージ
    // ──────────────────────────────────────────
    private void doDash(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        Vector dir = player.getLocation().getDirection().normalize().multiply(1.5);
        dir.setY(0.3);
        player.setVelocity(dir);

        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.3, 0.3, 0.3, 0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);

        // 1tick後に衝突チェック
        MythicRPG.getInstance().getServer().getScheduler().runTaskLater(MythicRPG.getInstance(), () -> {
            double damage = (data.getAttack() + data.getEquipAttackBonus()) * damageMultiplier;
            getTargetsInRange(player, 2.0).forEach(e -> e.damage(damage, player));
        }, 3L);
    }

    // ──────────────────────────────────────────
    //  thunder: 落雷召喚
    // ──────────────────────────────────────────
    private void doThunder(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        LivingEntity target = getNearestTarget(player, range);
        if (target == null) { noTarget(player); return; }

        player.getWorld().strikeLightning(target.getLocation());
        double damage = fixedDamage > 0 ? fixedDamage
                : (data.getAttack() + data.getEquipAttackBonus()) * damageMultiplier;
        target.damage(damage, player);
        MythicRPG.msg(player, "§e⚡ 落雷！");
    }

    // ──────────────────────────────────────────
    //  ice_arrow: 氷矢（スノーボール + 減速付与）
    // ──────────────────────────────────────────
    private void doIceArrow(Player player) {
        player.launchProjectile(Snowball.class);
        player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1f, 1.2f);
        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getEyeLocation(), 8, 0.1, 0.1, 0.1, 0.05);

        // ヒット時の処理はProjectileHitEventで拾うのが本来だが、
        // YAMLスキル簡易実装として発射のみ（ダメージはバニラ+DEF計算）
        // 近傍ターゲットにも即時スロー付与
        LivingEntity target = getNearestTarget(player, range);
        if (target != null) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.getByName("slow") != null
                    ? PotionEffectType.SLOW : PotionEffectType.SLOW, 60, 1));
        }
    }

    // ──────────────────────────────────────────
    //  mp_regen: MP即時回復
    // ──────────────────────────────────────────
    private void doMpRegen(Player player) {
        PlayerData data = PlayerDataManager.get(player);
        if (data == null) return;

        double mpAmount = fixedDamage > 0 ? fixedDamage : data.getMaxMp() * damageMultiplier;
        data.addMp(mpAmount);

        player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().add(0,1,0), 20, 0.5, 0.5, 0.5, 0.05);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f);
        MythicRPG.msg(player, "§b+" + (int)mpAmount + " MP 回復！");
    }

    // ──────────────────────────────────────────
    //  taunt: 周囲Mobを引き付ける
    // ──────────────────────────────────────────
    private void doTaunt(Player player) {
        List<LivingEntity> targets = getTargetsInRange(player, range);
        if (targets.isEmpty()) { MythicRPG.msg(player, "§7近くにターゲットがいません"); return; }

        player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation(), 30, 1, 1, 1, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2f);

        // 減速 + ターゲット引き付け
        targets.forEach(e -> {
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0));
            Vector pull = player.getLocation().toVector()
                    .subtract(e.getLocation().toVector()).normalize().multiply(0.5);
            e.setVelocity(pull);
        });

        MythicRPG.msg(player, "§6挑発！" + targets.size() + "体を引き付けた！");
    }

    // ──────────────────────────────────────────
    //  ヘルパー
    // ──────────────────────────────────────────
    private LivingEntity getNearestTarget(Player player, double r) {
        return player.getNearbyEntities(r, r, r).stream()
                .filter(e -> e instanceof LivingEntity && e != player)
                .map(e -> (LivingEntity) e)
                .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(player.getLocation())))
                .orElse(null);
    }

    private List<LivingEntity> getTargetsInRange(Player player, double r) {
        return player.getNearbyEntities(r, r, r).stream()
                .filter(e -> e instanceof LivingEntity && e != player)
                .map(e -> (LivingEntity) e)
                .toList();
    }

    private void knockback(LivingEntity target, Player from, double strength) {
        Vector kb = target.getLocation().toVector()
                .subtract(from.getLocation().toVector())
                .normalize().multiply(strength);
        kb.setY(0.2);
        target.setVelocity(kb);
    }

    private void noTarget(Player player) {
        MythicRPG.msg(player, "§7ターゲットが見つかりません");
    }
}
