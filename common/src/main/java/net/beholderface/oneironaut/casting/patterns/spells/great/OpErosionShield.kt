package net.beholderface.oneironaut.casting.patterns.spells.great

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.api.misc.MediaConstants
import net.beholderface.oneironaut.Oneironaut
import net.beholderface.oneironaut.casting.DisintegrationProtectionManager
import net.beholderface.oneironaut.casting.DisintegrationProtectionManager.DisintegrationProtectionEntry
import net.beholderface.oneironaut.corners
import net.beholderface.oneironaut.volume
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import ram.talia.hexal.api.getStrictlyPositiveLong

class OpErosionShield : SpellAction {
    override val argc = 3
    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        if (env !is CircleCastEnv){
            throw MishapNoSpellCircle()
        }
        val cornerA = args.getBlockPos(0, argc)
        val cornerB = args.getBlockPos(1, argc)
        val box = Box(cornerA, cornerB)
        val particlePositions : MutableList<ParticleSpray> = mutableListOf()
        for (corner in box.corners()){
            env.assertVecInRange(corner)
            particlePositions.add(ParticleSpray.cloud(corner, 2.0))
        }
        val durability = args.getStrictlyPositiveLong(2, argc)
        val cost = (box.volume() * 0.1) * (durability * 0.01) * MediaConstants.DUST_UNIT
        return SpellAction.Result(
            Spell(cornerA, cornerB, durability),
            cost.toLong(),
            particlePositions
        )
    }

    private data class Spell(val cornerA : BlockPos, val cornerB : BlockPos, val durability : Long) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            //it's not going to mishap if you use it in the overworld, but it's not going to do anything else either
            if (env.world != null && env.world == Oneironaut.getDeepNoosphere()){
                val entry = DisintegrationProtectionEntry(cornerA, cornerB, durability)
                DisintegrationProtectionManager.getServerState(env.world.server).addEntry(entry)
            }
        }
    }
}