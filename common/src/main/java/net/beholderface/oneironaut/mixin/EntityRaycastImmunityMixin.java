package net.beholderface.oneironaut.mixin;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.common.casting.actions.raycast.OpEntityRaycast;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import net.beholderface.oneironaut.registry.OneironautTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.MiscAPIKt;
import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = OpEntityRaycast.class)
public abstract class EntityRaycastImmunityMixin {
    @ModifyVariable(method = "execute", at = @At(value = "STORE", remap = false), remap = false)
    private EntityHitResult nullIfBlocker(
            EntityHitResult value,
            @Local CastingEnvironment env,
            @Local(ordinal = 0) Vec3d origin,
            @Local(ordinal = 1) Vec3d look,
            @Local(ordinal = 2) Vec3d end){
        if (value != null){
            int stepResolution = 64;
            Vec3d step = look.multiply(1.0 / stepResolution);
            for(int i = 0; i < origin.distanceTo(value.getPos()) * stepResolution; i++){
                if (env.getWorld().getBlockState(new BlockPos(MiscAPIKt.toVec3i(origin.add(step.multiply(i))))).isIn(OneironautTags.Blocks.blocksRaycast)){
                    return null;
                }
            }
        }
        return value;
    }

    //I have no idea how to work with the actual predicate system so I just made it edit the thing that the pattern uses as an argument for the raycast method
    @ModifyReturnValue(method = "execute$lambda$0", at = @At(value = "RETURN", remap = false), remap = false)
    private static boolean skipImmune(boolean original, @Local Entity it){
        if (it instanceof LivingEntity living){
            return !living.hasStatusEffect(OneironautMiscRegistry.DETECTION_RESISTANCE.get());
        }
        return true;
    }
}
