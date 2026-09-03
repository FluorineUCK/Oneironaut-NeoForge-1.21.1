package net.beholderface.oneironaut;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.common.items.ItemStaff;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.beholderface.oneironaut.block.ConceptDecoratorBlock;
import net.beholderface.oneironaut.block.ConceptModifierBlock;
import net.beholderface.oneironaut.block.InactiveSlipwayBlock;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;

import java.util.*;

/**
 * Common client loading entrypoint.
 */
public class OneironautClient {

    private static int applyBlockRenderLayers(Collection<Block> blocks, RenderType layer){
        int applied = 0;
        for (Block block : blocks){
            ItemBlockRenderTypes.setRenderLayer(block, layer);
            applied++;
        }
        return applied;
    }

    public static long lastShiftingHoverTick = 0L;
    public static ItemStack lastHoveredShifting = null;
    private static float processObservationPredicate(ItemStack stack, ClientLevel world, LivingEntity holder, int holderID){
        LocalPlayer cachedPlayer = cachedClient.player;
        final float OFF = 0.99f;
        final float ON = -0.01f;
        float output = ON;
        int fov = cachedClient.options.fov().get();
        double threshold = fov / (fov <= 85 ? 90.0 : 100.0);
        if (cachedPlayer != null){
            if (stack.isFramed()){
                assert stack.getFrame() != null;
                if (MiscAPIKt.vecProximity(stack.getFrame().position().subtract(cachedPlayer.getEyePosition()), cachedPlayer.getLookAngle()) <= threshold) {
                    output = OFF;
                }
            }
            if (stack.getEntityRepresentation() != null && stack.getEntityRepresentation() != cachedPlayer){
                Vec3 holderCenterApprox = stack.getEntityRepresentation().getBoundingBox().getCenter();
                if (MiscAPIKt.vecProximity(holderCenterApprox.subtract(cachedPlayer.getEyePosition()), cachedPlayer.getLookAngle()) <= threshold) {
                    output = OFF;
                }
            }
            if (holder == cachedPlayer && (holder.getItemInHand(InteractionHand.MAIN_HAND) == stack || holder.getItemInHand(InteractionHand.OFF_HAND) == stack)){
                output = OFF;
            }
            if (cachedPlayer.containerMenu.getCarried() == stack ||
                    (lastShiftingHoverTick + 1 >= cachedPlayer.level().getGameTime() && lastHoveredShifting == stack)){
                output = OFF;
            }
        }
        if (!cachedClient.isWindowActive()){
            output = ON;
        }
        return output;
    }

    private static Minecraft cachedClient = null;
    public static Minecraft getCachedClient(){
        return cachedClient;
    }
    public static void init() {
        cachedClient = Minecraft.getInstance();

            ScryingLensOverlayRegistry.addDisplayer(OneironautBlockRegistry.WISP_BATTERY.get(),
                    WispBatteryEntity::applyScryingLensOverlay
                    );

            List<RegistrySupplier<ConceptModifierBlock>> conceptModifiers = List.of(OneironautBlockRegistry.CONCEPT_MODIFIER_GRIDSIZE,
                    OneironautBlockRegistry.CONCEPT_MODIFIER_ANTIEROSION, OneironautBlockRegistry.CONCEPT_MODIFIER_MAXHEALTH, OneironautBlockRegistry.CONCEPT_MODIFIER_GTP_DROP,
                    OneironautBlockRegistry.CONCEPT_MODIFIER_REFERENCE_COMPARISON, OneironautBlockRegistry.CONCEPT_MODIFIER_REFERENCE_FALSY, OneironautBlockRegistry.CONCEPT_MODIFIER_STACK_SIZE
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
                    OneironautBlockRegistry.CONCEPT_MODIFIER_GTP_DROP.get(), OneironautBlockRegistry.CONCEPT_MODIFIER_REFERENCE_COMPARISON.get(),
                    OneironautBlockRegistry.CONCEPT_MODIFIER_STACK_SIZE.get(), OneironautBlockRegistry.CONCEPT_CONNECTOR.get(), OneironautBlockRegistry.CONCEPT_CORE.get()));
            for (RegistrySupplier<ConceptDecoratorBlock> supplier : OneironautBlockRegistry.COLORFUL_CONCEPT_MODIFIERS.values()){
                cutoutBlocks.add(supplier.get());
            }
            Block[] translucentBlocks = {OneironautBlockRegistry.RAYCAST_BLOCKER_GLASS.get(), OneironautBlockRegistry.MEDIA_GEL.get(),
                    OneironautBlockRegistry.CELL.get(), OneironautBlockRegistry.INSTANT_BREAKER_RIFTRESIDUE.get(),
                    OneironautBlockRegistry.PSUEDOAMETHYST_BLOCK_INSUBSTANTIAL.get()};

