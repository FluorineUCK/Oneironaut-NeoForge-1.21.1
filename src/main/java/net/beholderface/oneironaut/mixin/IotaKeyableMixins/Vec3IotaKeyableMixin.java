package net.beholderface.oneironaut.mixin.IotaKeyableMixins;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.idea.IdeaKeyable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec3Iota.class)
public abstract class Vec3IotaKeyableMixin implements IdeaKeyable {
    @Shadow public abstract Vec3 getVec3();

    @Override
    public String getKey() {
        return BlockPos.containing(this.getVec3()).toString();
    }

    @Override
    public boolean isValidKey(CastingEnvironment env) {
        Oneironaut.LOGGER.info("???");
        BlockPos pos = BlockPos.containing(this.getVec3());
        WorldBorder border = Oneironaut.getCachedServer().overworld().getWorldBorder();
        return pos.getY() >= -64 && pos.getY() <= 320 && border.isWithinBounds(pos);
    }
}
