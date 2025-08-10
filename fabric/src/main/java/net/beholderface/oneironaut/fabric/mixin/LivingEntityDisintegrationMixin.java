package net.beholderface.oneironaut.fabric.mixin;

import net.beholderface.oneironaut.DeepNoosphereDimensionEffects;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.DisintegrationProtectionManager;
import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityDisintegrationMixin {

    @Unique LivingEntity entity = (LivingEntity) (Object) this;
    @Unique private static DisintegrationProtectionManager.DisintegrationProtectionEntry latestFoundEntry = null;
    @Unique private static boolean hasCaughtError = false;


    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void disintegrate(CallbackInfo ci){
        boolean shouldDisintegrate = false;
        if (entity.getWorld().isClient){
            try {
                ClientWorld world = (ClientWorld) entity.getWorld();
                shouldDisintegrate = world.getDimensionEffects().getClass() == DeepNoosphereDimensionEffects.class && world.getTime() % 20 == 0;
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
            DisintegrationProtectionManager.DisintegrationProtectionEntry entry = latestFoundEntry;
            Vec3d pos = entity.getEyePos();
            if (!entity.getWorld().isClient && !(entity instanceof PlayerEntity player && (player.isCreative() || player.isSpectator()))){
                DisintegrationProtectionManager manager = DisintegrationProtectionManager.getServerState(((ServerWorld)entity.getWorld()).getServer());
                if (entry == null || !entry.canProtect(pos)){
                    entry = manager.getProtectionEntry(pos);
                }
                if (entry != null && !entry.isBroken()){
                    StatusEffectInstance instance = entity.getStatusEffect(OneironautMiscRegistry.DISINTEGRATION_PROTECTION.get());
                    if (instance != null){
                        if (instance.getDuration() <= 40){
                            instance.duration = 100;
                        }
                    } else {
                        entity.addStatusEffect(new StatusEffectInstance(OneironautMiscRegistry.DISINTEGRATION_PROTECTION.get(), 100, 0, true, true));
                    }
                    boolean hit = entry.hit(2, pos,(ServerWorld) entity.getWorld());
                    if (!hit){
                        latestFoundEntry = entry;
                    } else {
                        latestFoundEntry = null;
                        manager.removeEntry(entry);
                    }
                }
            }
            Oneironaut.processDisintegration(entity);
        }
    }
}
