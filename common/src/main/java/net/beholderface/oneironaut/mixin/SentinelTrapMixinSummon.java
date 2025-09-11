package net.beholderface.oneironaut.mixin;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.beholderface.oneironaut.block.blockentity.SentinelTrapImpetusEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
@Mixin(targets = "at.petrak.hexcasting.common.casting.actions.spells.sentinel.OpCreateSentinel$Spell")
public abstract class SentinelTrapMixinSummon {
    @Final
    @Shadow private Vec3d target;

    @Unique
    private final Map<RegistryKey<World>, Map<BlockPos, Vec3d>> oneironaut$trapMap = SentinelTrapImpetusEntity.trapLocationMap;

    @Inject(method = "cast(Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)V", at = @At("HEAD"), remap = false)
    public void triggerTrap(CastingEnvironment env, CallbackInfo ci){
        if (env.getCastingEntity() == null){
            return;
        }
        World world = env.getWorld();
        RegistryKey<World> worldKey = world.getRegistryKey();
        if (oneironaut$trapMap.containsKey(worldKey)){
            Map<BlockPos, Vec3d> trapPosMap = oneironaut$trapMap.get(worldKey);
            Iterator<Map.Entry<BlockPos, Vec3d>> entryIterator = trapPosMap.entrySet().iterator();
            Map.Entry<BlockPos, Vec3d> currentEntry;
            List<BlockPos> expiredKeys = new ArrayList<>();
            while(entryIterator.hasNext()){
                currentEntry = entryIterator.next();
                if (target.isInRange(currentEntry.getValue(), 8.0)){
                    BlockPos pos = currentEntry.getKey();
                    if (world.getBlockState(pos).getBlock() == OneironautBlockRegistry.SENTINEL_TRAP.get()){
                        SentinelTrapImpetusEntity be = (SentinelTrapImpetusEntity) world.getBlockEntity(pos);
                        ServerPlayerEntity foundPlayer = null;
                        if (be.getStoredPlayer() != null){
                            foundPlayer = (ServerPlayerEntity) world.getPlayerByUuid(be.getStoredPlayer().getUuid());
                        }
                        be.setTargetPlayer(env.getCastingEntity().getUuid());
                        be.startExecution(foundPlayer);
                    } else {
                        expiredKeys.add(currentEntry.getKey());
                    }
                }
            }
            for (BlockPos key : expiredKeys){
                trapPosMap.remove(key);
            }
        }
    }
}