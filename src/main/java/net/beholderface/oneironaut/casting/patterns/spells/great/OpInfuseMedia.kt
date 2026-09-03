package net.beholderface.oneironaut.casting.patterns.spells.great

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.core.BlockPos
import net.beholderface.oneironaut.casting.mishaps.MishapUninfusable
import net.beholderface.oneironaut.getInfuseResult
import net.beholderface.oneironaut.toVec3i

class OpInfuseMedia : SpellAction {
    override val argc = 1
    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val target = args.getVec3(0, argc).toVec3i()
        val targetv3d = args.getVec3(0, argc)
        env.assertVecInRange(targetv3d)
        val targetType = env.world.getBlockState(BlockPos(target))
        val (result, cost, advancement) = getInfuseResult(targetType, env.world)
        if (result == Blocks.BARRIER.defaultBlockState()){
            throw MishapUninfusable.of(BlockPos(target)/*, "media"*/)
        }
        return SpellAction.Result(
            Spell(BlockPos(target), result, cost, advancement),
            cost,
            listOf(ParticleSpray.cloud(targetv3d, 2.0))
        )
    }
    private data class Spell(val target: BlockPos, var result: BlockState, val cost: Long, val advancement : String?) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            env.world.setBlockAndUpdate(target, result)
        }
    }
}