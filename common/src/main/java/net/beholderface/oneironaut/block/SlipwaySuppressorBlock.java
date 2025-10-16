package net.beholderface.oneironaut.block;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.block.blockentity.WispBatteryEntity;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import ram.talia.hexal.common.blocks.BlockSlipway;
import ram.talia.hexal.common.blocks.entity.BlockEntitySlipway;
import ram.talia.hexal.common.lib.HexalBlockEntities;
import ram.talia.hexal.common.lib.HexalBlocks;

public class SlipwaySuppressorBlock extends Block {
    public static final BooleanProperty REDSTONE_POWERED = Properties.POWERED;
    public SlipwaySuppressorBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(REDSTONE_POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(REDSTONE_POWERED);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return world.getBlockState(pos.up()).getBlock() instanceof BlockSlipway ? 15 : 0;
    }

    //it doesn't actually, I just want redstone to point at it
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext pContext) {
        return this.getDefaultState().with(REDSTONE_POWERED, pContext.getWorld().isReceivingRedstonePower(pContext.getBlockPos()));
    }

    private boolean breaking = false;
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos pFromPos,
                               boolean pIsMoving) {
        super.neighborUpdate(state, world, pos, block, pFromPos, pIsMoving);
        try {
            boolean currentlyPowered = state.get(REDSTONE_POWERED);
            boolean detectedPower = world.isReceivingRedstonePower(pos);
            if (currentlyPowered != detectedPower) {
                world.setBlockState(pos, state.with(REDSTONE_POWERED, detectedPower), 2);
                currentlyPowered = detectedPower;
            }
            BlockPos up = pos.up();
            boolean slipwayDisabled = world.getBlockState(up).getBlock() instanceof InactiveSlipwayBlock;
            if (currentlyPowered != slipwayDisabled && !breaking){
                if (slipwayDisabled){
                    reactivateSlipway(world, up);
                } else if (world.getBlockState(up).getBlock() instanceof BlockSlipway) {
                    world.setBlockState(up, OneironautBlockRegistry.INACTIVE_SLIPWAY.get().getDefaultState());
                }
            }
            world.updateComparators(pos, this);
        } catch (Throwable t){
            //just making sure that some weird bug doesn't suppress the suppressors
        }
        breaking = false;
    }

    public static void reactivateSlipway(World world, BlockPos target){
        world.setBlockState(target, HexalBlocks.SLIPWAY.getDefaultState());
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean(BlockEntitySlipway.TAG_IS_ACTIVE, true);
        nbt.putLong(BlockEntitySlipway.TAG_NEXT_SPAWN_TICK, world.getTime() + 100L);
        BlockEntitySlipway.writeIdToNbt(nbt, HexalBlockEntities.SLIPWAY);
        BlockEntitySlipway slipwayEntity = (BlockEntitySlipway) BlockEntitySlipway.createFromNbt(target, HexalBlocks.SLIPWAY.getDefaultState(), nbt);
        if (slipwayEntity == null){
            return;
        }
        world.addBlockEntity(slipwayEntity);
        slipwayEntity.markDirty();
    }

    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        breaking = true;
        if (world.getBlockState(pos.up()).getBlock() instanceof InactiveSlipwayBlock){
            reactivateSlipway(world, pos.up());
        }
    }
}
