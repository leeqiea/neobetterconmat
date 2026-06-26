package com.leeqia.neoBetterCombat.network;

import com.leeqia.neoBetterCombat.NeoBetterCombat;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * 客户端 -> 服务器：玩家挥动了副手。{@code entityId} 是目标的 ID，
 * 当挥动未命中任何目标时为 {@code -1}（仅播放动画）。
 */
public record OffhandAttackPayload(int entityId) implements CustomPacketPayload {
    public static final int NO_TARGET = -1;

    public static final Type<OffhandAttackPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NeoBetterCombat.MODID, "offhand_attack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OffhandAttackPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, OffhandAttackPayload::entityId, OffhandAttackPayload::new);

    @Override
    public Type<OffhandAttackPayload> type() {
        return TYPE;
    }
}
