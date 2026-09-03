package net.beholderface.oneironaut.recipe

import at.petrak.hexcasting.common.lib.HexStateIngredients
import at.petrak.hexcasting.common.recipe.RecipeSerializerBase
import at.petrak.hexcasting.common.recipe.ingredient.state.StateIngredient
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderLookup
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

/** A non-crafting recipe queried by the media-infusion spell. */
data class InfusionRecipe(
    val blockIn: StateIngredient,
    val blockOut: BlockState,
    val mediaCost: Long,
) : Recipe<InfusionRecipe.Input> {
    data class Input(val state: BlockState) : RecipeInput {
        override fun getItem(index: Int): ItemStack = ItemStack.EMPTY
        override fun size(): Int = 0
    }

    override fun matches(input: Input, world: Level): Boolean = blockIn.test(input.state)
    fun matches(state: BlockState): Boolean = blockIn.test(state)

    override fun assemble(input: Input, registries: HolderLookup.Provider): ItemStack = ItemStack.EMPTY
    override fun canCraftInDimensions(width: Int, height: Int): Boolean = false
    override fun getResultItem(registries: HolderLookup.Provider): ItemStack = ItemStack.EMPTY
    override fun getSerializer(): RecipeSerializer<*> = OneironautRecipeRegistry.INFUSE_SERIALIZER
    override fun getType(): RecipeType<*> = OneironautRecipeRegistry.INFUSION_TYPE

    class Serializer : RecipeSerializerBase<InfusionRecipe>() {
        private val recipeCodec: MapCodec<InfusionRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                HexStateIngredients.TYPED_CODEC.fieldOf("blockIn").forGetter(InfusionRecipe::blockIn),
                BlockState.CODEC.fieldOf("resultType").forGetter(InfusionRecipe::blockOut),
                com.mojang.serialization.Codec.LONG.fieldOf("mediaCost").forGetter(InfusionRecipe::mediaCost),
            ).apply(instance, ::InfusionRecipe)
        }

        private val recipeStreamCodec = object : StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> {
            override fun decode(buf: RegistryFriendlyByteBuf): InfusionRecipe = InfusionRecipe(
                HexStateIngredients.TYPED_STREAM_CODEC.decode(buf),
                Block.stateById(buf.readVarInt()),
                buf.readLong(),
            )

            override fun encode(buf: RegistryFriendlyByteBuf, recipe: InfusionRecipe) {
                HexStateIngredients.TYPED_STREAM_CODEC.encode(buf, recipe.blockIn)
                buf.writeVarInt(Block.getId(recipe.blockOut))
                buf.writeLong(recipe.mediaCost)
            }
        }

        override fun codec(): MapCodec<InfusionRecipe> = recipeCodec
        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> = recipeStreamCodec
    }
}
