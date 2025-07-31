package net.beholderface.oneironaut.fabric.mixin;

import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.DisintegrationProtectionManager;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class DeepNoosphereErosionMixin {

    @Shadow @Final private MinecraftServer server;
    @Unique ServerWorld world = (ServerWorld)(Object) this;
    @Unique private static final TagKey<Block> tagkey = MiscAPIKt.getBlockTagKey(new Identifier("oneironaut:hexbreakimmune"));
    @Unique private static DisintegrationProtectionManager.DisintegrationProtectionEntry latestFoundEntry = null;

    @Inject(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;hasRandomTicks()Z"))
    public void randomDisintegration(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci){
        if (Oneironaut.getDeepNoosphere() != null && world == Oneironaut.getDeepNoosphere()){
            ChunkPos chunkPos = chunk.getPos();
            BlockPos lowerCorner = new BlockPos(chunkPos.getStartX(), world.getDimension().minY(), chunkPos.getStartZ());
            Random random = world.random;
            for (int i = 0; i < Math.floor(((double)randomTickSpeed) / 3); i++){
                BlockPos pos = lowerCorner.add(random.nextInt(16), random.nextInt(world.getDimension().height()), random.nextInt(16));
                BlockState existingState = world.getBlockState(pos);
                if (!(existingState.getHardness(world, pos) == -1 || existingState.isIn(tagkey)
                        || existingState.isAir() || existingState.getBlock() == OneironautBlockRegistry.THOUGHT_SLURRY_BLOCK.get())){
                    DisintegrationProtectionManager.DisintegrationProtectionEntry entry = latestFoundEntry;
                    DisintegrationProtectionManager manager = DisintegrationProtectionManager.getServerState(server);
                    if (entry != null && !entry.canProtect(pos)){
                        entry = manager.getProtectionEntry(Vec3d.of(pos));
                    }
                    if (entry != null){
                        boolean hit = entry.hit(Vec3d.of(pos), world);
                        if (!hit){
                            latestFoundEntry = entry;
                        } else {
                            manager.removeEntry(entry);
                        }
                    } else {
                        world.breakBlock(pos, false);
                    }
                }
            }
        }
    }
}
