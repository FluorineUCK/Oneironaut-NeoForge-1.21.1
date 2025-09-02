package net.beholderface.oneironaut;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.common.items.ItemStaff;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.beholderface.oneironaut.block.ConceptDecoratorBlock;
import net.beholderface.oneironaut.block.ConceptModifierBlock;
import net.beholderface.oneironaut.block.ThoughtSlurry;
import net.beholderface.oneironaut.block.blockentity.ConceptCoreBlockEntity;
import net.beholderface.oneironaut.block.blockentity.ConceptModifierBlockEntity;
import net.beholderface.oneironaut.block.blockentity.HoverElevatorBlockEntity;
import net.beholderface.oneironaut.block.blockentity.WispBatteryEntity;
import net.beholderface.oneironaut.item.ItemLibraryCard;
import net.beholderface.oneironaut.item.ReverberationRod;
import net.beholderface.oneironaut.item.WispCaptureItem;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.beholderface.oneironaut.registry.OneironautItemRegistry;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.mixin.client.rendering.DimensionEffectsAccessor;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * Common client loading entrypoint.
 */
public class OneironautClient {

    private static int applyBlockRenderLayers(Collection<Block> blocks, RenderLayer layer){
        int applied = 0;
        for (Block block : blocks){
            BlockRenderLayerMap.INSTANCE.putBlock(block, layer);
            applied++;
        }
        return applied;
    }

    public static long lastShiftingHoverTick = 0L;
    public static ItemStack lastHoveredShifting = null;
    private static float processObservationPredicate(ItemStack stack, ClientWorld world, LivingEntity holder, int holderID){
        ClientPlayerEntity cachedPlayer = cachedClient.player;
        final float OFF = 0.99f;
        final float ON = -0.01f;
        float output = ON;
        int fov = cachedClient.options.getFov().getValue();
        double threshold = fov / (fov <= 85 ? 90.0 : 100.0);
        if (cachedPlayer != null){
            if (stack.isInFrame()){
                assert stack.getFrame() != null;
                if (MiscAPIKt.vecProximity(stack.getFrame().getPos().subtract(cachedPlayer.getEyePos()), cachedPlayer.getRotationVector()) <= threshold) {
                    output = OFF;
                }
            }
            if (stack.getHolder() != null && stack.getHolder() != cachedPlayer){
                Vec3d holderCenterApprox = stack.getHolder().getPos().add(stack.getHolder().getEyePos()).multiply(0.5);
                if (MiscAPIKt.vecProximity(holderCenterApprox.subtract(cachedPlayer.getEyePos()), cachedPlayer.getRotationVector()) <= threshold) {
                    output = OFF;
                }
            }
            if (holder == cachedPlayer && (holder.getStackInHand(Hand.MAIN_HAND) == stack || holder.getStackInHand(Hand.OFF_HAND) == stack)){
                output = OFF;
            }
            if (cachedPlayer.currentScreenHandler.getCursorStack() == stack ||
                    (lastShiftingHoverTick + 1 >= cachedPlayer.getWorld().getTime() && lastHoveredShifting == stack)){
                output = OFF;
            }
        }
        if (!cachedClient.isWindowFocused()){
            output = ON;
        }
        return output;
    }

