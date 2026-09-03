package net.beholderface.oneironaut.neo.platform.mixin;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifier;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifierManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "at/petrak/hexcasting/common/casting/actions/spells/great/OpTeleport$Spell")
public class GTPDropReductionMixin {
    @WrapOperation(method = "cast(Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;length()D", remap = true), remap = false)
    public double reducedLength(Vec3 instance, Operation<Double> original, @Local(argsOnly = true) CastingEnvironment env){
        if (env.getCastingEntity() instanceof ServerPlayer player){
            ConceptModifierManager manager = ConceptModifierManager.getServerState(player.server);
            ConceptModifier modifier = manager.getModifierByType(player, ConceptModifier.ModifierType.GTP_DROPREDUCTION);
            if (modifier != null){
                return Math.max(0.0, original.call(instance) - modifier.parameters.getDouble(ConceptModifier.TAG_POTENCY));
            }
        }
        return original.call(instance);
    }
}
