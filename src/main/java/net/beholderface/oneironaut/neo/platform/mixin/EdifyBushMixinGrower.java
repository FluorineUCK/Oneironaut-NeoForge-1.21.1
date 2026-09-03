package net.beholderface.oneironaut.neo.platform.mixin;

import at.petrak.hexcasting.common.misc.AkashicTreeGrower;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AkashicTreeGrower.class, remap = false)
public class EdifyBushMixinGrower {
    @Inject(method = "growTree", at = @At("HEAD"), cancellable = true, remap = false)
    public void produceBush(ServerLevel world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, RandomSource random, CallbackInfoReturnable<Boolean> cir){
        if (state.getBlock() == Blocks.SWEET_BERRY_BUSH){
            world.setBlockAndUpdate(pos, OneironautBlockRegistry.RENDER_BUSH.get().defaultBlockState().setValue(SweetBerryBushBlock.AGE, 1));
            cir.setReturnValue(true);
        }
    }
}
