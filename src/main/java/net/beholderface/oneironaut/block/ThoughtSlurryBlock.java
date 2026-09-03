package net.beholderface.oneironaut.block;

import net.minecraft.world.level.block.state.BlockState;

import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
//import net.minecraft.fluid.Fluid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.beholderface.oneironaut.Oneironaut;
//import net.oneironaut.block.ThoughtSlurry;
//import software.bernie.shadowed.eliotlash.mclib.math.functions.classic.Abs;

public class ThoughtSlurryBlock extends LiquidBlock {
    public static final ResourceLocation ID =
            ResourceLocation.tryBuild(Oneironaut.MOD_ID, "thought_slurry");
    public static final BlockBehaviour.Properties SETTINGS =
            BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noOcclusion().mapColor(MapColor.COLOR_PURPLE);
    public static final ThoughtSlurryBlock INSTANCE =
            new ThoughtSlurryBlock(ThoughtSlurry.STILL_FLUID, SETTINGS);

    public ThoughtSlurryBlock(ThoughtSlurry thoughtSlurry, BlockBehaviour.Properties settings) {
        super(thoughtSlurry, settings);
    }

    //@Override
    public boolean isTransparent(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }
    public boolean canPathfindThrough(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return true;
    }
}
