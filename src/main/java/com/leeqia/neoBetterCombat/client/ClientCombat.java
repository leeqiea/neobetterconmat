package com.leeqia.neoBetterCombat.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import com.leeqia.neoBetterCombat.Config;
import com.leeqia.neoBetterCombat.ModAttachments;
import com.leeqia.neoBetterCombat.network.MainhandAttackPayload;
import com.leeqia.neoBetterCombat.network.OffhandAttackPayload;
import com.leeqia.neoBetterCombat.network.OffhandCooldownPayload;

import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientCombat {
    private static final Identifier ATTACK_INDICATOR_BG =
            Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_background");
    private static final Identifier ATTACK_INDICATOR_PROGRESS =
            Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_progress");
    private static final Field OPTIONS_HIDE_GUI = findField(net.minecraft.client.Options.class, "hideGui");
    private static final Field GUI_HUD = findField(net.minecraft.client.gui.Gui.class, "hud");
    private static final Method HUD_IS_HIDDEN = findMethod("net.minecraft.client.gui.Hud", "isHidden");

    private static boolean offhandHandled;

    private ClientCombat() {}

    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ClientCombat::onRegisterClientPayloads);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, parent) -> new ConfigurationScreen(container, parent));
        NeoForge.EVENT_BUS.register(ClientCombat.class);
    }

    private static void onRegisterClientPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(OffhandCooldownPayload.TYPE, ClientCombat::onCooldownSync);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!Config.ENABLE_OFFHAND_ATTACK.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || isHudHidden(mc) || !mc.options.getCameraType().isFirstPerson()
                || mc.options.attackIndicator().get() != AttackIndicatorStatus.CROSSHAIR) {
            return;
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty() || !Config.isOffhandAttackUsable(offhand, player.getMainHandItem())) {
            return;
        }
        int current = player.getData(ModAttachments.OFFHAND_COOLDOWN.get());
        if (current <= 0) {
            return; // 副手准备就绪：不需要指示器
        }
        int max = offhandMaxCooldown(player, offhand);
        float progress = Math.max(0.0F, 1.0F - (float) current / (float) max);

        GuiGraphicsExtractor g = event.getGuiGraphics();
        g.nextStratum();
        int y = g.guiHeight() / 2 - 7 + 16;
        int x = g.guiWidth() / 2 - 8 - 18;
        g.blitSprite(RenderPipelines.CROSSHAIR, ATTACK_INDICATOR_BG, x, y, 16, 4);
        int p = (int) (progress * 17.0F);
        if (p > 0) {
            g.blitSprite(RenderPipelines.CROSSHAIR, ATTACK_INDICATOR_PROGRESS, 16, 4, 0, 0, x, y, p, 4);
        }
    }

    private static Field findField(Class<?> owner, String name) {
        try {
            return owner.getField(name);
        } catch (NoSuchFieldException ex) {
            return null;
        }
    }

    private static Method findMethod(String className, String name) {
        try {
            return Class.forName(className).getMethod(name);
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            return null;
        }
    }

    private static boolean isHudHidden(Minecraft mc) {
        try {
            if (OPTIONS_HIDE_GUI != null) {
                return OPTIONS_HIDE_GUI.getBoolean(mc.options);
            }
            if (GUI_HUD != null && HUD_IS_HIDDEN != null) {
                Object hud = GUI_HUD.get(mc.gui);
                Object hidden = HUD_IS_HIDDEN.invoke(hud);
                return hidden instanceof Boolean value && value;
            }
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return false;
        }
        return false;
    }

    private static int offhandMaxCooldown(LocalPlayer player, ItemStack offhand) {
        ItemAttributeModifiers mods = offhand.getAttributeModifiers();
        double speed = mods.compute(Attributes.ATTACK_SPEED,
                player.getAttributeBaseValue(Attributes.ATTACK_SPEED), EquipmentSlot.MAINHAND);
        if (speed <= 0.0D) {
            speed = 4.0D;
        }
        return Math.max(1, (int) Math.round(20.0D / speed));
    }

    private static void onCooldownSync(OffhandCooldownPayload payload, IPayloadContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.setData(ModAttachments.OFFHAND_COOLDOWN.get(), payload.cooldown());
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (offhandHandled && !Minecraft.getInstance().options.keyUse.isDown()) {
            offhandHandled = false;
        }
    }

    @SubscribeEvent
    public static void onInput(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator() || player.isUsingItem()) {
            return;
        }

        if (event.isAttack()) {
            handleAttack(mc, player, event);
        } else if (event.isUseItem() && event.getHand() == InteractionHand.OFF_HAND) {
            handleOffhand(mc, player, event);
        }
    }

    private static void handleAttack(Minecraft mc, LocalPlayer player, InputEvent.InteractionKeyMappingTriggered event) {
        // 不要干扰穿刺武器（矛）：它们有自己的刺击/冷却机制。
        if (player.getMainHandItem().has(DataComponents.PIERCING_WEAPON)) {
            return;
        }
        Entity target = pickEntity(mc, player);
        if (target != null) {
            event.setCanceled(true);
            if (Config.REQUIRE_FULL_ENERGY.get() && player.getAttackStrengthScale(0.5F) < 1.0F) {
                return;
            }
            ClientPacketDistributor.sendToServer(new MainhandAttackPayload(target.getId()));
            // 保持客户端攻击力量指示器与刚执行的命中同步。
            player.resetAttackStrengthTicker();
            return;
        }

        // 扩展范围内没有实体：可选择在完全未命中时抑制冷却重置。
        if (Config.REFUND_ENERGY.get()) {
            HitResult hit = mc.hitResult;
            if (hit == null || hit.getType() == HitResult.Type.MISS) {
                event.setCanceled(true);
            }
        }
    }

    private static void handleOffhand(Minecraft mc, LocalPlayer player, InputEvent.InteractionKeyMappingTriggered event) {
        if (!Config.ENABLE_OFFHAND_ATTACK.get()) {
            return;
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty() || !Config.isOffhandAttackUsable(offhand, player.getMainHandItem())) {
            return;
        }

        event.setCanceled(true);
        event.setSwingHand(false);

        // 上升沿：与主手一样，单次右键按下只攻击一次。按住不放
        // 不会重复攻击，直到松开再按。
        if (offhandHandled) {
            return;
        }
        offhandHandled = true;

        if (player.getData(ModAttachments.OFFHAND_COOLDOWN.get()) > 0) {
            return;
        }

        Entity target = pickEntity(mc, player);
        boolean hit = target != null && Config.isEntityAttackable(target);
        int id = hit ? target.getId() : OffhandAttackPayload.NO_TARGET;
        ClientPacketDistributor.sendToServer(new OffhandAttackPayload(id));
        player.swing(InteractionHand.OFF_HAND);
        if (hit || !Config.REFUND_ENERGY.get()) {
            player.setData(ModAttachments.OFFHAND_COOLDOWN.get(), offhandMaxCooldown(player, offhand));
        }
    }

    private static Entity pickEntity(Minecraft mc, LocalPlayer player) {
        Entity camera = mc.getCameraEntity();
        if (camera == null || mc.level == null) {
            return null;
        }

        double reach = player.entityInteractionRange() + (Config.LONGER_ATTACK.get() ? 1.0D : 0.0D);
        double width = Config.WIDER_ATTACK.get() ? (double) Config.WIDER_ATTACK_WIDTH.get() : 0.5D;

        Vec3 eye = camera.getEyePosition(1.0F);
        Vec3 view = camera.getViewVector(1.0F);
        Vec3 end = eye.add(view.x * reach, view.y * reach, view.z * reach);

        double maxDist = reach;
        BlockHitResult block = mc.level.clip(new ClipContext(eye, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, camera));
        if (block.getType() != HitResult.Type.MISS) {
            maxDist = eye.distanceTo(block.getLocation());
            end = block.getLocation();
        }

        AABB searchBox = camera.getBoundingBox().expandTowards(view.scale(reach)).inflate(1.0D + width);
        Entity best = null;
        double bestDist = maxDist;

        for (Entity entity : mc.level.getEntities(camera, searchBox, e -> e.isPickable() && !e.isSpectator())) {
            double extra = width > 0.5D ? entity.getBbWidth() * (width - 0.5D) : 0.0D;
            AABB box = entity.getBoundingBox().inflate(extra, 0.0D, extra);
            if (box.contains(eye)) {
                return entity;
            }
            Optional<Vec3> clip = box.clip(eye, end);
            if (clip.isPresent()) {
                double d = eye.distanceTo(clip.get());
                if (d <= bestDist) {
                    best = entity;
                    bestDist = d;
                }
            }
        }
        return best;
    }
}
