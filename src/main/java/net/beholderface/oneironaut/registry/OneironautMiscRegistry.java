package net.beholderface.oneironaut.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.block.ThoughtSlurry;
import net.beholderface.oneironaut.status.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;

public class OneironautMiscRegistry {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Oneironaut.MOD_ID, Registries.FLUID);
    public static final net.neoforged.neoforge.registries.DeferredRegister<FluidType> FLUID_TYPES =
            net.neoforged.neoforge.registries.DeferredRegister.create(
                    NeoForgeRegistries.Keys.FLUID_TYPES, Oneironaut.MOD_ID);
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Oneironaut.MOD_ID, Registries.MOB_EFFECT);
    //I will not scream at my computer over this

    public static void init() {
        FLUIDS.register();
        EFFECTS.register();
    }

    public static void registerNeo(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
    }

    public static final RegistrySupplier<MobEffect> DETECTION_RESISTANCE = EFFECTS.register("detection_resistance", DetectionResistEffect::new);
    public static final RegistrySupplier<MobEffect> NOT_MISSING = EFFECTS.register("not_missing", GlowingAmbitEffect::new);
    public static final RegistrySupplier<MobEffect> RUMINATION = EFFECTS.register("rumination", MonkfruitDelayEffect::new);
    public static final RegistrySupplier<MobEffect> DISINTEGRATION = EFFECTS.register("disintegration", MediaDisintegrationEffect::new);
    public static final RegistrySupplier<MobEffect> DISINTEGRATION_PROTECTION = EFFECTS.register("disintegration_protection", DisintegrationProtectionEffect::new);

    public static final RegistrySupplier<ThoughtSlurry> THOUGHT_SLURRY = FLUIDS.register("thought_slurry", () -> ThoughtSlurry.STILL_FLUID /*new ThoughtSlurry.Still(OneironautThingRegistry.THOUGHT_SLURRY_ATTRIBUTES)*/);
    public static final RegistrySupplier<ThoughtSlurry> THOUGHT_SLURRY_FLOWING = FLUIDS.register("thought_slurry_flowing", () -> ThoughtSlurry.FLOWING_FLUID /*new ThoughtSlurry.Flowing(OneironautThingRegistry.THOUGHT_SLURRY_ATTRIBUTES)*/);
    public static final DeferredHolder<FluidType, FluidType> THOUGHT_SLURRY_TYPE = FLUID_TYPES.register(
            "thought_slurry",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.oneironaut.thought_slurry")
                    .density(1200)
                    .viscosity(1200)
                    .canSwim(true)
                    .canDrown(true)
                    .supportsBoating(true))
    );

    public static final ResourceKey<Enchantment> OVERCAST_DAMAGE_ENCHANT =
            ResourceKey.create(Registries.ENCHANTMENT, Oneironaut.id("overcast_damage"));
}
