package net.beholderface.oneironaut.mixin;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.beholderface.oneironaut.OneironautConfig;
import net.beholderface.oneironaut.network.FireballUpdatePacket;
import net.beholderface.oneironaut.registry.OneironautTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * In 1.21 an {@link AbstractHurtingProjectile}'s acceleration follows its velocity vector, so
 * Hex Casting's normal impulse already redirects subsequent acceleration. This hook retains
 * Oneironaut's owner handoff and explicitly synchronises the resulting velocity to clients.
 */
@Mixin(targets = "at.petrak.hexcasting.common.casting.actions.spells.OpAddMotion$Spell")
public abstract class ImpulseRedirectFireballMixin {
    @Final
    @Shadow
    private Entity target;

    @Inject(
            method = "cast(Lat/petrak/hexcasting/api/casting/eval/CastingEnvironment;)V",
            at = @At(value = "RETURN", remap = false),
            remap = false
    )
    private void oneironaut$redirectFireball(CastingEnvironment env, CallbackInfo ci) {
        if (!(target instanceof AbstractHurtingProjectile projectile)
                || !OneironautConfig.getServer().getImpulseRedirectsFireball()) {
            return;
        }

        boolean immune = target.getType().is(OneironautTags.Entities.impulseRedirectBlacklist)
                || projectile instanceof WitherSkull skull && skull.isDangerous();
        if (immune) {
            return;
        }

        projectile.setOwner(env.getCastingEntity());
        Vec3 velocity = projectile.getDeltaMovement();
        IXplatAbstractions.INSTANCE.sendPacketNear(
                projectile.position(),
                128,
                env.getWorld(),
                new FireballUpdatePacket(velocity, projectile)
        );
    }
}
