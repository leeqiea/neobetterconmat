package com.leeqia.neoBetterCombat.network;

import com.leeqia.neoBetterCombat.NeoBetterCombat;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 客户端 -> 服务器：玩家用主手攻击了 {@code entityId}（自定义攻击范围）。 */
public record MainhandAttackPayload(int entityId) implements CustomPacketPayload {
    public static final Type<MainhandAttackPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NeoBetterCombat.MODID, "mainhand_attack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MainhandAttackPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, MainhandAttackPayload::entityId, MainhandAttackPayload::new);

    @Override
    public Type<MainhandAttackPayload> type() {
        return TYPE;
    }
}
