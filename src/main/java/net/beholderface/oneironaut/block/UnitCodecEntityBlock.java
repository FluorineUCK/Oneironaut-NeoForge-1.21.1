package net.beholderface.oneironaut.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Registered addon blocks are singleton instances, so a unit codec is enough
 * to satisfy the 1.21 block codec contract without changing their constructors.
 */
public abstract class UnitCodecEntityBlock extends BaseEntityBlock {
    protected UnitCodecEntityBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }
}
