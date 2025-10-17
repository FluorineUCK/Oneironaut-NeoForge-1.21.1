package net.beholderface.oneironaut.block;

import net.beholderface.oneironaut.block.blockentity.TransformingSkullBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TranformingSkullBlock extends SkullBlock {

    public TranformingSkullBlock(Settings settings) {
        super(Type.PLAYER, settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TransformingSkullBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClient) {
            return (_world, _pos, _state, _be) -> TransformingSkullBlockEntity.tick(_world, _pos, _state, false);
        }
        return null;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state){
        return BlockRenderType.INVISIBLE;
    }
}
