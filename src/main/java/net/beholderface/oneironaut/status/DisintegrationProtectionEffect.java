package net.beholderface.oneironaut.status;

import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class DisintegrationProtectionEffect extends MobEffect {
    public DisintegrationProtectionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8d6acc);
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier){
        entity.removeEffect(OneironautMiscRegistry.DISINTEGRATION);
        return true;
    }

    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier){
        return duration % 20 == 0;
    }

    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        super.onEffectAdded(entity, amplifier);
        entity.removeEffect(OneironautMiscRegistry.DISINTEGRATION);
    }
}
