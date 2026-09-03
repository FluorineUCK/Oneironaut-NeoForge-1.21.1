package net.beholderface.oneironaut.casting.mishaps

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.TreeList
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.core.Holder
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapMissingEnchant(val stack: ItemStack, val enchant: Holder<Enchantment>) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment = dyeColor(DyeColor.PURPLE)

    override fun particleSpray(ctx: CastingEnvironment) =
        ParticleSpray.burst(stack.entityRepresentation?.position()!!, 1.0)

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component =
        error("oneironaut:missingenchant", stack.hoverName, enchant.value().description())

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> {
        if (ctx.castingEntity != null && ctx.castingEntity is ServerPlayer){
            (ctx.castingEntity as ServerPlayer).setExperienceLevels(((ctx.castingEntity as ServerPlayer).experienceLevel - 3).coerceAtLeast(0))
        }
        return stack.appended(GarbageIota())
    }

    companion object {
        @JvmStatic
        fun of(stack: ItemStack, enchant: Holder<Enchantment>): MishapMissingEnchant {
            return MishapMissingEnchant(stack, enchant)
        }
    }

}
