package net.beholderface.oneironaut.neo;

import net.beholderface.oneironaut.registry.OneironautBlockRegistry;
import net.beholderface.oneironaut.registry.OneironautMiscRegistry;
import net.beholderface.oneironaut.registry.OneironautTags;
import net.beholderface.oneironaut.casting.OvercastDamageEnchant;
import net.beholderface.oneironaut.status.MediaDisintegrationEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/** Loader-specific gameplay hooks replacing removed 1.20 enchantment classes. */
public final class OneironautGameplayEvents {
    private OneironautGameplayEvents() {
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)
                || !(living.level() instanceof ServerLevel level)
                || !living.onGround()) {
            return;
        }

        Holder<Enchantment> frostWalker = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.FROST_WALKER);
        int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(frostWalker, living);
        if (enchantmentLevel <= 0) {
            return;
        }

        BlockPos center = BlockPos.containing(living.getX(), living.getY() - 1.0, living.getZ());
        float radius = Math.min(16.0F, 2.0F + enchantmentLevel);
        BlockState frozen = OneironautBlockRegistry.MEDIA_ICE_FROSTED.get().defaultBlockState();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-Mth.floor(radius), -1, -Mth.floor(radius)),
                center.offset(Mth.floor(radius), 1, Mth.floor(radius)))) {
            if (pos.distToCenterSqr(living.position()) > radius * radius
                    || !level.isEmptyBlock(pos.above())
                    || !level.getBlockState(pos).is(OneironautBlockRegistry.THOUGHT_SLURRY_BLOCK.get())
                    || !frozen.canSurvive(level, pos)) {
                continue;
            }
            BlockPos immutable = pos.immutable();
            if (level.setBlockAndUpdate(immutable, frozen)) {
                level.scheduleTick(
                        immutable,
                        OneironautBlockRegistry.MEDIA_ICE_FROSTED.get(),
                        Mth.nextInt(level.random, 60, 120)
                );
            }
        }
    }

    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)
                || !(event.getSource().getEntity() instanceof LivingEntity attacker)
                || event.getSource().getDirectEntity() != attacker) {
            return;
        }
        Holder<Enchantment> mindRender = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(OneironautMiscRegistry.OVERCAST_DAMAGE_ENCHANT);
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(
                mindRender,
                attacker.getMainHandItem()
        );
        if (enchantmentLevel > 0) {
            OvercastDamageEnchant.applyMindDamage(
                    attacker,
                    event.getEntity(),
                    enchantmentLevel,
                    event.getEntity().getType().is(OneironautTags.Entities.mindRenderAutospare)
            );
        }
    }

    public static void onMobEffectRemoved(MobEffectEvent.Remove event) {
        handleDisintegrationRemoval(event.getEntity(), event.getEffectInstance(), false);
    }

    public static void onMobEffectExpired(MobEffectEvent.Expired event) {
        handleDisintegrationRemoval(event.getEntity(), event.getEffectInstance(), true);
    }

    private static void handleDisintegrationRemoval(
            LivingEntity entity,
            net.minecraft.world.effect.MobEffectInstance instance,
            boolean expired
    ) {
        if (instance != null
                && instance.is(OneironautMiscRegistry.DISINTEGRATION)
                && OneironautMiscRegistry.DISINTEGRATION.get() instanceof MediaDisintegrationEffect effect) {
            effect.onEffectRemoved(entity, instance, expired);
        }
    }
}
