package com.leeqia.neoBetterCombat;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, NeoBetterCombat.MODID);

    /** 副手攻击剩余冷却时间（以刻为单位，瞬态数据，不会在重新登录后保留）。 */
    public static final Supplier<AttachmentType<Integer>> OFFHAND_COOLDOWN = ATTACHMENT_TYPES.register(
            "offhand_cooldown",
            () -> AttachmentType.builder(() -> 0).build());

    private ModAttachments() {}
}
