package net.beholderface.oneironaut.registry;

import net.minecraft.world.food.FoodProperties;

import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.items.ItemStaff;
import dev.architectury.core.item.ArchitecturyBucketItem;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.beholderface.oneironaut.MiscClientAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Rarity;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class OneironautItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Oneironaut.MOD_ID, Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Oneironaut.MOD_ID, Registries.CREATIVE_MODE_TAB);

    //I will not scream at my computer over this

    public static void init() {
        ITEMS.register();
        TABS.register();
    }

    //public static final ItemGroup ONEIRONAUT_GROUP = CreativeTabRegistry.create(Text.of("oneironaut:oneironaut"), () -> new ItemStack(OneironautItemRegistry.PSUEDOAMETHYST_SHARD.get()));
    public static final RegistrySupplier<CreativeModeTab> ONEIRONAUT_GROUP = TABS.register("oneironaut", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.oneironaut.oneironaut"))
                    .icon(() -> OneironautItemRegistry.PSUEDOAMETHYST_SHARD.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        for (RegistrySupplier<Item> supplier : OneironautItemRegistry.ITEMS) {
                            Item item = supplier.get();
                            if (isVisibleInCreativeTab(item)) {
                                output.accept(item);
                            }
                        }
                    })
                    .build());

    private static Item.Properties stackable64() {
        return new Item.Properties().stacksTo(64);
    }

    private static Item.Properties stackable16() {
        return new Item.Properties().stacksTo(16);
    }

    private static Item.Properties unstackable() {
        return new Item.Properties().stacksTo(1);
    }

    private static Item.Properties unstackable1024() {
        return new Item.Properties().durability(1024);
    }


    public static final RegistrySupplier<ItemStolenMediaProvider> PSUEDOAMETHYST_SHARD = ITEMS.register("pseudoamethyst_shard", () -> new
            ItemStolenMediaProvider(stackable64(), (int) (MediaConstants.SHARD_UNIT * 1.5), 1500));
    public static final RegistrySupplier<ShiftingPseudoamethystItem> SHIFTING_PSEUDOAMETHYST = ITEMS.register("shifting_pseudoamethyst", ()-> new ShiftingPseudoamethystItem(stackable64().rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<ArchitecturyBucketItem> THOUGHT_SLURRY_BUCKET = ITEMS.register("thought_slurry_bucket", () -> new ArchitecturyBucketItem(OneironautMiscRegistry.THOUGHT_SLURRY, unstackable()));
    public static final RegistrySupplier<ReverberationRod> REVERBERATION_ROD = ITEMS.register("reverberation_rod", () -> new ReverberationRod(unstackable()));
    public static final RegistrySupplier<BottomlessMediaItem> BOTTOMLESS_MEDIA_ITEM = ITEMS.register("endless_phial", () -> new BottomlessMediaItem(unstackable()));
    public static final RegistrySupplier<BottomlessCastingItem> BOTTOMLESS_CASTING_ITEM = ITEMS.register("bottomless_trinket", () -> new BottomlessCastingItem(unstackable()));
    public static final RegistrySupplier<ItemStaff> ECHO_STAFF = ITEMS.register("echo_staff", () -> new GeneralNoisyStaff(unstackable(), SoundEvents.SCULK_CLICKING, SoundEvents.SCULK_SHRIEKER_SHRIEK, null));
    public static final RegistrySupplier<ItemStaff> BEACON_STAFF = ITEMS.register("beacon_staff", () -> new GeneralNoisyStaff(unstackable(), SoundEvents.BEACON_ACTIVATE, SoundEvents.BEACON_DEACTIVATE, null));
    public static final RegistrySupplier<ShovelItem> SPOON_STAFF = ITEMS.register("spoon_staff", () ->
            new ShovelItem(Tiers.IRON, unstackable1024().attributes(
                    DiggerItem.createAttributes(Tiers.IRON, 1.5F, -3.0F))));
    public static final RegistrySupplier<GeneralPigmentItem> PIGMENT_NOOSPHERE = ITEMS.register("pigment_noosphere", () -> new GeneralPigmentItem(stackable64(), GeneralPigmentItem.colors_noosphere));
    public static final RegistrySupplier<GeneralPigmentItem> PIGMENT_FLAME = ITEMS.register("pigment_flame", () -> new GeneralPigmentItem(stackable64(), GeneralPigmentItem.colors_flame));
    public static final RegistrySupplier<GeneralPigmentItem> PIGMENT_ECHO = ITEMS.register("pigment_echo", () -> new GeneralPigmentItem(stackable64(), GeneralPigmentItem.colors_echo));
    public static final RegistrySupplier<GeneralPigmentItem> PIGMENT_FRENZY = ITEMS.register("pigment_frenzyflame", () -> new GeneralPigmentItem(stackable64(), GeneralPigmentItem.colors_frenzy));
    public static final RegistrySupplier<Item> PIGMENT_CLOCK = ITEMS.register("pigment_clock", ()->new ArbitaryDeltaPigmentItem(stackable64(), ArbitaryDeltaPigmentItem.skyColors,
            ()-> {if (!Oneironaut.isServerThread()) {return ((MiscClientAPIKt.getClientDayTime() + 3000) % 24000) / ArbitaryDeltaPigmentItem.twentyMinutesInTicks;}
            else {return 12000.0;}}));
    public static final RegistrySupplier<MemoryFragmentItem> MEMORY_FRAGMENT = ITEMS.register("memory_fragment", () -> new MemoryFragmentItem(unstackable().rarity(Rarity.RARE), MemoryFragmentItem.NAMES_TOWER));
    public static final RegistrySupplier<WispCaptureItem> WISP_CAPTURE_ITEM = ITEMS.register("wisp_capture_device", ()-> new WispCaptureItem(unstackable()));
    public static final RegistrySupplier<MindScalpelItem> MIND_SCALPEL = ITEMS.register("mind_scalpel", ()->new MindScalpelItem(unstackable().rarity(Rarity.RARE)));
    public static final RegistrySupplier<RenderThorns> RENDER_THORNS = ITEMS.register("rending_thorns", ()->new RenderThorns(stackable64().rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<ItemLibraryCard> LIBRARY_CARD = ITEMS.register("library_card", ()->new ItemLibraryCard(unstackable()));
    public static final RegistrySupplier<Item> RIFT_RESIDUE = ITEMS.register("rift_residue", ()->new RiftResidueItem(stackable64(), ArbitaryDeltaPigmentItem.skyColors,
            ()-> ((System.currentTimeMillis() + TimeZone.getDefault().getRawOffset()) % ArbitaryDeltaPigmentItem.irlDayInMilliseconds) / ArbitaryDeltaPigmentItem.irlDayInMilliseconds));


    public static final RegistrySupplier<BlockItem> PSUEDOAMETHYST_BLOCK_ITEM = ITEMS.register("pseudoamethyst_block", () -> new BlockItem(OneironautBlockRegistry.PSUEDOAMETHYST_BLOCK.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> PSUEDOAMETHYST_BLOCK_INSUBSTANTIAL_ITEM = ITEMS.register("insubstantial_pseudoamethyst_block", () -> new BlockItem(OneironautBlockRegistry.PSUEDOAMETHYST_BLOCK_INSUBSTANTIAL.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> SUPER_BUDDING_ITEM = ITEMS.register("super_budding", () -> new BlockItem(OneironautBlockRegistry.SUPER_BUDDING.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> NOOSPHERE_BASALT_ITEM = ITEMS.register("noosphere_basalt", () -> new BlockItem(OneironautBlockRegistry.NOOSPHERE_BASALT.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> WISP_LANTERN_ITEM = ITEMS.register("wisp_lantern", () -> new BlockItem(OneironautBlockRegistry.WISP_LANTERN.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> WISP_LANTERN_TINTED_ITEM = ITEMS.register("wisp_lantern_tinted", () -> new BlockItem(OneironautBlockRegistry.WISP_LANTERN_TINTED.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> SENTINEL_SENSOR_ITEM = ITEMS.register("sentinel_sensor", () -> new BlockItem(OneironautBlockRegistry.SENTINEL_SENSOR.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> SENTINEL_TRAP_ITEM = ITEMS.register("sentinel_trap", () -> new BlockItem(OneironautBlockRegistry.SENTINEL_TRAP.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> RAYCAST_BLOCKER_ITEM = ITEMS.register("raycast_blocker", () -> new BlockItem(OneironautBlockRegistry.RAYCAST_BLOCKER.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> RAYCAST_BLOCKER_GLASS_ITEM = ITEMS.register("raycast_blocker_glass", () -> new BlockItem(OneironautBlockRegistry.RAYCAST_BLOCKER_GLASS.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> HEX_RESISTANT_BLOCK_ITEM = ITEMS.register("hex_resistant_block", () -> new BlockItem(OneironautBlockRegistry.HEX_RESISTANT_BLOCK.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> CIRCLE_ITEM = ITEMS.register("circle", () -> new BlockItem(OneironautBlockRegistry.CIRCLE.get(), new Item.Properties().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistrySupplier<BlockItem> MEDIA_ICE_ITEM = ITEMS.register("media_ice", () -> new BlockItem(OneironautBlockRegistry.MEDIA_ICE.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> MEDIA_GEL_ITEM = ITEMS.register("media_gel", () -> new BlockItem(OneironautBlockRegistry.MEDIA_GEL.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> CELL_ITEM = ITEMS.register("cell", () -> new BlockItem(OneironautBlockRegistry.CELL.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> WISP_BATTERY_ITEM = ITEMS.register("wisp_battery", ()-> new BlockItem(OneironautBlockRegistry.WISP_BATTERY.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> WISP_BATTERY_DECORATIVE_ITEM = ITEMS.register("decorative_wisp_battery", ()-> new BlockItem(OneironautBlockRegistry.WISP_BATTERY_DECORATIVE.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> HOVER_ELEVATOR_ITEM = ITEMS.register("hover_elevator", ()->new BlockItem(OneironautBlockRegistry.HOVER_ELEVATOR.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> HOVER_REPEATER_ITEM = ITEMS.register("hover_repeater", ()->new BlockItem(OneironautBlockRegistry.HOVER_REPEATER.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> PSEUDOAMETHYST_BUD_SMALL_ITEM = ITEMS.register("pseudoamethyst_bud_small", ()->new BlockItem(OneironautBlockRegistry.PSEUDOAMETHYST_BUD_SMALL.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> PSEUDOAMETHYST_BUD_MEDIUM_ITEM = ITEMS.register("pseudoamethyst_bud_medium", ()->new BlockItem(OneironautBlockRegistry.PSEUDOAMETHYST_BUD_MEDIUM.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> PSEUDOAMETHYST_BUD_LARGE_ITEM = ITEMS.register("pseudoamethyst_bud_large", ()->new BlockItem(OneironautBlockRegistry.PSEUDOAMETHYST_BUD_LARGE.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> PSEUDOAMETHYST_CLUSTER_ITEM = ITEMS.register("pseudoamethyst_cluster", ()->new BlockItem(OneironautBlockRegistry.PSEUDOAMETHYST_CLUSTER.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> SPACE_BOMB_ITEM = ITEMS.register("spacebomb", ()->new BlockItem(OneironautBlockRegistry.SPACE_BOMB.get(), unstackable()));
    public static final RegistrySupplier<BlockItem> SLIPWAY_SUPPRESSOR_ITEM = ITEMS.register("slipwaysuppressor", ()->new BlockItem(OneironautBlockRegistry.SLIPWAY_SUPPRESSOR.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> EXTRADIM_LOCUS_ITEM = ITEMS.register("extradimensional_border", ()->new BlockItem(OneironautBlockRegistry.EXTRADIM_LOCUS.get(), stackable64()));

    public static final RegistrySupplier<BlockItem> CONCEPT_MODIFIER_EMPTY = ITEMS.register("concept_modifier_empty", ()->new BlockItem(OneironautBlockRegistry.CONCEPT_MODIFIER_EMPTY.get(), stackable64()));
    public static final RegistrySupplier<BlockItem> CONCEPT_MODIFIER_SUS = ITEMS.register("concept_modifier_sus", ()->new BlockItem(OneironautBlockRegistry.CONCEPT_MODIFIER_SUS.get(), stackable64()));

    //these only exist to look good in patchouli and satisfy hexdoc
    public static final RegistrySupplier<BlockItem> INACTIVE_SLIPWAY_ITEM = ITEMS.register("inactiveslipway", ()->new BlockItem(OneironautBlockRegistry.INACTIVE_SLIPWAY.get(), new Item.Properties()));
    public static final RegistrySupplier<BlockItem> RIFT_RESIDUE_DROPPER_ITEM = ITEMS.register("rift_residue_dropper", ()->new BlockItem(OneironautBlockRegistry.INSTANT_BREAKER_RIFTRESIDUE.get(), new Item.Properties()));

    public static final Map<DyeColor, RegistrySupplier<BlockItem>> COLORFUL_CONCEPT_MODIFIERS = new HashMap<>();
    static {
        for (DyeColor color : DyeColor.values()){
            RegistrySupplier<BlockItem> supplier = ITEMS.register("concept_decorator_color/" + color.getName(), ()->new BlockItem(OneironautBlockRegistry.COLORFUL_CONCEPT_MODIFIERS.get(color).get(), stackable64()));
            COLORFUL_CONCEPT_MODIFIERS.put(color, supplier);
        }
    }

    public static final RegistrySupplier<WriteableBlockItem> CONCEPT_MODIFIER_GRIDSIZE = ITEMS.register("concept_modifier_gridsize", ()-> new WriteableBlockItem(OneironautBlockRegistry.CONCEPT_MODIFIER_GRIDSIZE.get(),
            unstackable(), (iota)-> iota instanceof DoubleIota && Math.abs(((DoubleIota) iota).getDouble()) <= 2.0));
    public static final RegistrySupplier<WriteableBlockItem> CONCEPT_MODIFIER_MAXHEALTH = ITEMS.register("concept_modifier_maxhealth", ()-> new WriteableBlockItem(OneironautBlockRegistry.CONCEPT_MODIFIER_MAXHEALTH.get(),
            unstackable(), (iota)-> iota instanceof DoubleIota && Math.abs(((DoubleIota) iota).getDouble()) <= 10.0));
    public static final RegistrySupplier<WriteableBlockItem> CONCEPT_MODIFIER_GTP_DROP = ITEMS.register("concept_modifier_gtp_drop", ()-> new WriteableBlockItem(OneironautBlockRegistry.CONCEPT_MODIFIER_GTP_DROP.get(),
            unstackable(), (iota)-> iota instanceof DoubleIota));
    public static final RegistrySupplier<WriteableBlockItem> CONCEPT_MODIFIER_ANTIEROSION = ITEMS.register("concept_modifier_antierosion", ()-> new WriteableBlockItem(OneironautBlockRegistry.CONCEPT_MODIFIER_ANTIEROSION.get(),
            unstackable(), (iota)-> false));
    public static final RegistrySupplier<WriteableBlockItem> CONCEPT_MODIFIER_REFERENCE_FALSY = ITEMS.register("concept_modifier_falsy", ()-> new WriteableBlockItem(OneironautBlockRegistry.CONCEPT_MODIFIER_REFERENCE_FALSY.get(),
            unstackable(), (iota)-> false));
    public static final RegistrySupplier<WriteableBlockItem> CONCEPT_MODIFIER_REFERENCE_COMPARISON = ITEMS.register("concept_modifier_comparison", ()-> new WriteableBlockItem(OneironautBlockRegistry.CONCEPT_MODIFIER_REFERENCE_COMPARISON.get(),
            unstackable(), (iota)-> iota instanceof BooleanIota));
    public static final RegistrySupplier<WriteableBlockItem> CONCEPT_MODIFIER_STACK_SIZE = ITEMS.register("concept_modifier_stack_size", ()-> new WriteableBlockItem(OneironautBlockRegistry.CONCEPT_MODIFIER_STACK_SIZE.get(),
            unstackable(), (iota)-> false));
    public static final RegistrySupplier<WriteableBlockItem> CONCEPT_CORE = ITEMS.register("concept_core", ()->new WriteableBlockItem(OneironautBlockRegistry.CONCEPT_CORE.get(), unstackable(),
            (iota)-> iota instanceof EntityIota));

    public static final RegistrySupplier<BlockItem> CONCEPT_CONNECTOR = ITEMS.register("concept_connector", ()->new BlockItem(OneironautBlockRegistry.CONCEPT_CONNECTOR.get(), stackable64()));

    public static final FoodProperties MONKFRUIT_FOOD = (new FoodProperties.Builder()).nutrition(4).saturationModifier(0.6F).fast().alwaysEdible().build();
    public static final FoodProperties MONKFRUIT_FOOD_COOKED = (new FoodProperties.Builder()).nutrition(6).saturationModifier(0.8F).alwaysEdible().build();
    public static final FoodProperties MONKFRUIT_FOOD_JAM = (new FoodProperties.Builder()).nutrition(6).saturationModifier(1.0F).alwaysEdible().build();
    public static final RegistrySupplier<MonkfruitItem> MONKFRUIT = ITEMS.register("monkfruit", ()->{
        return new MonkfruitItem(OneironautBlockRegistry.RENDER_BUSH.get(), ((stackable64()).food(MONKFRUIT_FOOD)));
    });
    public static final RegistrySupplier<MonkfruitItemCooked> MONKFRUIT_COOKED = ITEMS.register("monkfruit_cooked", ()->{
        return new MonkfruitItemCooked(((stackable64()).food(MONKFRUIT_FOOD_COOKED)));
    });
    public static final RegistrySupplier<MonkfruitItemJam> MONKFRUIT_JAM = ITEMS.register("hexjam", ()->{
        return new MonkfruitItemJam(((stackable64()).food(MONKFRUIT_FOOD_JAM)));
    });

    private static boolean isVisibleInCreativeTab(Item item) {
        return item != CONCEPT_MODIFIER_SUS.get()
                && item != CIRCLE_ITEM.get()
                && item != INACTIVE_SLIPWAY_ITEM.get()
                && item != RIFT_RESIDUE_DROPPER_ITEM.get();
    }
}
