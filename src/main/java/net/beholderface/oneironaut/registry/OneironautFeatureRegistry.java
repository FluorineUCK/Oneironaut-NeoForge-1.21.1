package net.beholderface.oneironaut.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.feature.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;

public class OneironautFeatureRegistry {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Oneironaut.MOD_ID, Registries.FEATURE);
    public static void init(){
        FEATURES.register();
    }
    //islands
    public static final RegistrySupplier<Feature<NoosphereSeaIslandConfig>> NOOSPHERE_SEA_ISLAND = FEATURES.register("noosphere_sea_island", () -> new NoosphereSeaIsland(NoosphereSeaIslandConfig.CODEC));
    public static final RegistrySupplier<Feature<NoosphereSeaVolcanoConfig>> NOOSPHERE_SEA_VOLCANO = FEATURES.register("noosphere_sea_volcano", () -> new NoosphereSeaVolcano(NoosphereSeaVolcanoConfig.CODEC));
    public static final RegistrySupplier<Feature<BlockVeinConfig>> BLOCK_VEIN = FEATURES.register("block_vein", () -> new BlockVein(BlockVeinConfig.CODEC));
    public static final RegistrySupplier<Feature<BlockBlobConfig>> BLOCK_BLOB = FEATURES.register("block_blob", () -> new BlockBlob(BlockBlobConfig.CODEC));
}
