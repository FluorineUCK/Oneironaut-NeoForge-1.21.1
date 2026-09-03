package net.beholderface.oneironaut.neo.platform.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.beholderface.oneironaut.MiscAPIKt;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class GlowAmbitEffectGlowMixin {

    @Unique
    private final LivingEntity oneironaut$entity = (LivingEntity) (Object) this;

    @ModifyReturnValue(method = "isCurrentlyGlowing()Z", at = @At(value = "RETURN", remap = true), remap = true)
    public boolean makeSpecialGlowingWork(boolean original){
        if (!original){
            if (MiscAPIKt.hasResonanceEffect(oneironaut$entity)){
                return true;
            }
        }
        return original;
    }
}
