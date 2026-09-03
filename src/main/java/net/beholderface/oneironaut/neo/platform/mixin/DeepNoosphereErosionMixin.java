package net.beholderface.oneironaut.neo.platform.mixin;

import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.DisintegrationProtectionManager;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.beholderface.oneironaut.registry.OneironautItemRegistry;
import net.beholderface.oneironaut.registry.OneironautTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class DeepNoosphereErosionMixin {

    @Shadow @Final private MinecraftServer server;
    @Unique ServerLevel world = (ServerLevel)(Object) this;
    @Unique private static final TagKey<Block> immunityKey = OneironautTags.Blocks.erosionImmune;
    @Unique private static final TagKey<Block> realityKey = MiscAPIKt.getBlockTagKey(ResourceLocation.parse("oneironaut:candropreality"));
    @Unique private static DisintegrationProtectionManager.DisintegrationProtectionEntry latestFoundEntry = null;

    @Inject(method = "tickChunk", at = @At("HEAD"))
    public void randomDisintegration(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci){
        if (Oneironaut.getDeepNoosphere() != null && world == Oneironaut.getDeepNoosphere()){
            ChunkPos chunkPos = chunk.getPos();
            BlockPos lowerCorner = new BlockPos(chunkPos.getMinBlockX(), world.dimensionType().minY(), chunkPos.getMinBlockZ());
            RandomSource random = world.random;
            for (int i = 0; i < Math.floor(((double)randomTickSpeed) / 3); i++){
                BlockPos pos = lowerCorner.offset(random.nextInt(16), random.nextInt(world.dimensionType().height()), random.nextInt(16));
                BlockState existingState = world.getBlockState(pos);
                if (!(existingState.getDestroySpeed(world, pos) == -1 || existingState.is(immunityKey)
                        || existingState.isAir() || existingState.getBlock() == OneironautBlockRegistry.THOUGHT_SLURRY_BLOCK.get())){
                    DisintegrationProtectionManager.DisintegrationProtectionEntry entry = latestFoundEntry;
                    DisintegrationProtectionManager manager = DisintegrationProtectionManager.getServerState(server);
                    if (entry == null || !entry.canProtect(pos)){
                        entry = manager.getProtectionEntry(Vec3.atLowerCornerOf(pos));
                    }
                    //Oneironaut.LOGGER.info(entry != null ? entry.getUuid() : "no");
                    if (entry != null && !entry.isBroken()){
                        boolean hit = entry.hit(Vec3.atLowerCornerOf(pos), world);
                        if (!hit){
                            latestFoundEntry = entry;
                        } else {
                            latestFoundEntry = null;
                            manager.removeEntry(entry);
                        }
                    } else {
                        world.destroyBlock(pos, false);
                        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        /*if (existingState.is(realityKey) && random.nextLong() % 10 == 0){
                            Vec3d centerPos = pos.toCenterPos();
                            ItemStack stack = new ItemStack(OneironautItemRegistry.REALITY_SHARD.get(), 1);
                            ItemEntity realityShard = new ItemEntity(world, centerPos.getX(), centerPos.getY(), centerPos.getZ(), stack);
                            world.spawnEntity(realityShard);
                        }*/
                    }
                }
            }
        }
    }
}
