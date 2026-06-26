package com.leeqia.neoBetterCombat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * New Better Combat 的配置文件。镜像了原版 Better Combat Rebirth mod 的选项，
 * 适配到 NeoForge 的 {@link ModConfigSpec} API。副手白名单和两个黑名单
 * 以简单的分类开关加"额外"自定义标签/物品列表的形式暴露出来。
 */
public final class Config {
    private static final String TR = "neobettercombat.configuration.";
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // -- 核心战斗调整 --
    public static final ModConfigSpec.BooleanValue REFUND_ENERGY;
    public static final ModConfigSpec.BooleanValue LONGER_ATTACK;
    public static final ModConfigSpec.BooleanValue WIDER_ATTACK;
    public static final ModConfigSpec.DoubleValue WIDER_ATTACK_WIDTH;
    public static final ModConfigSpec.BooleanValue RANDOM_CRITS;
    public static final ModConfigSpec.DoubleValue CRIT_CHANCE;
    public static final ModConfigSpec.BooleanValue REQUIRE_FULL_ENERGY;
    public static final ModConfigSpec.BooleanValue WEAKER_OFFHAND;
    public static final ModConfigSpec.DoubleValue OFFHAND_EFFICIENCY;
    public static final ModConfigSpec.BooleanValue ATTACK_AND_SPRINT;
    public static final ModConfigSpec.BooleanValue MORE_SWEEP;
    public static final ModConfigSpec.BooleanValue HIT_SOUND;
    public static final ModConfigSpec.BooleanValue CRIT_SOUND;
    public static final ModConfigSpec.BooleanValue ENABLE_OFFHAND_ATTACK;

    // -- 副手白名单（分类开关 + 额外列表） --
    public static final ModConfigSpec.BooleanValue ALLOW_SWORDS;
    public static final ModConfigSpec.BooleanValue ALLOW_AXES;
    public static final ModConfigSpec.BooleanValue ALLOW_PICKAXES;
    public static final ModConfigSpec.BooleanValue ALLOW_SHOVELS;
    public static final ModConfigSpec.BooleanValue ALLOW_HOES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> OFFHAND_EXTRA;

    // -- 主手黑名单（分类开关 + 额外列表） --
    public static final ModConfigSpec.BooleanValue BLOCK_WHILE_BOW;
    public static final ModConfigSpec.BooleanValue BLOCK_WHILE_CONSUMING;
    public static final ModConfigSpec.BooleanValue BLOCK_WHILE_BLOCKING;
    public static final ModConfigSpec.BooleanValue BLOCK_WHILE_TRIDENT;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MAINHAND_EXTRA;

    // -- 实体黑名单（分类开关 + 额外列表） --
    public static final ModConfigSpec.BooleanValue PROTECT_ARMOR_STANDS;
    public static final ModConfigSpec.BooleanValue PROTECT_VILLAGERS;
    public static final ModConfigSpec.BooleanValue PROTECT_OWNED_PETS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITY_EXTRA;

    public static final ModConfigSpec SPEC;

