package net.beholderface.oneironaut.casting;

import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.lib.HexDamageTypes;
import at.petrak.hexcasting.ktxt.AccessorWrappers;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.beholderface.oneironaut.network.ParticleBurstPacket;
import net.beholderface.oneironaut.registry.OneironautTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Runtime behavior for the data-driven Mind Render enchantment.
 *
 * <p>Enchantments became data-pack registry entries in 1.21, so this class is
 * intentionally a behavior helper rather than an Enchantment subclass.</p>
 */
public final class OvercastDamageEnchant {
    private static final Map<LivingEntity, Long> cooldownMap = new HashMap<>();
    private static final FrozenPigment playerlessColor = FrozenPigment.DEFAULT.get();

    private OvercastDamageEnchant() {
    }

    public static void applyMindDamage(
            @Nullable LivingEntity user,
            @NotNull Entity target,
            int level,
            boolean autospare
    ) {
        Level world = target.level();
        long currentTime = world.getGameTime();
        long lastTime = cooldownMap.getOrDefault(user, 0L);
        if (!(target instanceof LivingEntity livingTarget)
                || lastTime + 12 >= currentTime
                || world.isClientSide) {
            return;
        }

        boolean brainswept = target instanceof Mob mob && IXplatAbstractions.INSTANCE.isBrainswept(mob);
        boolean creative = target instanceof Player player && (player.isSpectator() || player.isCreative());
        DamageSource overcastSource = livingTarget.damageSources().source(HexDamageTypes.OVERCAST);
        if (livingTarget.isInvulnerableTo(overcastSource)
                || livingTarget.isDeadOrDying()
                || brainswept
                || creative) {
            return;
        }

        float oldHealth = livingTarget.getHealth();
        float newHealth = oldHealth - level / 2.0F;
        if (newHealth > 0) {
            livingTarget.setHealth(newHealth);
        } else if (autospare && newHealth < oldHealth) {
            livingTarget.setHealth(0.1F);
        } else {
            livingTarget.hurt(overcastSource, Float.MAX_VALUE);
            livingTarget.kill();
        }
        AccessorWrappers.markHurt(livingTarget);

        if (livingTarget.isAlive() && livingTarget.getHealth() <= 1.0F && target instanceof Mob mob) {
            boolean whitelisted = mob.getType().is(OneironautTags.Entities.mindRenderFlayWhitelist);
            boolean blacklisted = mob.getType().is(OneironautTags.Entities.mindRenderFlayBlacklist);
            if ((mob.getMaxHealth() <= 100.0F || whitelisted) && !blacklisted) {
                IXplatAbstractions.INSTANCE.setBrainsweepAddlData(mob);
                FrozenPigment pigment = user instanceof ServerPlayer player
                        ? IXplatAbstractions.INSTANCE.getPigment(player)
                        : playerlessColor;
                IXplatAbstractions.INSTANCE.sendPacketNear(
                        target.position(),
                        128.0,
                        (ServerLevel) mob.level(),
                        new ParticleBurstPacket(
                                target.position(),
                                new Vec3(0.0, 0.1, 0.0),
                                0.1,
                                0.025,
                                pigment,
                                64,
                                false
                        )
                );
                world.playSound(
                        null,
                        mob,
                        SoundEvents.ELDER_GUARDIAN_CURSE,
                        user instanceof ServerPlayer ? SoundSource.PLAYERS : SoundSource.BLOCKS,
                        user instanceof ServerPlayer ? 1.0F : 0.5F,
                        1.0F
                );
            }
        }

        if (user != null) {
            cooldownMap.put(user, currentTime);
        }
    }
}
