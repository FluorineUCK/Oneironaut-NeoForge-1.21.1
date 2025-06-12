package net.beholderface.oneironaut.registry;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.items.ItemStaff;
import dev.architectury.core.item.ArchitecturyBucketItem;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.item.*;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

public class OneironautItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Oneironaut.MOD_ID, RegistryKeys.ITEM);
    public static final DeferredRegister<ItemGroup> TABS = DeferredRegister.create(Oneironaut.MOD_ID, RegistryKeys.ITEM_GROUP);

    //I will not scream at my computer over this

    public static void init() {
        ITEMS.register();
        TABS.register();
    }

    //public static final ItemGroup ONEIRONAUT_GROUP = CreativeTabRegistry.create(Text.of("oneironaut:oneironaut"), () -> new ItemStack(OneironautItemRegistry.PSUEDOAMETHYST_SHARD.get()));
    public static final RegistrySupplier<ItemGroup> ONEIRONAUT_GROUP = TABS.register("oneironaut", ()->CreativeTabRegistry.create(
            Text.translatable("itemGroup.oneironaut.oneironaut"), ()->OneironautItemRegistry.PSUEDOAMETHYST_SHARD.get().getDefaultStack()));

    private static final Item.Settings ONEIRONAUT_STACKABLE64 = new Item.Settings().maxCount(64).arch$tab(ONEIRONAUT_GROUP);
    private static final Item.Settings ONEIRONAUT_STACKABLE16 = new Item.Settings().maxCount(16).arch$tab(ONEIRONAUT_GROUP);
    private static final Item.Settings ONEIRONAUT_UNSTACKABLE = new Item.Settings().maxCount(1).arch$tab(ONEIRONAUT_GROUP);


    public static final RegistrySupplier<ItemStolenMediaProvider> PSUEDOAMETHYST_SHARD = ITEMS.register("pseudoamethyst_shard", () -> new
            ItemStolenMediaProvider(ONEIRONAUT_STACKABLE64, (int) (MediaConstants.SHARD_UNIT * 1.5), 1500));
    public static final RegistrySupplier<Item> SHIFTING_PSEUDOAMETHYST = ITEMS.register("shifting_pseudoamethyst", ()-> new ShiftingPseudoamethystItem(ONEIRONAUT_STACKABLE64.rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<ArchitecturyBucketItem> THOUGHT_SLURRY_BUCKET = ITEMS.register("thought_slurry_bucket", () -> new ArchitecturyBucketItem(OneironautMiscRegistry.THOUGHT_SLURRY, ONEIRONAUT_UNSTACKABLE));
    public static final RegistrySupplier<ReverberationRod> REVERBERATION_ROD = ITEMS.register("reverberation_rod", () -> new ReverberationRod(ONEIRONAUT_UNSTACKABLE));
    public static final RegistrySupplier<BottomlessMediaItem> BOTTOMLESS_MEDIA_ITEM = ITEMS.register("endless_phial", () -> new BottomlessMediaItem(ONEIRONAUT_UNSTACKABLE));
    public static final RegistrySupplier<BottomlessCastingItem> BOTTOMLESS_CASTING_ITEM = ITEMS.register("bottomless_trinket", () -> new BottomlessCastingItem(ONEIRONAUT_UNSTACKABLE));
    public static final RegistrySupplier<ItemStaff> ECHO_STAFF = ITEMS.register("echo_staff", () -> new GeneralNoisyStaff(ONEIRONAUT_UNSTACKABLE, SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK, null));
    public static final RegistrySupplier<ItemStaff> BEACON_STAFF = ITEMS.register("beacon_staff", () -> new GeneralNoisyStaff(ONEIRONAUT_UNSTACKABLE, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundEvents.BLOCK_BEACON_DEACTIVATE, null));
    public static final RegistrySupplier<ShovelItem> SPOON_STAFF = ITEMS.register("spoon_staff", () -> new ShovelItem(ToolMaterials.IRON, 1.5F, -3.0F, ONEIRONAUT_UNSTACKABLE.maxDamage(1024)));
    public static final RegistrySupplier<GeneralPigmentItem> PIGMENT_NOOSPHERE = ITEMS.register("pigment_noosphere", () -> new GeneralPigmentItem(ONEIRONAUT_STACKABLE64, GeneralPigmentItem.colors_noosphere));
    public static final RegistrySupplier<GeneralPigmentItem> PIGMENT_FLAME = ITEMS.register("pigment_flame", () -> new GeneralPigmentItem(ONEIRONAUT_STACKABLE64, GeneralPigmentItem.colors_flame));
    public static final RegistrySupplier<GeneralPigmentItem> PIGMENT_ECHO = ITEMS.register("pigment_echo", () -> new GeneralPigmentItem(ONEIRONAUT_STACKABLE64, GeneralPigmentItem.colors_echo));
    public static final RegistrySupplier<GeneralPigmentItem> PIGMENT_FRENZY = ITEMS.register("pigment_frenzyflame", () -> new GeneralPigmentItem(ONEIRONAUT_STACKABLE64, GeneralPigmentItem.colors_frenzy));
    public static final RegistrySupplier<MemoryFragmentItem> MEMORY_FRAGMENT = ITEMS.register("memory_fragment", () -> new MemoryFragmentItem(ONEIRONAUT_UNSTACKABLE.rarity(Rarity.RARE), MemoryFragmentItem.NAMES_TOWER));
    public static final RegistrySupplier<WispCaptureItem> WISP_CAPTURE_ITEM = ITEMS.register("wisp_capture_device", ()-> new WispCaptureItem(ONEIRONAUT_UNSTACKABLE));
    public static final RegistrySupplier<MindScalpelItem> MIND_SCALPEL = ITEMS.register("mind_scalpel", ()->new MindScalpelItem(ONEIRONAUT_UNSTACKABLE.rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> RENDER_THORNS = ITEMS.register("rending_thorns", ()->new RenderThorns(ONEIRONAUT_STACKABLE64.rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<ItemLibraryCard> LIBRARY_CARD = ITEMS.register("library_card", ()->new ItemLibraryCard(ONEIRONAUT_UNSTACKABLE.rarity(Rarity.COMMON)));

    public static final RegistrySupplier<BlockItem> PSUEDOAMETHYST_BLOCK_ITEM = ITEMS.register("pseudoamethyst_block", () -> new BlockItem(OneironautBlockRegistry.PSUEDOAMETHYST_BLOCK.get(), ONEIRONAUT_STACKABLE64.rarity(Rarity.COMMON)));
    public static final RegistrySupplier<BlockItem> SUPER_BUDDING_ITEM = ITEMS.register("super_budding", () -> new BlockItem(OneironautBlockRegistry.SUPER_BUDDING.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> NOOSPHERE_BASALT_ITEM = ITEMS.register("noosphere_basalt", () -> new BlockItem(OneironautBlockRegistry.NOOSPHERE_BASALT.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> WISP_LANTERN_ITEM = ITEMS.register("wisp_lantern", () -> new BlockItem(OneironautBlockRegistry.WISP_LANTERN.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> WISP_LANTERN_TINTED_ITEM = ITEMS.register("wisp_lantern_tinted", () -> new BlockItem(OneironautBlockRegistry.WISP_LANTERN_TINTED.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> SENTINEL_SENSOR_ITEM = ITEMS.register("sentinel_sensor", () -> new BlockItem(OneironautBlockRegistry.SENTINEL_SENSOR.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> SENTINEL_TRAP_ITEM = ITEMS.register("sentinel_trap", () -> new BlockItem(OneironautBlockRegistry.SENTINEL_TRAP.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> RAYCAST_BLOCKER_ITEM = ITEMS.register("raycast_blocker", () -> new BlockItem(OneironautBlockRegistry.RAYCAST_BLOCKER.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> RAYCAST_BLOCKER_GLASS_ITEM = ITEMS.register("raycast_blocker_glass", () -> new BlockItem(OneironautBlockRegistry.RAYCAST_BLOCKER_GLASS.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> HEX_RESISTANT_BLOCK_ITEM = ITEMS.register("hex_resistant_block", () -> new BlockItem(OneironautBlockRegistry.HEX_RESISTANT_BLOCK.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> CIRCLE_ITEM = ITEMS.register("circle", () -> new BlockItem(OneironautBlockRegistry.CIRCLE.get(), new Item.Settings().fireproof().rarity(Rarity.EPIC)));
    public static final RegistrySupplier<BlockItem> MEDIA_ICE_ITEM = ITEMS.register("media_ice", () -> new BlockItem(OneironautBlockRegistry.MEDIA_ICE.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> MEDIA_GEL_ITEM = ITEMS.register("media_gel", () -> new BlockItem(OneironautBlockRegistry.MEDIA_GEL.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> CELL_ITEM = ITEMS.register("cell", () -> new BlockItem(OneironautBlockRegistry.CELL.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> WISP_BATTERY_ITEM = ITEMS.register("wisp_battery", ()-> new BlockItem(OneironautBlockRegistry.WISP_BATTERY.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> WISP_BATTERY_DECORATIVE_ITEM = ITEMS.register("decorative_wisp_battery", ()-> new BlockItem(OneironautBlockRegistry.WISP_BATTERY_DECORATIVE.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> HOVER_ELEVATOR_ITEM = ITEMS.register("hover_elevator", ()->new BlockItem(OneironautBlockRegistry.HOVER_ELEVATOR.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> HOVER_REPEATER_ITEM = ITEMS.register("hover_repeater", ()->new BlockItem(OneironautBlockRegistry.HOVER_REPEATER.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> PSEUDOAMETHYST_BUD_SMALL_ITEM = ITEMS.register("pseudoamethyst_bud_small", ()->new BlockItem(OneironautBlockRegistry.PSEUDOAMETHYST_BUD_SMALL.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> PSEUDOAMETHYST_BUD_MEDIUM_ITEM = ITEMS.register("pseudoamethyst_bud_medium", ()->new BlockItem(OneironautBlockRegistry.PSEUDOAMETHYST_BUD_MEDIUM.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> PSEUDOAMETHYST_BUD_LARGE_ITEM = ITEMS.register("pseudoamethyst_bud_large", ()->new BlockItem(OneironautBlockRegistry.PSEUDOAMETHYST_BUD_LARGE.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> PSEUDOAMETHYST_CLUSTER_ITEM = ITEMS.register("pseudoamethyst_cluster", ()->new BlockItem(OneironautBlockRegistry.PSEUDOAMETHYST_CLUSTER.get(), ONEIRONAUT_STACKABLE64));
    public static final RegistrySupplier<BlockItem> SPACE_BOMB_ITEM = ITEMS.register("spacebomb", ()->new BlockItem(OneironautBlockRegistry.SPACE_BOMB.get(), ONEIRONAUT_UNSTACKABLE));
    public static final RegistrySupplier<BlockItem> SLIPWAY_SUPPRESSOR_ITEM = ITEMS.register("slipwaysuppressor", ()->new BlockItem(OneironautBlockRegistry.SLIPWAY_SUPPRESSOR.get(), ONEIRONAUT_STACKABLE64));

    public static final FoodComponent MONKFRUIT_FOOD = (new FoodComponent.Builder()).hunger(4).saturationModifier(0.6F).snack().alwaysEdible().build();
    public static final FoodComponent MONKFRUIT_FOOD_COOKED = (new FoodComponent.Builder()).hunger(6).saturationModifier(0.8F).alwaysEdible().build();
    public static final FoodComponent MONKFRUIT_FOOD_JAM = (new FoodComponent.Builder()).hunger(6).saturationModifier(1.0F).alwaysEdible().build();
    public static final RegistrySupplier<MonkfruitItem> MONKFRUIT = ITEMS.register("monkfruit", ()->{
        return new MonkfruitItem(OneironautBlockRegistry.RENDER_BUSH.get(), ((ONEIRONAUT_STACKABLE64).food(MONKFRUIT_FOOD)));
    });
    public static final RegistrySupplier<MonkfruitItemCooked> MONKFRUIT_COOKED = ITEMS.register("monkfruit_cooked", ()->{
        return new MonkfruitItemCooked(((ONEIRONAUT_STACKABLE64).food(MONKFRUIT_FOOD_COOKED)));
    });
    public static final RegistrySupplier<MonkfruitItemJam> MONKFRUIT_JAM = ITEMS.register("hexjam", ()->{
        return new MonkfruitItemJam(((ONEIRONAUT_STACKABLE64).food(MONKFRUIT_FOOD_JAM)));
    });
}
