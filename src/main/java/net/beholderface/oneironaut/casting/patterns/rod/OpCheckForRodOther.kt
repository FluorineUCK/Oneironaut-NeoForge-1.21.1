package net.beholderface.oneironaut.casting.patterns.rod

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.beholderface.oneironaut.item.ReverberationRod

class OpCheckForRodOther : ConstMediaAction {
    override val argc = 0
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val state = ReverberationRod.getEnv(env.castingEntity)
        return state?.currentlyCasting?.asActionResult ?: false.asActionResult
    }
}