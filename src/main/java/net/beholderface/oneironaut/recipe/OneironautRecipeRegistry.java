package net.beholderface.oneironaut.recipe;

import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge-owned registration for Oneironaut's non-crafting infusion recipe. */
public final class OneironautRecipeRegistry {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Oneironaut.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Oneironaut.MOD_ID);

    public static final RecipeSerializer<InfusionRecipe> INFUSE_SERIALIZER =
            new InfusionRecipe.Serializer();
    public static final RecipeType<InfusionRecipe> INFUSION_TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return Oneironaut.MOD_ID + ":infuse";
        }
    };

    static {
        SERIALIZERS.register("infuse", () -> INFUSE_SERIALIZER);
        TYPES.register("infuse", () -> INFUSION_TYPE);
    }

    private OneironautRecipeRegistry() {
    }

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
        TYPES.register(modBus);
    }
}
