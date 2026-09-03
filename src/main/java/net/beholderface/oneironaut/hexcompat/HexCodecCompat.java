package net.beholderface.oneironaut.hexcompat;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

/** Small public-API adapter for Hex Casting's 1.21 codec-based iota storage. */
public final class HexCodecCompat {
    private HexCodecCompat() {
    }

    public static Tag encode(Iota iota) {
        return IotaType.TYPED_CODEC.encodeStart(NbtOps.INSTANCE, iota).getOrThrow();
    }

    @Nullable
    public static Iota decode(@Nullable Tag tag) {
        if (tag == null) {
            return null;
        }
        DataResult<Iota> result = IotaType.TYPED_CODEC.parse(NbtOps.INSTANCE, tag);
        return result.result().orElse(null);
    }
}