    //private static ClientPlayerEntity cachedPlayer = null;
    private static MinecraftClient cachedClient = null;
    public static MinecraftClient getCachedClient(){
        return cachedClient;
    }
    public static void init() {

        //if (Platform.isFabric()){
            /*ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
                registry.register(new Identifier("oneironaut:block/thought_slurry"));
                registry.register(new Identifier("oneironaut:block/thought_slurry_flowing"));
            });*/

            FluidRenderHandlerRegistry.INSTANCE.register(ThoughtSlurry.STILL_FLUID, ThoughtSlurry.FLOWING_FLUID, new SimpleFluidRenderHandler(
                    new Identifier("oneironaut:block/thought_slurry"),
                    new Identifier("oneironaut:block/thought_slurry_flowing"),
                    0x8621c2
            ));
            ScryingLensOverlayRegistry.addDisplayer(OneironautBlockRegistry.WISP_BATTERY.get(),
                    WispBatteryEntity::applyScryingLensOverlay
                    );

            List<RegistrySupplier<ConceptModifierBlock>> conceptModifiers = List.of(OneironautBlockRegistry.CONCEPT_MODIFIER_GRIDSIZE,
                    OneironautBlockRegistry.CONCEPT_MODIFIER_ANTIEROSION, OneironautBlockRegistry.CONCEPT_MODIFIER_MAXHEALTH, OneironautBlockRegistry.CONCEPT_MODIFIER_GTP_DROP,
                    OneironautBlockRegistry.CONCEPT_MODIFIER_REFERENCE_COMPARISON, OneironautBlockRegistry.CONCEPT_MODIFIER_REFERENCE_FALSY
            );
            for (RegistrySupplier<ConceptModifierBlock> supplier : conceptModifiers){
                ScryingLensOverlayRegistry.addDisplayer(supplier.get(), ConceptModifierBlockEntity::applyScryingLensOverlay);
            }
            ScryingLensOverlayRegistry.addDisplayer(OneironautBlockRegistry.CONCEPT_CORE.get(), ConceptCoreBlockEntity::applyScryingLensOverlay);

            List<Block> cutoutBlocks = new ArrayList<>(List.of(OneironautBlockRegistry.WISP_LANTERN.get(), OneironautBlockRegistry.WISP_LANTERN_TINTED.get(),
                    OneironautBlockRegistry.WISP_BATTERY.get(), OneironautBlockRegistry.WISP_BATTERY_DECORATIVE.get(),
                    OneironautBlockRegistry.CIRCLE.get(), OneironautBlockRegistry.PSEUDOAMETHYST_CLUSTER.get(), OneironautBlockRegistry.PSEUDOAMETHYST_BUD_LARGE.get(),
                    OneironautBlockRegistry.PSEUDOAMETHYST_BUD_MEDIUM.get(), OneironautBlockRegistry.PSEUDOAMETHYST_BUD_SMALL.get(),
                    OneironautBlockRegistry.RENDER_BUSH.get(), OneironautBlockRegistry.DEEP_NOOSPHERE_FLOOR.get(),
                    OneironautBlockRegistry.CONCEPT_MODIFIER_REFERENCE_FALSY.get(), OneironautBlockRegistry.CONCEPT_MODIFIER_GRIDSIZE.get(), OneironautBlockRegistry.CONCEPT_MODIFIER_EMPTY.get(),
                    OneironautBlockRegistry.CONCEPT_MODIFIER_SUS.get(), OneironautBlockRegistry.CONCEPT_MODIFIER_ANTIEROSION.get(), OneironautBlockRegistry.CONCEPT_MODIFIER_MAXHEALTH.get(),
                    OneironautBlockRegistry.CONCEPT_MODIFIER_GTP_DROP.get(), OneironautBlockRegistry.CONCEPT_MODIFIER_REFERENCE_COMPARISON.get()));
            for (RegistrySupplier<ConceptDecoratorBlock> supplier : OneironautBlockRegistry.COLORFUL_CONCEPT_MODIFIERS.values()){
                cutoutBlocks.add(supplier.get());
            }
            Block[] translucentBlocks = {OneironautBlockRegistry.RAYCAST_BLOCKER_GLASS.get(), OneironautBlockRegistry.MEDIA_GEL.get(),
                    OneironautBlockRegistry.CELL.get()};

            BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), ThoughtSlurry.STILL_FLUID, ThoughtSlurry.FLOWING_FLUID);

            Oneironaut.LOGGER.info("Applied cutout layer to " + applyBlockRenderLayers(cutoutBlocks, RenderLayer.getCutout()) + " blocks");
            Oneironaut.LOGGER.info("Applied translucent layer to " + applyBlockRenderLayers(List.of(translucentBlocks), RenderLayer.getTranslucent()) + " blocks");

            Oneironaut.LOGGER.info("Registering client-side hoverlift processor.");

            ClientTickEvent.CLIENT_POST.register((client)->{
                try {
                    HoverElevatorBlockEntity.processHover(false, client.world != null ? client.world.getTime() : -1L);
                } catch (ConcurrentModificationException exception){
                    Oneironaut.LOGGER.error("Oopsie client-side hoverlift exception " + exception.getMessage());
                }
                /*if (client.world != null && client.world.getDimensionEffects().getClass() == DeepNoosphereDimensionEffects.class && client.world.getTime() % 20 == 0){
                    for (PlayerEntity player : client.world.getPlayers()){
                        Oneironaut.processDisintegration(player);
                    }
                }*/
            });

            ClientLifecycleEvent.CLIENT_STARTED.register((client)->{
                //cachedPlayer = client.player;
                cachedClient = client;
                if (cachedClient != null){
                    Oneironaut.LOGGER.info("Cached client object. Player:" + client.player);
                } else {
                    Oneironaut.LOGGER.info("Could not cache client object.");
                }
            });
            DimensionEffectsAccessor.getIdentifierMap().put(Oneironaut.id("noosphere"), new NoosphereDimensionEffects());
            DimensionEffectsAccessor.getIdentifierMap().put(Oneironaut.id("deep_noosphere"), new DeepNoosphereDimensionEffects());
        /*} else {
            Oneironaut.LOGGER.info("oh no, forge, aaaaaaaaaaaa");
        }*/

        ItemPackagedHex[] castingItems = {OneironautItemRegistry.REVERBERATION_ROD.get(), OneironautItemRegistry.BOTTOMLESS_CASTING_ITEM.get()/*, OneironautItemRegistry.INSULATED_TRINKET.get()*/};
        for (ItemPackagedHex item : castingItems){
            ItemPropertiesRegistry.register(item, ItemPackagedHex.HAS_PATTERNS_PRED, (stack, world, holder, holderID) -> {
                return item.hasHex(stack) ? 0.99f : -0.01f;
            });
        }

        ItemPropertiesRegistry.register(OneironautItemRegistry.REVERBERATION_ROD.get(), ReverberationRod.CASTING_PREDICATE, (stack, world, holder, holderID) -> {
            //return 0.99f;
            if (holder != null){
                //return 0.99f;
                return holder.getActiveItem().equals(stack) ? 0.99f : -0.01f;
            } else {
                return -0.01f;
            }
            //return OneironautItemRegistry.REVERBERATION_ROD.get().hasHex(stack) ? 0.99f : -0.01f;
        });
        ItemPropertiesRegistry.register(OneironautItemRegistry.WISP_CAPTURE_ITEM.get(), WispCaptureItem.FILLED_PREDICATE, (stack, world, holder, holderID) -> {
            return ((WispCaptureItem)stack.getItem()).hasWisp(stack, world) ? 0.99f : -0.01f;
        });

        ItemPropertiesRegistry.register(OneironautItemRegistry.SHIFTING_PSEUDOAMETHYST.get(), new Identifier(Oneironaut.MOD_ID, "observation"),
                OneironautClient::processObservationPredicate);
        ItemPropertiesRegistry.register(OneironautItemRegistry.LIBRARY_CARD.get(), new Identifier(Oneironaut.MOD_ID, "written"), (stack, world, holder, holderID) -> {
            return ((ItemLibraryCard)stack.getItem()).getDimension(stack) != null ? 0.99f : -0.01f;
        });

        //ah yes, because I definitely want to turn my expensive staff into a much less expensive variant
        Item[] nameSensitiveStaves = {OneironautItemRegistry.ECHO_STAFF.get(), OneironautItemRegistry.BEACON_STAFF.get(), OneironautItemRegistry.SPOON_STAFF.get()};
        for (Item staff: nameSensitiveStaves) {
            ItemPropertiesRegistry.register(staff, ItemStaff.FUNNY_LEVEL_PREDICATE, (stack, level, holder, holderID) -> {
                if (!stack.hasCustomName()) {
                    return 0;
                }
                var name = stack.getName().getString().toLowerCase(Locale.ROOT);
                if (name.contains("old")) {
                    return 1f;
                } else if (name.contains("wand of the forest")) {
                    return 2f;
                } else {
                    return 0f;
                }
            });
        }
    }
}
