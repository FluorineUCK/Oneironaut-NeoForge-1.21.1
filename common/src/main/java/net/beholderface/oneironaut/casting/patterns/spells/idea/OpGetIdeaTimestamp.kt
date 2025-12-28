package net.beholderface.oneironaut.casting.patterns.spells.idea

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import net.beholderface.oneironaut.casting.idea.IdeaInscriptionManager
import net.beholderface.oneironaut.getIdeaKey

class OpGetIdeaTimestamp : ConstMediaAction {
    override val argc = 1
    override val mediaCost = 0L
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        var output : Double = -1.0
        val keyIota = args.getIdeaKey(0, argc, env)
        output = IdeaInscriptionManager.getEntryTimestamp(keyIota, env.world)
        return listOf(DoubleIota(output))
    }
}