            ItemBlockRenderTypes.setRenderLayer(ThoughtSlurry.STILL_FLUID, RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ThoughtSlurry.FLOWING_FLUID, RenderType.translucent());

            Oneironaut.LOGGER.info("Applied cutout layer to " + applyBlockRenderLayers(cutoutBlocks, RenderType.cutout()) + " blocks");
            Oneironaut.LOGGER.info("Applied translucent layer to " + applyBlockRenderLayers(List.of(translucentBlocks), RenderType.translucent()) + " blocks");

            Oneironaut.LOGGER.info("Registering client-side hoverlift processor.");

            ClientTickEvent.CLIENT_POST.register((client)->{
                try {
                    HoverElevatorBlockEntity.processHover(false, client.level != null ? client.level.getGameTime() : -1L);
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
                InactiveSlipwayBlock.init();
            });
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
                return holder.getUseItem().equals(stack) ? 0.99f : -0.01f;
            } else {
                return -0.01f;
            }
            //return OneironautItemRegistry.REVERBERATION_ROD.get().hasHex(stack) ? 0.99f : -0.01f;
        });
        ItemPropertiesRegistry.register(OneironautItemRegistry.WISP_CAPTURE_ITEM.get(), WispCaptureItem.FILLED_PREDICATE, (stack, world, holder, holderID) -> {
            return ((WispCaptureItem)stack.getItem()).hasWisp(stack) ? 0.99f : -0.01f;
        });

        ItemPropertiesRegistry.register(OneironautItemRegistry.SHIFTING_PSEUDOAMETHYST.get(), Oneironaut.id("observation"),
                OneironautClient::processObservationPredicate);
        ItemPropertiesRegistry.register(OneironautItemRegistry.LIBRARY_CARD.get(), Oneironaut.id("written"), (stack, world, holder, holderID) -> {
            return ((ItemLibraryCard)stack.getItem()).getDimension(stack) != null ? 0.99f : -0.01f;
        });

        //ah yes, because I definitely want to turn my expensive staff into a much less expensive variant
        Item[] nameSensitiveStaves = {OneironautItemRegistry.ECHO_STAFF.get(), OneironautItemRegistry.BEACON_STAFF.get(), OneironautItemRegistry.SPOON_STAFF.get()};
        for (Item staff: nameSensitiveStaves) {
            ItemPropertiesRegistry.register(staff, ItemStaff.FUNNY_LEVEL_PREDICATE, (stack, level, holder, holderID) -> {
                if (!stack.has(DataComponents.CUSTOM_NAME)) {
                    return 0;
                }
                var name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
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

    public static boolean isWorldClientNoosphere(Level world){
        if (world instanceof ClientLevel clientWorld){
            return clientWorld.effects().getClass() == NoosphereDimensionEffects.class
                    || clientWorld.effects().getClass() == DeepNoosphereDimensionEffects.class;
        }
        return false;
    }
    public static boolean isWorldClientDeepNoosphere(Level world){
        if (world instanceof ClientLevel clientWorld){
            return clientWorld.effects().getClass() == DeepNoosphereDimensionEffects.class;
        }
        return false;
    }

}
