package net.beholderface.oneironaut.neo.platform.mixin;

import at.petrak.hexcasting.common.items.ItemStaff;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.beholderface.oneironaut.item.GeneralNoisyStaff;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.beholderface.oneironaut.item.GeneralNoisyStaff;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;


@SuppressWarnings("ConstantConditions")
@Mixin(value = ItemStaff.class)
public abstract class OneResetSoundMixin {

    @WrapOperation(method = "use", at = @At(value="INVOKE", target = "Lnet/minecraft/world/entity/player/Player;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", remap = true), remap = true)
    private void dontSoundIfNoisyStaff(Player instance, SoundEvent sound, float volume, float pitch, Operation<Void> original, @Local Player player, @Local InteractionHand hand){
        if (!(player.getItemInHand(hand).getItem() instanceof GeneralNoisyStaff)){
            original.call(instance, sound, volume, pitch);
        }
    }
}
