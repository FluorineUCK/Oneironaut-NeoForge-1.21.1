package net.beholderface.oneironaut.fabric.mixin;

import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.DisintegrationProtectionManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityDisintegrationMixin {

    @Unique LivingEntity entity = (LivingEntity) (Object) this;
    @Unique private static boolean hasCaughtError = false;


    @Inject(method = "tick", at = @At(value = "TAIL", remap = false), remap = true)
    public void disintegrate(CallbackInfo ci){
        boolean shouldDisintegrate = false;
        if (entity.getWorld().isClient){
            try {
                shouldDisintegrate = Oneironaut.isWorldDeepNoosphere(entity.getWorld()) && entity.getWorld().getTime() % 20 == 0;
            } catch (NoClassDefFoundError why){
                if (!hasCaughtError){
                    Oneironaut.LOGGER.info("I hear they're adding glorbo to silksong");
                    hasCaughtError = true;
                }
            }
        } else {
            ServerWorld world = (ServerWorld) entity.getWorld();
            shouldDisintegrate = world == Oneironaut.getDeepNoosphere() && world.getTime() % 20 == 0;
        }
        if (shouldDisintegrate){
            DisintegrationProtectionManager.handleDisintegrationTick(entity);
        }
    }
}
