package net.beholderface.oneironaut.status;

import net.beholderface.oneironaut.DeepNoosphereDimensionEffects;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.OvercastDamageEnchant;
import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MediaDisintegrationEffect extends StatusEffect {
    public MediaDisintegrationEffect() {
        super(StatusEffectCategory.HARMFUL, 0x8f6b94);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier){
        return duration % 30 == 0;
    }

    public final Map<LivingEntity, UUID> modifierData = new HashMap<>();

    public static final String TAG_MODIFIER_NAME = "oneironaut:disintegration";
    public static final String ATTRIBUTE_UUID_STRING = "99c9e34e-bb82-419c-82ce-7751cea942a0";
    public static final UUID ATTRIBUTE_UUID = UUID.fromString(ATTRIBUTE_UUID_STRING);
    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier){
        if (entity instanceof PlayerEntity player && (player.isCreative() || player.isSpectator())){
            return;
        }
        entity.damage(entity.getDamageSources().genericKill(), 0.00001f);
        entity.heal(0.00001f);
        EntityAttributeInstance instance = entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        //I stole most of this from Spectrum's life drain effect
        if (instance != null) {
            EntityAttributeModifier currentMod = instance.getModifier(ATTRIBUTE_UUID);
            if (currentMod != null) {
                double oldValue = instance.getValue();
                instance.removeModifier(currentMod);
                double oldModValue = Math.abs(currentMod.getValue());
                double newModValue = oldModValue + (amplifier + 1);
                EntityAttributeModifier newModifier = new EntityAttributeModifier(ATTRIBUTE_UUID,
                        this::getTranslationKey, -newModValue, EntityAttributeModifier.Operation.ADDITION);
                instance.addPersistentModifier(newModifier);
                double newValue = instance.getValue();
                if (newValue != oldValue - (newModValue - oldModValue) /*if only max health could go negative, this check wouldn't be necessary*/){
                    OvercastDamageEnchant.applyMindDamage(null, entity, 9001, false);
                } else if (entity.getHealth() > entity.getMaxHealth()) {
                    entity.setHealth(entity.getMaxHealth());
                }
            }
        }
    }

    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier){
        super.onRemoved(entity, attributes, amplifier);
        if (Oneironaut.getCachedServer() != null){
            if (entity.getWorld() == Oneironaut.getDeepNoosphere() && !entity.hasStatusEffect(OneironautMiscRegistry.DISINTEGRATION_PROTECTION.get())){
                if (entity.getStatusEffect(this) != null && entity.getStatusEffect(this).duration > 0){
                    Oneironaut.reapplicationSet.add(new Pair<>(entity, new StatusEffectInstance(this, 100, Math.min(amplifier + 1, Byte.MAX_VALUE)/*you've made it mad*/)));
                }
            }
        }
    }
}
