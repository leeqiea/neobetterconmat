package com.leeqia.neoBetterCombat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 模组添加的额外命中/暴击音效。 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, NeoBetterCombat.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SWORD_SLASH = register("player.swordslash");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRITICAL_STRIKE = register("player.criticalstrike");

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(NeoBetterCombat.MODID, name)));
    }
}
