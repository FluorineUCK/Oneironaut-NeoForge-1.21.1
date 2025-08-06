package net.beholderface.oneironaut.block;

import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.beholderface.oneironaut.registry.OneironautItemRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public interface Slurryloggable extends Waterloggable {
    BooleanProperty slurrylogged = BooleanProperty.of("slurrylogged");
    @Override
    default boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid){
        return fluid == Fluids.WATER || fluid == ThoughtSlurry.STILL_FLUID.getStill(false).getFluid();
    }

    @Override
    default boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!(state.get(Properties.WATERLOGGED) || state.get(slurrylogged))) {
            BooleanProperty property = null;
            if (fluidState.getFluid() == Fluids.WATER){
                property = Properties.WATERLOGGED;
            } else if (fluidState.getFluid() == ThoughtSlurry.STILL_FLUID.getStill(false).getFluid()) {
                property = slurrylogged;
            }
            if (!world.isClient() && property != null) {
                world.setBlockState(pos, (BlockState)state.with(property, true), 3);
                world.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    default ItemStack tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
        if (state.get(Properties.WATERLOGGED)) {
            world.setBlockState(pos, (BlockState)state.with(Properties.WATERLOGGED, false), 3);
            if (!state.canPlaceAt(world, pos)) {
                world.breakBlock(pos, true);
            }
            return new ItemStack(Items.WATER_BUCKET);
        } else if (state.get(slurrylogged)){
            world.setBlockState(pos, (BlockState)state.with(slurrylogged, false), 3);
            if (!state.canPlaceAt(world, pos)) {
                world.breakBlock(pos, true);
            }
            return new ItemStack(OneironautItemRegistry.THOUGHT_SLURRY_BUCKET.get());
        } else {
            return ItemStack.EMPTY;
        }
    }
}
