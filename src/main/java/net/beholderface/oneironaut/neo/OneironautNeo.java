package net.beholderface.oneironaut.neo;

import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.OneironautConfig;
import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import net.beholderface.oneironaut.registry.OneironautPatternRegistry;
import net.beholderface.oneironaut.recipe.OneironautRecipeRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Oneironaut.MOD_ID)
public final class OneironautNeo {
    public OneironautNeo(IEventBus modBus, ModContainer modContainer) {
        OneironautConfig.setCommon(OneironautNeoConfig.COMMON);
        OneironautConfig.setClient(OneironautNeoConfig.CLIENT);
        OneironautConfig.setServer(OneironautNeoConfig.SERVER);
        modContainer.registerConfig(ModConfig.Type.SERVER, OneironautNeoConfig.SERVER_SPEC);

        OneironautAttachments.register(modBus);
        OneironautMiscRegistry.registerNeo(modBus);
        OneironautNetworking.register(modBus);
        OneironautRecipeRegistry.register(modBus);
        modBus.addListener(OneironautCapabilities::register);
        modBus.addListener(OneironautPatternRegistry::register);
        NeoForge.EVENT_BUS.addListener(OneironautGameplayEvents::onEntityTick);
        NeoForge.EVENT_BUS.addListener(OneironautGameplayEvents::onLivingDamage);
        NeoForge.EVENT_BUS.addListener(OneironautGameplayEvents::onMobEffectRemoved);
        NeoForge.EVENT_BUS.addListener(OneironautGameplayEvents::onMobEffectExpired);
        Oneironaut.init();
        Oneironaut.LOGGER.info("Oneironaut NeoForge pre-39 compatibility port initialized");
    }
}
