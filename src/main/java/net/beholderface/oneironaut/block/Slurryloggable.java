package net.beholderface.oneironaut.block;

import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.beholderface.oneironaut.registry.OneironautItemRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;

public interface Slurryloggable extends SimpleWaterloggedBlock {
    BooleanProperty slurrylogged = BooleanProperty.create("slurrylogged");
    @Override
    default boolean canPlaceLiquid(Player player, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid){
        return fluid == Fluids.WATER || fluid == ThoughtSlurry.STILL_FLUID;
    }

    @Override
    default boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!(state.getValue(BlockStateProperties.WATERLOGGED) || state.getValue(slurrylogged))) {
            BooleanProperty property = null;
            if (fluidState.getType() == Fluids.WATER){
                property = BlockStateProperties.WATERLOGGED;
            } else if (fluidState.getType() == ThoughtSlurry.STILL_FLUID) {
                property = slurrylogged;
            }
            if (!world.isClientSide() && property != null) {
                world.setBlock(pos, (BlockState)state.setValue(property, true), 3);
                world.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(world));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    default ItemStack pickupBlock(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            world.setBlock(pos, (BlockState)state.setValue(BlockStateProperties.WATERLOGGED, false), 3);
            if (!state.canSurvive(world, pos)) {
                world.destroyBlock(pos, true);
            }
            return new ItemStack(Items.WATER_BUCKET);
        } else if (state.getValue(slurrylogged)){
            world.setBlock(pos, (BlockState)state.setValue(slurrylogged, false), 3);
            if (!state.canSurvive(world, pos)) {
                world.destroyBlock(pos, true);
            }
            return new ItemStack(OneironautItemRegistry.THOUGHT_SLURRY_BUCKET.get());
        } else {
            return ItemStack.EMPTY;
        }
    }
}
