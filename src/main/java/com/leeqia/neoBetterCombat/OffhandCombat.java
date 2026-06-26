package com.leeqia.neoBetterCombat;

import java.lang.reflect.Method;

import com.leeqia.neoBetterCombat.network.OffhandCooldownPayload;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 副手攻击的服务器端执行。副手的命中计算独立于主手，
 * 因此目标被命中后的无敌时间不会在两隻手之间共享。
 */
public final class OffhandCombat {
    private static final Method KNOCKBACK_WITH_DAMAGE = findKnockback(
            double.class, double.class, double.class, DamageSource.class, float.class);
    private static final Method KNOCKBACK_SIMPLE = findKnockback(double.class, double.class, double.class);
    private static boolean loggedKnockbackFailure;

    private OffhandCombat() {}

    /** 计算给定副手物品的副手攻击冷却时间（以刻为单位）。 */
    public static int computeCooldown(ServerPlayer player, ItemStack offhand) {
        ItemAttributeModifiers mods = offhand.getAttributeModifiers();
        double speed = mods.compute(Attributes.ATTACK_SPEED,
                player.getAttributeBaseValue(Attributes.ATTACK_SPEED), EquipmentSlot.MAINHAND);
        if (speed <= 0.0D) {
            speed = 4.0D;
        }
        return Math.max(1, (int) Math.round(20.0D / speed));
    }

    public static int getCooldown(ServerPlayer player) {
        return player.getData(ModAttachments.OFFHAND_COOLDOWN.get());
    }

    public static void setCooldown(ServerPlayer player, int ticks) {
        player.setData(ModAttachments.OFFHAND_COOLDOWN.get(), ticks);
        PacketDistributor.sendToPlayer(player, new OffhandCooldownPayload(ticks));
    }

    /** 处理从客户端收到的副手攻击请求。 */
    public static void handle(ServerPlayer player, int entityId) {
        if (!Config.ENABLE_OFFHAND_ATTACK.get()) {
            return;
        }
        ItemStack offhand = player.getOffhandItem();
        ItemStack mainhand = player.getMainHandItem();
        if (offhand.isEmpty() || !Config.isOffhandAttackUsable(offhand, mainhand)) {
            return;
        }
        // 权威冷却门控：副手只有在冷却就绪后才能攻击。
        if (getCooldown(player) > 0) {
            return;
        }

        player.swing(InteractionHand.OFF_HAND);

        Entity target = entityId < 0 ? null : player.level().getEntity(entityId);
        boolean hit = target != null && canHit(player, target);
        if (hit) {
            performAttack(player, target, offhand);
        }

        if (hit || !Config.REFUND_ENERGY.get()) {
            setCooldown(player, computeCooldown(player, offhand));
        }
    }

    private static boolean canHit(ServerPlayer player, Entity target) {
        if (target == player || !target.isAttackable() || target.skipAttackInteraction(player)) {
            return false;
        }
        if (!Config.isEntityAttackable(target)) {
            return false;
        }
        if (Config.PROTECT_OWNED_PETS.get() && target instanceof TamableAnimal pet && pet.isOwnedBy(player)) {
            return false;
        }
        return true;
    }

    private static void performAttack(ServerPlayer player, Entity target, ItemStack weapon) {
        ServerLevel level = (ServerLevel) player.level();
        DamageSource source = player.damageSources().playerAttack(player);

        float base = (float) weapon.getAttributeModifiers().compute(Attributes.ATTACK_DAMAGE,
                player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE), EquipmentSlot.MAINHAND);

        // 基于冷却的力量缩放（镜像原版的 0.2 + s² × 0.8 公式）。
        int cd = computeCooldown(player, weapon);
        int current = getCooldown(player);
        float strength = current <= 0 ? 1.0F : Math.max(0.0F, 1.0F - (float) current / (float) cd);
        base *= 0.2F + strength * strength * 0.8F;

        float damage = EnchantmentHelper.modifyDamage(level, weapon, target, source, base);

        var crit = CommonHooks.fireCriticalHit(player, target, false, 1.0F);
        boolean isCrit = crit.isCriticalHit();
        if (isCrit) {
            damage *= crit.getDamageMultiplier();
        }

        if (Config.WEAKER_OFFHAND.get()) {
            damage *= (float) (double) Config.OFFHAND_EFFICIENCY.get();
        }

