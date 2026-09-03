package net.beholderface.oneironaut.status;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.beholderface.oneironaut.registry.OneironautMiscRegistry;

public class GlowingAmbitEffect extends MobEffect {
    //this class is (mostly) only necessary because StatusEffect's constructor method is protected for some reason, the actual functionality of the effect is in a couple mixins
    //the glow is in GlowAmbitEffectGlowMixin, and the ambit is in GlowAmbitEffectAmbitMixin
    public GlowingAmbitEffect() {
        super(MobEffectCategory.NEUTRAL, 0x7355ff);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier){
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier){
        long time = entity.level().getGameTime();
        RandomSource rand = entity.level().random;
        if (!(entity.level().isClientSide) && !(entity.hasEffect(OneironautMiscRegistry.DETECTION_RESISTANCE))){
            final int dissonance = Math.min(amplifier, 5);
            final int defaultInterval = 20;
            int thisInterval = (int) Math.min(Math.floor(Math.abs(rand.nextGaussian() - 0.5) * 6 * dissonance), defaultInterval - 1);
            if (time % (defaultInterval - thisInterval) == 0) {
                ((ServerLevel) entity.level()).playSound(
                        null, entity, SoundEvents.AMETHYST_BLOCK_CHIME, entity.getSoundSource(),
                        (float)(2 + (((rand.nextGaussian() - 0.5) * dissonance) / 3)), (1 - (rand.nextFloat() * (dissonance / 3f))));
            }
        }
        return true;
    }
}
