package net.beholderface.oneironaut.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.data.client.BlockStateVariantMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class DeepNoosphereErosionMixin {

    @Unique ServerWorld world = (ServerWorld)(Object) this;

    @Inject(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;hasRandomTicks()Z"))
    public void randomDisintegration(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci){
        if (Oneironaut.getDeepNoosphere() != null && world == Oneironaut.getDeepNoosphere()){
            ChunkPos chunkPos = chunk.getPos();
            BlockPos lowerCorner = new BlockPos(chunkPos.getStartX(), world.getDimension().minY(), chunkPos.getStartZ());
            Random random = world.random;
            for (int i = 0; i < Math.floor(((double)randomTickSpeed) / 3); i++){
                BlockPos pos = lowerCorner.add(random.nextInt(16), random.nextInt(world.getDimension().height()), random.nextInt(16));
                if (pos != null){
                    BlockState existingState = world.getBlockState(pos);
                    if (!(existingState.getHardness(world, pos) == -1 || existingState.isIn(MiscAPIKt.getBlockTagKey(new Identifier("oneironaut:hexbreakimmune")))
                            || existingState.isAir() || existingState.getBlock() == OneironautBlockRegistry.THOUGHT_SLURRY_BLOCK.get())){
                        //Oneironaut.LOGGER.info("Disintegrating block {} at position {}", existingState.getBlock().getName().getString(), pos.toShortString());
                        world.breakBlock(pos, false);
                    }
                }
            }
        }
    }
}