    static {
        REFUND_ENERGY = BUILDER
                .comment("未命中目标的近战攻击不会触发武器冷却。（默认关闭。）")
                .translation(TR + "refundEnergyOnMiss").define("refundEnergyOnMiss", false);
        LONGER_ATTACK = BUILDER
                .comment("近战攻击增加 1 格攻击距离。（默认关闭。）")
                .translation(TR + "longerAttack").define("longerAttack", false);
        WIDER_ATTACK = BUILDER
                .comment("近战攻击命中范围更宽（更容易命中目标）。")
                .translation(TR + "widerAttack").define("widerAttack", true);
        WIDER_ATTACK_WIDTH = BUILDER
                .comment("攻击碰撞箱的宽度。0.5 等于原版宽度（不增宽）；增大该值以加宽。")
                .translation(TR + "widerAttackWidth").defineInRange("widerAttackWidth", 0.5D, 0.0D, 64.0D);
        RANDOM_CRITS = BUILDER
                .comment("近战攻击有一定概率随机暴击（暴击还会击破盾牌）。")
                .translation(TR + "randomCrits").define("randomCrits", true);
        CRIT_CHANCE = BUILDER
                .comment("随机暴击的概率（0.0 - 1.0）。")
                .translation(TR + "randomCritChance").defineInRange("randomCritChance", 0.3D, 0.0D, 1.0D);
        REQUIRE_FULL_ENERGY = BUILDER
                .comment("只有攻击力量条满了才能攻击。（默认关闭。）")
                .translation(TR + "requireFullEnergy").define("requireFullEnergy", false);
        WEAKER_OFFHAND = BUILDER
                .comment("副手攻击造成减少的伤害。")
                .translation(TR + "weakerOffhand").define("weakerOffhand", true);
        OFFHAND_EFFICIENCY = BUILDER
                .comment("启用“弱化副手”时的副手伤害倍率。")
                .translation(TR + "offhandEfficiency").defineInRange("offhandEfficiency", 0.5D, 0.0D, 16384.0D);
        ATTACK_AND_SPRINT = BUILDER
                .comment("冲刺时攻击敌人不再打断冲刺。")
                .translation(TR + "attackAndSprint").define("attackAndSprint", true);
        MORE_SWEEP = BUILDER
                .comment("所有物品都能触发剑的横扫动画并造成横扫伤害。",
                         "关闭时（默认），只有标记为剑的武器才能横扫。")
                .translation(TR + "moreSweep").define("moreSweep", false);
        HIT_SOUND = BUILDER
                .comment("命中目标时播放额外的音效。")
                .translation(TR + "additionalHitSound").define("additionalHitSound", true);
        CRIT_SOUND = BUILDER
                .comment("暴击时播放额外的音效。")
                .translation(TR + "additionalCritSound").define("additionalCritSound", true);
        ENABLE_OFFHAND_ATTACK = BUILDER
                .comment("允许通过右键使用副手武器进行攻击。")
                .translation(TR + "enableOffhandAttack").define("enableOffhandAttack", true);

        BUILDER.comment("哪些物品可以用于副手攻击。").push("offhand");
        ALLOW_SWORDS = BUILDER.translation(TR + "allowSwords").define("allowSwords", true);
        ALLOW_AXES = BUILDER.translation(TR + "allowAxes").define("allowAxes", true);
        ALLOW_PICKAXES = BUILDER.translation(TR + "allowPickaxes").define("allowPickaxes", true);
        ALLOW_SHOVELS = BUILDER.translation(TR + "allowShovels").define("allowShovels", true);
        ALLOW_HOES = BUILDER.translation(TR + "allowHoes").define("allowHoes", true);
        OFFHAND_EXTRA = BUILDER
                .comment("额外副手物品。使用 '#namespace:tag' 表示标签，'namespace:item' 表示单个物品。")
                .translation(TR + "offhandExtra")
                .defineListAllowEmpty("extraWhitelist", List.of(), () -> "namespace:item", Config::validateItemEntry);
        BUILDER.pop();

        BUILDER.comment("主手正在执行以下操作时，禁用副手攻击。").push("mainhandBlacklist");
        BLOCK_WHILE_BOW = BUILDER.translation(TR + "blockWhileBow").define("disableWhileChargingBow", true);
        BLOCK_WHILE_CONSUMING = BUILDER.translation(TR + "blockWhileConsuming").define("disableWhileConsuming", true);
        BLOCK_WHILE_BLOCKING = BUILDER.translation(TR + "blockWhileBlocking").define("disableWhileBlocking", true);
        BLOCK_WHILE_TRIDENT = BUILDER.translation(TR + "blockWhileTrident").define("disableWhileUsingTrident", true);
        MAINHAND_EXTRA = BUILDER
                .comment("额外主手条目。使用 '#tag'、'namespace:item' 或 'anim:<use_animation>'（例如 anim:spyglass）。")
                .translation(TR + "mainhandExtra")
                .defineListAllowEmpty("extraBlacklist", List.of(), () -> "namespace:item", Config::validateMainhandEntry);
        BUILDER.pop();

        BUILDER.comment("副手攻击不能命中的实体。").push("entityBlacklist");
        PROTECT_ARMOR_STANDS = BUILDER.translation(TR + "protectArmorStands").define("protectArmorStands", true);
        PROTECT_VILLAGERS = BUILDER.translation(TR + "protectVillagers").define("protectVillagers", true);
        PROTECT_OWNED_PETS = BUILDER.translation(TR + "protectOwnedPets").define("protectOwnedPets", true);
        ENTITY_EXTRA = BUILDER
                .comment("额外受保护的实体。使用 '#namespace:tag' 或 'namespace:entity'。")
                .translation(TR + "entityExtra")
                .defineListAllowEmpty("extraBlacklist", List.of(), () -> "namespace:entity", Config::validateItemEntry);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private Config() {}

    // ------------------------------------------------------------------
    // 元素验证器（为游戏内列表编辑器提供红/绿反馈）
    // ------------------------------------------------------------------

    /** 接受 {@code #namespace:tag} 或 {@code namespace:id} 格式（用于物品和实体）。 */
    private static boolean validateItemEntry(final Object obj) {
        if (!(obj instanceof String s)) {
            return false;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return false;
        }
        if (t.startsWith("#")) {
            t = t.substring(1);
        }
        return Identifier.tryParse(t) != null;
    }

    /** 类似 {@link #validateItemEntry}，但额外支持 {@code anim:<use_animation>}。 */
    private static boolean validateMainhandEntry(final Object obj) {
        if (obj instanceof String s && s.trim().toLowerCase(Locale.ROOT).startsWith("anim:")) {
            return !s.trim().substring("anim:".length()).isBlank();
        }
        return validateItemEntry(obj);
    }

    // ------------------------------------------------------------------
    // 延迟解析的查找缓存（在配置（重新）加载时清除）
    // ------------------------------------------------------------------
    private static List<TagKey<Item>> offhandTags;
    private static List<Item> offhandItems;
    private static List<TagKey<Item>> mainBlTags;
    private static List<Item> mainBlItems;
    private static List<ItemUseAnimation> mainBlAnims;
    private static List<TagKey<EntityType<?>>> entityBlTags;
    private static List<EntityType<?>> entityBlTypes;

    public static synchronized void invalidate() {
        offhandTags = null;
    }

    private static void resolve() {
        if (offhandTags != null) {
            return;
        }
        offhandTags = new ArrayList<>();
        offhandItems = new ArrayList<>();
        if (ALLOW_SWORDS.get()) offhandTags.add(ItemTags.SWORDS);
        if (ALLOW_AXES.get()) offhandTags.add(ItemTags.AXES);
        if (ALLOW_PICKAXES.get()) offhandTags.add(ItemTags.PICKAXES);
        if (ALLOW_SHOVELS.get()) offhandTags.add(ItemTags.SHOVELS);
        if (ALLOW_HOES.get()) offhandTags.add(ItemTags.HOES);
        for (String s : OFFHAND_EXTRA.get()) {
            parseItem(s, offhandTags, offhandItems);
        }

        mainBlTags = new ArrayList<>();
        mainBlItems = new ArrayList<>();
        mainBlAnims = new ArrayList<>();
        if (BLOCK_WHILE_BOW.get()) {
            mainBlAnims.add(ItemUseAnimation.BOW);
            mainBlAnims.add(ItemUseAnimation.CROSSBOW);
        }
        if (BLOCK_WHILE_CONSUMING.get()) {
            mainBlAnims.add(ItemUseAnimation.EAT);
            mainBlAnims.add(ItemUseAnimation.DRINK);
        }
        if (BLOCK_WHILE_BLOCKING.get()) {
            mainBlAnims.add(ItemUseAnimation.BLOCK);
        }
        if (BLOCK_WHILE_TRIDENT.get()) {
            mainBlAnims.add(ItemUseAnimation.SPEAR);
            mainBlAnims.add(ItemUseAnimation.TRIDENT);
        }
        for (String s : MAINHAND_EXTRA.get()) {
            String low = s.toLowerCase(Locale.ROOT).trim();
            if (low.startsWith("anim:")) {
                try {
                    mainBlAnims.add(ItemUseAnimation.valueOf(low.substring("anim:".length()).trim().toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException ex) {
                    NeoBetterCombat.LOGGER.warn("Unknown use animation in mainhand blacklist: {}", s);
                }
            } else {
                parseItem(s, mainBlTags, mainBlItems);
            }
        }

        entityBlTags = new ArrayList<>();
        entityBlTypes = new ArrayList<>();
        if (PROTECT_ARMOR_STANDS.get()) addEntityType(entityBlTypes, "armor_stand");
        if (PROTECT_VILLAGERS.get()) addEntityType(entityBlTypes, "villager");
        for (String s : ENTITY_EXTRA.get()) {
            String t = s.trim();
            if (t.startsWith("#")) {
                entityBlTags.add(TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(t.substring(1))));
            } else {
                BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(t)).ifPresent(entityBlTypes::add);
            }
        }
    }

    private static void addEntityType(List<EntityType<?>> types, String id) {
        BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.withDefaultNamespace(id)).ifPresent(types::add);
    }

