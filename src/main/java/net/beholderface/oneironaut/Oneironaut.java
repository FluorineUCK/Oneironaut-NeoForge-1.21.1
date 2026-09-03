package net.beholderface.oneironaut;

import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.items.ItemStaff;
import at.petrak.hexcasting.common.lib.HexItems;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.beholderface.oneironaut.block.InactiveSlipwayBlock;
import net.beholderface.oneironaut.block.blockentity.HoverElevatorBlockEntity;
import net.beholderface.oneironaut.casting.DepartureEntry;
import net.beholderface.oneironaut.casting.DisintegrationProtectionManager;
import net.beholderface.oneironaut.casting.idea.IdeaInscriptionManager;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifier;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifierManager;
import net.beholderface.oneironaut.hexcompat.PerWorldPatternReconciler;
import net.beholderface.oneironaut.item.BottomlessMediaItem;
import net.beholderface.oneironaut.registry.*;
import net.beholderface.oneironaut.status.MediaDisintegrationEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ram.talia.hexal.common.entities.WanderingWisp;

import java.util.*;
import java.util.function.Consumer;

import static net.beholderface.oneironaut.MiscAPIKt.getItemTagKey;
import static net.beholderface.oneironaut.MiscAPIKt.stringToWorld;

/**
 * This is effectively the loading entrypoint for most of your code, at least
 * if you are using Architectury as intended.
 */
public class Oneironaut {
    public static final String MOD_ID = "oneironaut";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static final List<Item> randomWispPigments = new ArrayList<>();
    private static ServerLevel noosphere = null;
    private static ServerLevel deepNoosphere = null;
    private static MinecraftServer server = null;


