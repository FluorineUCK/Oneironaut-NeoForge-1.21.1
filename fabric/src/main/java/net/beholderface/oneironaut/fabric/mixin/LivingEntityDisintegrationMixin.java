package net.beholderface.oneironaut.fabric.mixin;

import net.beholderface.oneironaut.DeepNoosphereDimensionEffects;
import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.client.world.ClientWorld;
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

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void disintegrate(CallbackInfo ci){
        boolean shouldDisintegrate = false;
        if (entity.getWorld().isClient){
            ClientWorld world = (ClientWorld) entity.getWorld();
            shouldDisintegrate = world.getDimensionEffects().getClass() == DeepNoosphereDimensionEffects.class && world.getTime() % 20 == 0;
        } else {
            ServerWorld world = (ServerWorld) entity.getWorld();
            shouldDisintegrate = world == Oneironaut.getDeepNoosphere() && world.getTime() % 20 == 0;
        }
        if (shouldDisintegrate){
            Oneironaut.processDisintegration(entity);
        }
    }
}
