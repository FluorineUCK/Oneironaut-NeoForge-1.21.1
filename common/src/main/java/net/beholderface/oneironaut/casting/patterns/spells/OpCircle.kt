package net.beholderface.oneironaut.casting.patterns.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.item.ItemStack
import net.beholderface.oneironaut.registry.OneironautItemRegistry
import net.minecraft.server.network.ServerPlayerEntity
import kotlin.math.min

class OpCircle : SpellAction {
    override val argc = 1
    override fun execute(args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result {
        val quantity = args.getInt(0, argc).coerceIn(1, 64)
        val cost = quantity * MediaConstants.DUST_UNIT
        return SpellAction.Result(Spell(quantity), cost, listOf(ParticleSpray.cloud(ctx.mishapSprayPos(), 2.0)))
    }
    private data class Spell(val quantity : Int) : RenderedSpell {
        override fun cast(env: CastingEnvironment){
            if (env.castingEntity != null && env.castingEntity is ServerPlayerEntity){
                val caster = env.castingEntity as ServerPlayerEntity
                caster.giveItemStack(ItemStack(OneironautItemRegistry.CIRCLE_ITEM.get(), quantity))
            }
        }
    }
}