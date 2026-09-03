package net.beholderface.oneironaut.status;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.lib.HexDamageTypes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.beholderface.oneironaut.casting.environments.ForcedMediaCostEnv;
import net.beholderface.oneironaut.item.BottomlessMediaItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;

public class DetectionResistEffect extends MobEffect {
    public DetectionResistEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xcfa0f3);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier){
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier){
        long time = entity.level().getGameTime();
        if (!(entity.level().isClientSide)){
            ItemStack mainStack = entity.getMainHandItem();
            ItemStack offStack = entity.getOffhandItem();
            if ((time % 5) == 0){
                ((ServerLevel) entity.level()).playSound(
                        null, entity, SoundEvents.AMETHYST_BLOCK_CHIME, entity.getSoundSource(), 1.5f, 1f);
            }
            if (amplifier < 100){ //lets people like modpack devs and server admins give things the effect without hurting them
                if (entity instanceof ServerPlayer player){
                    if ((time % 20) == 0){
                        CastingEnvironment env = new ForcedMediaCostEnv(player, InteractionHand.MAIN_HAND);
                        long deficit = env.extractMedia(MediaConstants.DUST_UNIT / 10, false);
                        if (deficit > 0 && (time % 40) == 0){
                            Mishap.Companion.trulyHurt(entity, entity.damageSources().source(HexDamageTypes.OVERCAST), 1f);
                        }
                    }
                } else if (entity instanceof Mob){
                    if (mainStack.getItem() instanceof BottomlessMediaItem || offStack.getItem() instanceof BottomlessMediaItem || IXplatAbstractions.INSTANCE.isBrainswept((Mob) entity)){
                        //do nothing, they are immune
                    } else if ((time % 40) == 0) {
                        Mishap.Companion.trulyHurt(entity, entity.damageSources().source(HexDamageTypes.OVERCAST), 1f);
                    }
                } else if ((time % 40) == 0){
                    Mishap.Companion.trulyHurt(entity, entity.damageSources().source(HexDamageTypes.OVERCAST), 1f);
                }
            }
        }
        return true;
    }
}
