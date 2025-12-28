package net.beholderface.oneironaut.mixin.IotaKeyableMixins;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.idea.IdeaKeyable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec3Iota.class)
public abstract class Vec3IotaKeyableMixin implements IdeaKeyable {
    @Shadow public abstract Vec3d getVec3();

    @Override
    public String getKey() {
        return BlockPos.ofFloored(this.getVec3()).toString();
    }

    @Override
    public boolean isValidKey(CastingEnvironment env) {
        BlockPos pos = BlockPos.ofFloored(this.getVec3());
        WorldBorder border = Oneironaut.getCachedServer().getOverworld().getWorldBorder();
        return pos.getY() < -64 || pos.getY() > 320 || !(border.contains(pos));
    }
}
