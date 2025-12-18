package net.beholderface.oneironaut.fabric.mixin;

import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import kotlin.collections.CollectionsKt;
import net.beholderface.oneironaut.MiscStaticData;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CastingImage.class)
public class MaxStackSizeMixinImage {

    @Unique
    private final CastingImage oneironaut$image = (CastingImage) (Object) this;

    @WrapOperation(method = "serializeToNBT", at = @At(value = "INVOKE",
            target = "Lat/petrak/hexcasting/api/casting/iota/IotaType;isTooLargeToSerialize(Ljava/lang/Iterable;)Z", remap = false), remap = false)
    public boolean isTooLarge(Iterable<Iota> examinee, Operation<Boolean> original){
        if (oneironaut$image.getUserData().getBoolean(MiscStaticData.TAG_ALLOW_SERIALIZE)){
            return false;
        } else {
            return original.call(examinee);
        }
    }

    @WrapOperation(method = "serializeToNbt()Lnet/minecraft/nbt/NbtCompound;", at = @At(value = "INVOKE",
            target = "Lat/petrak/hexcasting/api/utils/HexUtils;serializeToNBT(Ljava/lang/Iterable;)Lnet/minecraft/nbt/NbtElement;"))
    public NbtElement serializePermissive(Iterable<? extends Iota> toSerialize, Operation<NbtElement> original){
        if (oneironaut$image.getUserData().getBoolean(MiscStaticData.TAG_ALLOW_SERIALIZE)){
            return new ListIota(CollectionsKt.toList(toSerialize)).serialize();
        } else {
            return original.call(toSerialize);
        }
    }
}
