package com.leeqia.neoBetterCombat.network;

import com.leeqia.neoBetterCombat.NeoBetterCombat;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OffhandCooldownPayload(int cooldown) implements CustomPacketPayload {
    public static final Type<OffhandCooldownPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NeoBetterCombat.MODID, "offhand_cooldown"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OffhandCooldownPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, OffhandCooldownPayload::cooldown, OffhandCooldownPayload::new);

    @Override
    public Type<OffhandCooldownPayload> type() {
        return TYPE;
    }
}
