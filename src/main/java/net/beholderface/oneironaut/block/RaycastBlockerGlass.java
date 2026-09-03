package net.beholderface.oneironaut.block;

import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public class RaycastBlockerGlass extends TransparentBlock {
    public RaycastBlockerGlass(net.minecraft.world.level.block.state.BlockBehaviour.Properties settings) {
        super(settings);
    }

    public boolean isTranslucent(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return world.getMaxLightLevel();
    }
}
