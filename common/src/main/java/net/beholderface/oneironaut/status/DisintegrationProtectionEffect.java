package net.beholderface.oneironaut.status;

import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class DisintegrationProtectionEffect extends StatusEffect {
    public DisintegrationProtectionEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x8d6acc);
    }

    public void applyUpdateEffect(LivingEntity entity, int amplifier){
        entity.removeStatusEffect(OneironautMiscRegistry.DISINTEGRATION.get());
    }

    public boolean canApplyUpdateEffect(int duration, int amplifier){
        return duration % 20 == 0;
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        entity.removeStatusEffect(OneironautMiscRegistry.DISINTEGRATION.get());
    }
}
