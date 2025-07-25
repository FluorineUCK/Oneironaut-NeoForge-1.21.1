package net.beholderface.oneironaut;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.items.ItemStaff;
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder;
import at.petrak.hexcasting.common.lib.HexItems;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.beholderface.oneironaut.block.InactiveSlipwayBlock;
import net.beholderface.oneironaut.block.blockentity.HoverElevatorBlockEntity;
import net.beholderface.oneironaut.casting.DepartureEntry;
import net.beholderface.oneironaut.casting.IdeaInscriptionManager;
import net.beholderface.oneironaut.casting.OvercastDamageEnchant;
import net.beholderface.oneironaut.casting.lichdom.LichData;
import net.beholderface.oneironaut.casting.lichdom.LichMediaExtractComponent;
import net.beholderface.oneironaut.casting.lichdom.LichdomManager;
import net.beholderface.oneironaut.item.BottomlessMediaItem;
import net.beholderface.oneironaut.recipe.OneironautRecipeSerializer;
import net.beholderface.oneironaut.recipe.OneironautRecipeTypes;
import net.beholderface.oneironaut.registry.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ram.talia.hexal.common.entities.WanderingWisp;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
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
    private static ServerWorld noosphere = null;
    private static ServerWorld deepNoosphere = null;
    private static MinecraftServer server = null;


    public static void init() {
        LOGGER.info("why do they call it oven when you of in the cold food of out hot eat the food");
        OneironautMiscRegistry.init();
        OneironautBlockRegistry.init();
        OneironautItemRegistry.init();
        OneironautFeatureRegistry.init();
        OneironautIotaTypeRegistry.init();
        OneironautPatternRegistry.init();
        OneironautRecipeSerializer.registerSerializers(OneironautRecipeTypes.Companion.bind(Registries.RECIPE_SERIALIZER));
        OneironautRecipeTypes.registerTypes(OneironautRecipeTypes.Companion.bind(Registries.RECIPE_TYPE));

        LifecycleEvent.SERVER_STARTED.register((startedserver) ->{
            server = startedserver;
            noosphere = stringToWorld("oneironaut:noosphere", startedserver);
            deepNoosphere = stringToWorld("oneironaut:deep_noosphere", startedserver);
            IdeaInscriptionManager ideaState = IdeaInscriptionManager.getServerState(startedserver);
            IdeaInscriptionManager.cleanMap(startedserver, ideaState);
            ideaState.markDirty();
            LichdomManager lichState = LichdomManager.getServerState(startedserver);
            lichState.markDirty();
            randomWispPigments.addAll(HexItems.DYE_PIGMENTS.values());
            randomWispPigments.addAll(HexItems.PRIDE_PIGMENTS.values());
            randomWispPigments.add(HexItems.DEFAULT_PIGMENT);
            randomWispPigments.add(HexItems.UUID_PIGMENT);
            randomWispPigments.add(OneironautItemRegistry.PIGMENT_NOOSPHERE.get());
            randomWispPigments.add(OneironautItemRegistry.PIGMENT_FLAME.get());
            OneironautCastEnvComponents.init();
            InactiveSlipwayBlock.init();
        });

        TickEvent.SERVER_PRE.register((server) -> {
            BottomlessMediaItem.time = server.getOverworld().getTime();
        });

        LOGGER.info("Registering server-side hoverlift processor.");
        TickEvent.SERVER_POST.register((server)->{
            try {
                HoverElevatorBlockEntity.processHover(true, server.getOverworld().getTime());
            } catch (ConcurrentModificationException exception){
                LOGGER.error("Oopsie server-side hoverlift exception " + exception.getMessage());
            }
            DepartureEntry.clearMap();
            ServerPlayerEntity noospherePlayer = noosphere.getRandomAlivePlayer();
            Random rand = noosphere.random;
            if (noospherePlayer != null && rand.nextInt(1024) == 0){
                double gaussDistance = 16.0;
                WanderingWisp wisp = new WanderingWisp(noosphere, noospherePlayer.getPos().add(
                        rand.nextGaussian() * gaussDistance, rand.nextGaussian() * gaussDistance, rand.nextGaussian() * gaussDistance));
                ItemStack stack = randomWispPigments.get(rand.nextInt(randomWispPigments.size())).getDefaultStack();
                wisp.setPigment(new FrozenPigment(stack, ((Entity)wisp).getUuid()));
                noosphere.spawnEntity(wisp);
            }
            if (deepNoosphere.getTime() % 20 == 0){
                for (ServerPlayerEntity player : deepNoosphere.getPlayers()){
                    if (!(player.isCreative() || player.isSpectator())){
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 10));
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 10));
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 60, 10));
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 60, 10));
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.UNLUCK, 60, 10));
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 10));
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0));
                        player.removeStatusEffect(StatusEffects.REGENERATION);
                        player.removeStatusEffect(StatusEffects.ABSORPTION);
                        player.removeStatusEffect(StatusEffects.RESISTANCE);
                        player.removeStatusEffect(StatusEffects.WATER_BREATHING);
                        player.removeStatusEffect(StatusEffects.SATURATION);
                        OvercastDamageEnchant.applyMindDamage(null, player, (int)Math.floor(player.getMaxHealth() / 6f), false);
                    }
                }
            }
            LichdomManager.tick(server);
        });

        PlayerEvent.PLAYER_RESPAWN.register((player, leavingEnd)->{
            if (LichdomManager.isPlayerLich(player) && !leavingEnd){
                LichData data = LichdomManager.getLichData(player);
                data.setMedia(data.getMedia() / 2);
            }
        });

        ItemStack fakeStaffStack = HexItems.STAFF_OAK.getDefaultStack();
        TagKey<Item> realStaffTag = getItemTagKey(new Identifier("hexcasting:staves"));
        TagKey<Item> fakeStaffTag = getItemTagKey(new Identifier("oneironaut:datapack_staves"));
        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            ItemStack heldStack = player.getStackInHand(hand);
            if (heldStack.isIn(fakeStaffTag) && !(heldStack.getItem() instanceof ItemStaff)){
                if (heldStack.isIn(realStaffTag)){
                    fakeStaffStack.use(player.getWorld(), player, hand);
                    player.swingHand(hand);
                } else {
                    LOGGER.info(player.getName().getString() + " has right-clicked an item tagged as a datapacked staff, but that item does not have the normal staff tag, which is necessary for the datapack staff functionality to work.");
                }
            }
            return CompoundEventResult.pass();
        });
        //IdeaInscriptionManager ideaState = IdeaInscriptionManager.getServerState()
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
    public static Identifier id(String string) {
        return new Identifier(MOD_ID, string);
    }
    public static ServerWorld getNoosphere(){
        if (noosphere == null){
            throw new IllegalStateException("getNoosphere method called before server start");
        }
        return noosphere;
    }
    public static ServerWorld getDeepNoosphere(){
        if (deepNoosphere == null){
            throw new IllegalStateException("getDeepNoosphere method called before server start");
        }
        return deepNoosphere;
    }
    public static MinecraftServer getCachedServer(){
        if (server == null){
            throw new IllegalStateException("getCachedServer method called before server start");
        }
        return server;
    }
}
