package net.beholderface.oneironaut.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.beholderface.oneironaut.MiscAPIKt;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class GlowAmbitEffectGlowMixin {

    @Unique
    private final LivingEntity oneironaut$entity = (LivingEntity) (Object) this;

    @Shadow public abstract boolean hasStatusEffect(StatusEffect effect);

    @ModifyReturnValue(method = "isGlowing()Z", at = @At(value = "RETURN", remap = true), remap = true)
    public boolean makeSpecialGlowingWork(boolean original){
        if (!original){
            if (MiscAPIKt.hasResonanceEffect(oneironaut$entity)){
                return true;
            }
        }
        return original;
    }
}
