package com.leeqia.neoBetterCombat;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.SweepAttackEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class CombatEvents {
    private static final ConcurrentHashMap<UUID, Boolean> LAST_CRIT = new ConcurrentHashMap<>();

    private CombatEvents() {}

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        boolean crit = event.isCriticalHit();
        if (!crit && Config.RANDOM_CRITS.get() && !player.isSprinting()
                && player.getRandom().nextFloat() < (float) (double) Config.CRIT_CHANCE.get()) {
            event.setCriticalHit(true);
            event.setDamageMultiplier(1.5F);
            crit = true;
        }
        LAST_CRIT.put(player.getUUID(), crit);

        if (crit && Config.RANDOM_CRITS.get()) {
            breakShield(event.getTarget());
        }
    }

    private static void breakShield(Entity target) {
        if (target instanceof LivingEntity living && living.level() instanceof ServerLevel level) {
            ItemStack blocking = living.getItemBlockingWith();
            if (blocking != null) {
                BlocksAttacks blocksAttacks = blocking.get(DataComponents.BLOCKS_ATTACKS);
                if (blocksAttacks != null) {
                    blocksAttacks.disable(level, living, 5.0F, blocking);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onSweepAttack(SweepAttackEvent event) {
        if (Config.MORE_SWEEP.get() && !event.isSweeping()) {
            event.setSweeping(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player player)) {
            return;
        }
        boolean crit = LAST_CRIT.remove(player.getUUID()) == Boolean.TRUE;

        SoundEvent sound = null;
        if (crit && Config.CRIT_SOUND.get()) {
            sound = ModSounds.CRITICAL_STRIKE.get();
        } else if (Config.HIT_SOUND.get()) {
            sound = ModSounds.SWORD_SLASH.get();
        }
        if (sound != null) {
            victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                    sound, player.getSoundSource(), 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        int cooldown = player.getData(ModAttachments.OFFHAND_COOLDOWN.get());
        if (cooldown > 0) {
            player.setData(ModAttachments.OFFHAND_COOLDOWN.get(), cooldown - 1);
        }
    }
}
