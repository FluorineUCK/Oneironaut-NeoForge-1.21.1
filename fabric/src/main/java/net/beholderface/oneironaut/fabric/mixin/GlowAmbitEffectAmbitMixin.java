package net.beholderface.oneironaut.fabric.mixin;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.beholderface.oneironaut.MiscAPIKt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CastingEnvironment.class)
public abstract class GlowAmbitEffectAmbitMixin {

    @Unique
    private final CastingEnvironment oneironaut$ctx = (CastingEnvironment) (Object) this;

    @ModifyReturnValue(method = "isEntityInRange(Lnet/minecraft/entity/Entity;)Z", at = @At(value = "RETURN", remap = false), remap = true)
    public boolean makeSpecialAmbitWork(boolean original, @Local Entity entity){
        if (!original){
            //not sure if the dimension check is strictly necessary, but it can't hurt
            if (oneironaut$ctx.getWorld() == entity.getWorld() && entity instanceof LivingEntity living){
                return MiscAPIKt.hasResonanceEffect(living);
            }
        }
        return original;
    }
}
