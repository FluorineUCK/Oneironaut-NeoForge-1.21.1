package net.beholderface.oneironaut.block;

import net.beholderface.oneironaut.block.blockentity.TransformingSkullBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TranformingWallSkullBlock extends WallSkullBlock {

    public TranformingWallSkullBlock(Settings settings) {
        super(SkullBlock.Type.PLAYER, settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TransformingSkullBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!world.isClient) {
            return (_world, _pos, _state, _be) -> TransformingSkullBlockEntity.tick(_world, _pos, _state, true);
        }
        return null;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state){
        return BlockRenderType.INVISIBLE;
    }
}
