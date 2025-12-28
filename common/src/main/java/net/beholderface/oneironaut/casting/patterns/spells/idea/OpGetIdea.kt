package net.beholderface.oneironaut.casting.patterns.spells.idea

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.beholderface.oneironaut.casting.idea.IdeaEntry
import net.beholderface.oneironaut.casting.idea.IdeaInscriptionManager
import net.beholderface.oneironaut.getIdeaKey
import net.minecraft.entity.Entity
import ram.talia.moreiotas.api.casting.iota.EntityTypeIota

class OpGetIdea : ConstMediaAction {
    override val argc = 1
    override val mediaCost = MediaConstants.DUST_UNIT / 8
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val keyIota = args.getIdeaKey(0, argc, env)
        val entry = IdeaInscriptionManager.getEntry(keyIota, env.world)
        if (entry != null){
            if (entry.type == IdeaEntry.EntryType.IOTA){
                return listOf(entry.payload as Iota)
            } else if (entry.type == IdeaEntry.EntryType.ENTITY){
                return listOf(EntityTypeIota((entry.payload as Entity).type))
            }
        }
        return listOf(GarbageIota())
    }
}