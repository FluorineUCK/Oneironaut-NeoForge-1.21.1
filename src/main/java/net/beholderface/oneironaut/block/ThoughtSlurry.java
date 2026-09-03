package net.beholderface.oneironaut.block;

import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.registry.OneironautItemRegistry;
import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;



public class ThoughtSlurry extends FlowingFluid {
    @Override
    public FluidType getFluidType() {
        return OneironautMiscRegistry.THOUGHT_SLURRY_TYPE.get();
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == getSource() || fluid == getFlowing();
    }
    public static final ResourceLocation ID =
            ResourceLocation.tryBuild(Oneironaut.MOD_ID, "thought_slurry");



    public static final ResourceLocation FLOWING_ID =
            ResourceLocation.tryBuild(Oneironaut.MOD_ID, "flowing_thought_slurry");

    public static final ThoughtSlurry.Flowing FLOWING_FLUID =
            new ThoughtSlurry.Flowing();
    public static final ThoughtSlurry.Still STILL_FLUID =
            new ThoughtSlurry.Still();

    //public static FlowableFluid THOUGHT_SLURRY;
    //public static FlowableFluid THOUGHT_SLURRY_FLOWING;

    public static final TagKey<Fluid> TAG =
            TagKey.create(Registries.FLUID, ThoughtSlurry.ID);

    @Override
    public Fluid getFlowing() {
        return OneironautMiscRegistry.THOUGHT_SLURRY_FLOWING.get();
    }

    @Override
    public Fluid getSource() {
        return OneironautMiscRegistry.THOUGHT_SLURRY.get();
    }

@Override
    public FluidState getFlowing(int level, boolean falling) {
        return (this.getFlowing().defaultFluidState().setValue(LEVEL, level)).setValue(FALLING, falling);
        //return ThoughtSlurry.FLOWING_FLUID;
    }


/*@Override
    public Fluid getFlowing() {
        return Flowing.FLOWING_FLUID;
    }*/




    @Override
    protected boolean canConvertToSource(Level world) {
        return true;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor world, BlockPos pos, BlockState state) {
        final BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropResources(state, world, pos, blockEntity);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader world) {
        return 3;
    }

    @Override
    protected int getDropOff(LevelReader world) {
        return 1;
    }

@Override
    public Item getBucket() {
        return OneironautItemRegistry.THOUGHT_SLURRY_BUCKET.get();
       // return Items.LAVA_BUCKET;
    }


    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    public int getTickDelay(LevelReader world) {
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0f;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return ThoughtSlurryBlock.INSTANCE.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSource(FluidState state) {
        return state.isSource();
    }

    @Override
    public int getAmount(FluidState state) {
        //return state.getLevel();
        return 8;
    }

    public static class Flowing extends ThoughtSlurry {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(FlowingFluid.LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(FlowingFluid.LEVEL);
        }

    }

    public static class Still extends ThoughtSlurry {
        @Override
        public boolean isSource(FluidState state) {
            return true;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

    }


}
