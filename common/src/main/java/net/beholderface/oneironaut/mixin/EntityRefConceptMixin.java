package net.beholderface.oneironaut.mixin;

import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifier;
import net.beholderface.oneironaut.casting.conceptmodification.ConceptModifierManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(EntityIota.class)
public abstract class EntityRefConceptMixin {
    @Shadow public abstract Entity getEntity();

    @WrapMethod(method = "isTruthy", remap = false)
    public boolean isntTruthy(Operation<Boolean> original){
        if (this.getEntity() instanceof ServerPlayerEntity player){
            ConceptModifierManager manager = ConceptModifierManager.getServerState(Oneironaut.getCachedServer());
            if (manager != null){
                if (manager.hasModifierType(player, ConceptModifier.ModifierType.FALSY_REFERENCE)){
                    return false;
                }
            }
        }
        return original.call();
    }

    @WrapMethod(method = "toleratesOther", remap = false)
    public boolean intolerance(Iota that, Operation<Boolean> original){
        if (this.getEntity() instanceof ServerPlayerEntity player){
            ConceptModifierManager manager = ConceptModifierManager.getServerState(Oneironaut.getCachedServer());
            if (manager != null){
                ConceptModifier modifier = manager.getModifierByType(player, ConceptModifier.ModifierType.REFERENCE_COMPARISON);
                if (modifier != null){
                    return modifier.parameters.getBoolean(ConceptModifier.TAG_COMPARISON_OVERRIDE);
                }
            }
        }
        return original.call(that);
    }
}
