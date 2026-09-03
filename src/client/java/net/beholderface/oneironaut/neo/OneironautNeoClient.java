package net.beholderface.oneironaut.neo;

import net.beholderface.oneironaut.DeepNoosphereDimensionEffects;
import net.beholderface.oneironaut.NoosphereDimensionEffects;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.OneironautClient;
import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.lang.reflect.InvocationTargetException;

@EventBusSubscriber(modid = Oneironaut.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class OneironautNeoClient {
    private static final ResourceLocation SLURRY_STILL = Oneironaut.id("block/thought_slurry");
    private static final ResourceLocation SLURRY_FLOWING = Oneironaut.id("block/thought_slurry_flowing");

    private OneironautNeoClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            OneironautClient.init();
            registerDevelopmentProbe();
        });
    }

    private static void registerDevelopmentProbe() {
        if (!Boolean.getBoolean("oneironaut.probe.validateRiftResidueTooltip")) {
            return;
        }
        String className = "net.beholderface.oneironaut.neo.probe.OneironautClientProbe";
        try {
            Class.forName(className).getMethod("register").invoke(null);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Requested Oneironaut client probe is absent: " + className, exception);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Could not register Oneironaut client probe", exception);
        }
    }

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(Oneironaut.id("noosphere"), new NoosphereDimensionEffects());
        event.register(Oneironaut.id("deep_noosphere"), new DeepNoosphereDimensionEffects());
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return SLURRY_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return SLURRY_FLOWING;
            }

            @Override
            public int getTintColor() {
                return 0xFF8621C2;
            }
        }, OneironautMiscRegistry.THOUGHT_SLURRY_TYPE.get());
    }
}
