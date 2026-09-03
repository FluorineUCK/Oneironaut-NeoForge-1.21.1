package net.beholderface.oneironaut.status;

import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.OvercastDamageEnchant;
import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class MediaDisintegrationEffect extends MobEffect {
    public static final ResourceLocation ATTRIBUTE_ID = Oneironaut.id("disintegration");

    public MediaDisintegrationEffect() {
        super(MobEffectCategory.HARMFUL, 0x8f6b94);
        addAttributeModifier(Attributes.MAX_HEALTH, ATTRIBUTE_ID, -1.0,
                AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 30 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return true;
        }

        // Preserve the upstream damage/heal pulse so health state is recalculated immediately.
        entity.hurt(entity.damageSources().genericKill(), 0.00001f);
        entity.heal(0.00001f);

        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return true;
        }

        AttributeModifier current = maxHealth.getModifier(ATTRIBUTE_ID);
        if (current == null) {
            return true;
        }

        double oldValue = maxHealth.getValue();
        double oldModifierValue = Math.abs(current.amount());
        double newModifierValue = oldModifierValue + amplifier + 1;
        maxHealth.removeModifier(ATTRIBUTE_ID);
        maxHealth.addPermanentModifier(new AttributeModifier(
                ATTRIBUTE_ID,
                -newModifierValue,
                AttributeModifier.Operation.ADD_VALUE
        ));

        double newValue = maxHealth.getValue();
        if (newValue != oldValue - (newModifierValue - oldModifierValue)) {
            OvercastDamageEnchant.applyMindDamage(null, entity, 9001, false);
        } else if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
        return true;
    }

    /** Called from NeoForge's effect-removal events. */
    public void onEffectRemoved(LivingEntity entity, MobEffectInstance removed, boolean expired) {
        if (!expired
                && removed.getDuration() > 0
                && Oneironaut.isServerThread()
                && entity.level() == Oneironaut.getDeepNoosphere()
                && !entity.hasEffect(OneironautMiscRegistry.DISINTEGRATION_PROTECTION)) {
            Oneironaut.reapplicationSet.add(new Tuple<>(entity, new MobEffectInstance(
                    OneironautMiscRegistry.DISINTEGRATION,
                    100,
                    Math.min(removed.getAmplifier() + 1, Byte.MAX_VALUE),
                    true,
                    true
            )));
        }
    }
}
