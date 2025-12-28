package net.beholderface.oneironaut.casting.patterns.spells.idea

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getPlayer
import at.petrak.hexcasting.api.casting.iota.Iota
import net.beholderface.oneironaut.casting.idea.IdeaInscriptionManager
import net.beholderface.oneironaut.getIdeaKey
import net.beholderface.oneironaut.getSoulprint
import net.beholderface.oneironaut.registry.OneironautIotaTypeRegistry

class OpGetIdeaWriter : ConstMediaAction {
    override val argc = 2
    override val mediaCost = 0L
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val keyIota = args.getIdeaKey(0, argc, env)
        val suspect = if (args[1].type == OneironautIotaTypeRegistry.UUID){
            args.getSoulprint(1, argc)
        } else {
            args.getPlayer(1, argc).uuid
        }
        val uuid = IdeaInscriptionManager.getEntryWriter(keyIota, env.world)
        return suspect.equals(uuid).asActionResult
    }
}