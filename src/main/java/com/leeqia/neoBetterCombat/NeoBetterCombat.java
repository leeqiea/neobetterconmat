package com.leeqia.neoBetterCombat;

import org.slf4j.Logger;

import com.leeqia.neoBetterCombat.network.MainhandAttackPayload;
import com.leeqia.neoBetterCombat.network.OffhandAttackPayload;
import com.leeqia.neoBetterCombat.network.OffhandCooldownPayload;
import com.mojang.logging.LogUtils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(NeoBetterCombat.MODID)
public class NeoBetterCombat {
    public static final String MODID = "neobettercombat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NeoBetterCombat(IEventBus modEventBus, ModContainer modContainer) {
        ModSounds.SOUNDS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener((ModConfigEvent.Loading e) -> Config.invalidate());
        modEventBus.addListener((ModConfigEvent.Reloading e) -> Config.invalidate());

        NeoForge.EVENT_BUS.register(CombatEvents.class);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            com.leeqia.neoBetterCombat.client.ClientCombat.init(modEventBus, modContainer);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(MainhandAttackPayload.TYPE, MainhandAttackPayload.CODEC, NeoBetterCombat::handleMainhand);
        registrar.playToServer(OffhandAttackPayload.TYPE, OffhandAttackPayload.CODEC, NeoBetterCombat::handleOffhand);
        // S2C：客户端处理器通过 RegisterClientPayloadHandlersEvent 单独注册。
        registrar.playToClient(OffhandCooldownPayload.TYPE, OffhandCooldownPayload.CODEC);
    }

    private static void handleMainhand(MainhandAttackPayload payload, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
        }
        Entity target = player.level().getEntity(payload.entityId());
        if (target == null || target == player) {
            return;
        }
        boolean wasSprinting = player.isSprinting();
        player.attack(target);
        if (Config.ATTACK_AND_SPRINT.get() && wasSprinting) {
            player.setSprinting(true);
        }
    }

    private static void handleOffhand(OffhandAttackPayload payload, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            OffhandCombat.handle(player, payload.entityId());
        }
    }
}
