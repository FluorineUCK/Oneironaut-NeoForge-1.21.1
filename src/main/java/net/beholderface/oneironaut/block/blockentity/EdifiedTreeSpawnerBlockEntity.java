package net.beholderface.oneironaut.block.blockentity;

import at.petrak.hexcasting.common.misc.AkashicTreeGrower;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class EdifiedTreeSpawnerBlockEntity extends BlockEntity {
    public EdifiedTreeSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(OneironautBlockRegistry.EDIFIED_TREE_SPAWNER_ENTITY.get(), pos, state);
    }
    public void tick(Level world, BlockPos pos, BlockState state){
        if (!world.isClientSide && world instanceof ServerLevel serverWorld){
            //world.setBlockState(pos, Blocks.OAK_SAPLING.defaultBlockState());
            AkashicTreeGrower.INSTANCE.growTree(serverWorld, serverWorld.getChunkSource().getGenerator(), pos, state, world.random);
        }
    }
}