    public static final Set<Tuple<LivingEntity, MobEffectInstance>> reapplicationSet = new HashSet<>();
    public static void init() {
        LOGGER.info("why do they call it oven when you of in the cold food of out hot eat the food");
        OneironautMiscRegistry.init();
        OneironautBlockRegistry.init();
        OneironautItemRegistry.init();
        OneironautFeatureRegistry.init();
        OneironautIotaTypeRegistry.init();
        LifecycleEvent.SERVER_STARTED.register((startedserver) ->{
            PerWorldPatternReconciler.reconcile(startedserver);
            server = startedserver;
            noosphere = stringToWorld("oneironaut:noosphere", startedserver);
            deepNoosphere = stringToWorld("oneironaut:deep_noosphere", startedserver);

            IdeaInscriptionManager ideaState = IdeaInscriptionManager.getServerState(startedserver);
            IdeaInscriptionManager.cleanMap(startedserver, ideaState);
            ideaState.setDirty();

            DisintegrationProtectionManager disintegrationState = DisintegrationProtectionManager.getServerState(startedserver);
            disintegrationState.cleanEntries();
            disintegrationState.setDirty();

            ConceptModifierManager conceptState = ConceptModifierManager.getServerState(startedserver);
            conceptState.verifyModifiers();
            conceptState.setDirty();

            HexItems.DYE_PIGMENTS.values().forEach(pigment -> randomWispPigments.add(pigment.get()));
            HexItems.PRIDE_PIGMENTS.values().forEach(pigment -> randomWispPigments.add(pigment.get()));
            randomWispPigments.add(HexItems.DEFAULT_PIGMENT.get());
            randomWispPigments.add(HexItems.UUID_PIGMENT.get());
            randomWispPigments.add(OneironautItemRegistry.PIGMENT_NOOSPHERE.get());
            randomWispPigments.add(OneironautItemRegistry.PIGMENT_FLAME.get());
            randomWispPigments.add(OneironautItemRegistry.PIGMENT_FRENZY.get());
            randomWispPigments.add(OneironautItemRegistry.PIGMENT_CLOCK.get());
            OneironautCastEnvComponents.init();
            InactiveSlipwayBlock.init();
        });

        TickEvent.SERVER_PRE.register((server) -> {
            BottomlessMediaItem.time = server.overworld().getGameTime();
        });

        LOGGER.info("Registering server-side hoverlift processor.");
        TickEvent.SERVER_POST.register((server)->{
            try {
                HoverElevatorBlockEntity.processHover(true, server.overworld().getGameTime());
            } catch (ConcurrentModificationException exception){
                LOGGER.error("Oopsie server-side hoverlift exception {}", exception.getMessage());
            }
            DepartureEntry.clearMap();
            ServerPlayer noospherePlayer = noosphere.getRandomPlayer();
            RandomSource rand = noosphere.random;
            if (noospherePlayer != null && rand.nextInt(1024) == 0){
                double gaussDistance = 16.0;
                WanderingWisp wisp = new WanderingWisp(noosphere, noospherePlayer.position().add(
                        rand.nextGaussian() * gaussDistance, rand.nextGaussian() * gaussDistance, rand.nextGaussian() * gaussDistance));
                ItemStack stack = randomWispPigments.get(rand.nextInt(randomWispPigments.size())).getDefaultInstance();
                wisp.setPigment(new FrozenPigment(stack, ((Entity)wisp).getUUID()));
                noosphere.addFreshEntity(wisp);
            }
            for (Tuple<LivingEntity, MobEffectInstance> pair : reapplicationSet){
                if (!pair.getA().hasEffect(pair.getB().getEffect())){
                    pair.getA().addEffect(pair.getB());
                }
            }
            reapplicationSet.clear();
        });

        PlayerEvent.PLAYER_RESPAWN.register((player, leavingEnd, removalReason)->{
            ConceptModifierManager conceptModifierManager = ConceptModifierManager.getServerState(player.server);
            for (ConceptModifier modifier : conceptModifierManager.getAllModifiers(player)){
                modifier.onApply(player);
            }
        });
        PlayerEvent.PLAYER_JOIN.register((player)->{
            ConceptModifierManager conceptModifierManager = ConceptModifierManager.getServerState(player.server);
            for (ConceptModifier modifier : conceptModifierManager.getAllModifiers(player)){
                modifier.onApply(player);
            }
        });

        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            ItemStack heldStack = player.getItemInHand(hand);
            if (heldStack.is(OneironautTags.Items.datapackStaves) && !(heldStack.getItem() instanceof ItemStaff)){
                if (heldStack.is(HexTags.Items.STAVES)){
                    ItemStack fakeStaffStack = HexItems.STAFF_OAK.get().getDefaultInstance();
                    fakeStaffStack.use(player.level(), player, hand);
                    player.swing(hand);
                } else {
                    LOGGER.info(player.getName().getString() + " has right-clicked an item tagged as a datapacked staff, but that item does not have the normal staff tag, which is necessary for the datapack staff functionality to work.");
                }
            }
            return CompoundEventResult.pass();
        });
    }

    public enum Loggers {
        INFO,
        DEBUG,
        WARN,
        ERROR,
        FATAL,
        TRACE
    }

    //for easily toggling whether several things should be logged without having to search through the whole file
    public static void boolLogger(String str, boolean bool){
        boolLogger(str, bool, Loggers.INFO);
    }

    public static void boolLogger(String str, boolean bool, Loggers logType){
        if (bool){
            Consumer<String> logger = switch (logType) {
                case DEBUG -> LOGGER::debug;
                case WARN -> LOGGER::warn;
                case ERROR -> LOGGER::error;
                case FATAL -> LOGGER::fatal;
                case TRACE -> LOGGER::trace;
                default -> LOGGER::info;
            };
            logger.accept(str);
        }
    }

    /**
     * Shortcut for identifiers specific to this mod.
     */
    public static ResourceLocation id(String string) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, string);
    }
    public static ServerLevel getNoosphere(){
        if (noosphere == null){
            throw new IllegalStateException("getNoosphere method called before server start");
        }
        return noosphere;
    }
    public static ServerLevel getDeepNoosphere(){
        if (deepNoosphere == null){
            throw new IllegalStateException("getDeepNoosphere method called before server start");
        }
        return deepNoosphere;
    }
    public static boolean isWorldNoosphere(Level world){
        try {
            if (world != null){
                if (world instanceof ServerLevel serverWorld){
                    return serverWorld == noosphere || serverWorld == deepNoosphere;
                } else if (world.isClientSide){
                    return OneironautClient.isWorldClientNoosphere(world);
                }
            }
        } catch (Exception e){
            //just let it return false
        }
        return false;
    }
    public static boolean isWorldDeepNoosphere(Level world){
        try {
            if (world != null){
                if (world instanceof ServerLevel serverWorld){
                    return serverWorld == deepNoosphere;
                } else if (world.isClientSide){
                    return OneironautClient.isWorldClientDeepNoosphere(world);
                }
            }
        } catch (Exception e){
            //just let it return false
        }
        return false;
    }
    public static MinecraftServer getCachedServer(){
        if (server == null){
            throw new IllegalStateException("getCachedServer method called before server start. or on client. or something else, idfk");
        }
        return server;
    }

    public static MinecraftServer getCachedServerOrNull() {
        return server;
    }

    public static boolean isServerThread(){
        if (server != null){
            return Thread.currentThread() == server.getRunningThread();
        }
        return false;
    }

    public static void processDisintegration(LivingEntity entity){
        /*if (entity instanceof ServerPlayerEntity player){
            if (ConceptModifierManager.getServerState(player.server).hasModifierType(player, ConceptModifier.ModifierType.ANTIEROSION)){
                return;
            }
        }*/
        if (!entity.hasEffect(OneironautMiscRegistry.DISINTEGRATION_PROTECTION)){
            if (!entity.hasEffect(OneironautMiscRegistry.DISINTEGRATION)){
                entity.addEffect(new MobEffectInstance(OneironautMiscRegistry.DISINTEGRATION, 100, 0, true, true));
            } else {
                MobEffectInstance instance = entity.getEffect(OneironautMiscRegistry.DISINTEGRATION);
                if (instance != null && instance.getDuration() <= 40){
                    entity.addEffect(new MobEffectInstance(
                            OneironautMiscRegistry.DISINTEGRATION,
                            instance.getDuration() + 90,
                            instance.getAmplifier(),
                            instance.isAmbient(),
                            instance.isVisible(),
                            instance.showIcon()
                    ));
                }
            }
        }
    }
}