        // 重置目标的无敌时间，使副手命中独立于主手生效。
        int savedInvuln = target.invulnerableTime;
        target.invulnerableTime = 0;
        int savedHurt = 0;
        if (target instanceof LivingEntity living) {
            savedHurt = living.hurtTime;
            living.hurtTime = 0;
        }

        boolean hurt = target.hurtServer(level, source, damage);

        if (hurt) {
            if (isCrit) {
                player.crit(target);
            }
            EnchantmentHelper.doPostAttackEffectsWithItemSource(level, target, source, weapon);
            player.setLastHurtMob(target);

            // 副手攻击现在也能进行横扫与主手一样。
            boolean sword = weapon.typeHolder().is(net.minecraft.tags.ItemTags.SWORDS);
            if (Config.MORE_SWEEP.get() || sword) {
                doSweep(player, level, target, weapon, base, source);
            }

            // 副手攻击现在会消耗副手武器的耐久度。
            int perAttack = damagePerAttack(weapon);
            if (perAttack > 0) {
                weapon.hurtAndBreak(perAttack, player, EquipmentSlot.OFFHAND);
            }
        } else {
            target.invulnerableTime = savedInvuln;
            if (target instanceof LivingEntity living) {
                living.hurtTime = savedHurt;
            }
        }
    }

    private static int damagePerAttack(ItemStack weapon) {
        net.minecraft.world.item.component.Weapon w =
                weapon.get(net.minecraft.core.component.DataComponents.WEAPON);
        return w != null ? w.itemDamagePerAttack() : 1;
    }

    /** 副手武器的横扫攻击，镜像原版 {@code Player#doSweepAttack}。 */
    private static void doSweep(ServerPlayer player, ServerLevel level, Entity target,
            ItemStack weapon, float baseDamage, DamageSource source) {
        float sweepDamage = 1.0F + (float) player.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * baseDamage;
        double reachSq = net.minecraft.util.Mth.square(player.entityInteractionRange());
        net.minecraft.world.phys.AABB box = target.getBoundingBox().inflate(1.0D, 0.25D, 1.0D);

        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (nearby != player && nearby != target && !player.isAlliedTo(nearby)
                    && !(nearby instanceof net.minecraft.world.entity.decoration.ArmorStand stand && stand.isMarker())
                    && player.distanceToSqr(nearby) < reachSq) {
                if (nearby.hurtServer(level, source, sweepDamage)) {
                    applySweepKnockback(nearby, source, sweepDamage,
                            net.minecraft.util.Mth.sin(player.getYRot() * (float) (Math.PI / 180.0)),
                            -net.minecraft.util.Mth.cos(player.getYRot() * (float) (Math.PI / 180.0)));
                    EnchantmentHelper.doPostAttackEffects(level, nearby, source);
                }
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
        spawnSweepParticle(level, player);
    }

    private static Method findKnockback(Class<?>... parameterTypes) {
        try {
            return LivingEntity.class.getMethod("knockback", parameterTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    private static void applySweepKnockback(LivingEntity target, DamageSource source, float damage, double x, double z) {
        try {
            if (KNOCKBACK_WITH_DAMAGE != null) {
                KNOCKBACK_WITH_DAMAGE.invoke(target, 0.4D, x, z, source, damage);
                return;
            }
            if (KNOCKBACK_SIMPLE != null) {
                KNOCKBACK_SIMPLE.invoke(target, 0.4D, x, z);
            }
        } catch (ReflectiveOperationException | IllegalArgumentException ex) {
            if (!loggedKnockbackFailure) {
                loggedKnockbackFailure = true;
                NeoBetterCombat.LOGGER.warn("Unable to apply sweep knockback; continuing without knockback", ex);
            }
        }
    }

    /** 在玩家前方发送原版横扫粒子。 */
    public static void spawnSweepParticle(ServerLevel level, net.minecraft.world.entity.player.Player player) {
        double dx = -net.minecraft.util.Mth.sin(player.getYRot() * (float) (Math.PI / 180.0));
        double dz = net.minecraft.util.Mth.cos(player.getYRot() * (float) (Math.PI / 180.0));
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK,
                player.getX() + dx, player.getY(0.5), player.getZ() + dz, 0, dx, 0.0, dz, 0.0);
    }
}
