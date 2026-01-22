package net.beholderface.oneironaut.block.blockentity;

import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InstantBreakingBlockEntity extends BlockEntity {
    public InstantBreakingBlockEntity(BlockPos pos, BlockState state) {
        super(OneironautBlockRegistry.INSTANT_BREAKER_ENTITY.get(), pos, state);
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos pos){
        world.breakBlock(pos, true);
    }
}