    private static void parseItem(String s, List<TagKey<Item>> tags, List<Item> items) {
        String t = s.trim();
        if (t.isEmpty()) {
            return;
        }
        if (t.startsWith("#")) {
            tags.add(TagKey.create(Registries.ITEM, Identifier.parse(t.substring(1))));
        } else {
            BuiltInRegistries.ITEM.getOptional(Identifier.parse(t)).ifPresent(items::add);
        }
    }

    private static boolean matches(ItemStack stack, List<TagKey<Item>> tags, List<Item> items) {
        if (stack.isEmpty()) {
            return false;
        }
        for (Item item : items) {
            if (stack.getItem() == item) {
                return true;
            }
        }
        for (TagKey<Item> tag : tags) {
            if (stack.typeHolder().is(tag)) {
                return true;
            }
        }
        return false;
    }

    /** 给定当前主手物品的情况下，该副手物品是否可用于副手攻击。 */
    public static boolean isOffhandAttackUsable(ItemStack offhand, ItemStack mainhand) {
        resolve();
        if (!matches(offhand, offhandTags, offhandItems)) {
            return false;
        }
        if (mainBlAnims.contains(mainhand.getUseAnimation())) {
            return false;
        }
        return !matches(mainhand, mainBlTags, mainBlItems);
    }

    /** 该实体是否允许被副手攻击锁定为目标。 */
    public static boolean isEntityAttackable(Entity entity) {
        resolve();
        if (entityBlTypes.contains(entity.getType())) {
            return false;
        }
        for (TagKey<EntityType<?>> tag : entityBlTags) {
            if (entity.getType().builtInRegistryHolder().is(tag)) {
                return false;
            }
        }
        return true;
    }
}
