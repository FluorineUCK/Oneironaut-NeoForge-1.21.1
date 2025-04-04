package net.beholderface.oneironaut.casting.patterns

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.beholderface.oneironaut.casting.environments.ExtradimensionalCastEnv
import net.beholderface.oneironaut.casting.iotatypes.DimIota
import net.beholderface.oneironaut.casting.mishaps.MishapExtradimensionalFail

class OpEvalExtradimensional : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation
    ): OperationResult {
        if (env !is PlayerBasedCastEnv){
            throw MishapBadCaster()
        }
        val stack = image.stack.toMutableList()
        val toExecute = stack.removeLastOrNull() ?: throw MishapNotEnoughArgs(2, 0)
        val dimension = stack.removeLastOrNull() ?: throw MishapNotEnoughArgs(2, 1)
        val server = env.world.server
        if (dimension !is DimIota){
            throw MishapInvalidIota.ofType(dimension, 1, "oneironaut:imprint")
        } else if (dimension.toWorld(server) == env.world){
            throw MishapInvalidIota.ofType(dimension, 1, "oneironaut:differentimprint")
        }
        val newEnv = if (env is ExtradimensionalCastEnv) {
            ExtradimensionalCastEnv(env.caster, env, dimension.toWorld(server), env.vm)
        } else {
            ExtradimensionalCastEnv(env.caster, env, dimension.toWorld(server))
        }
        return exec(newEnv, image.copy(stack = stack), continuation, toExecute)
    }

    fun exec(env: ExtradimensionalCastEnv, image: CastingImage, continuation: SpellContinuation, instrs: Iota): OperationResult {
        val toExecute = if (instrs is ListIota){
            instrs.list.toList()
        } else {
            listOf(instrs)
        }
        val subHarness = env.vm
        subHarness.image = image
        val executionResult = subHarness.queueExecuteAndWrapIotas(toExecute, env.world)
        if (!executionResult.resolutionType.success){
            throw MishapExtradimensionalFail()
        }
        return OperationResult(subHarness.image.withUsedOp(), listOf(), continuation, HexEvalSounds.HERMES)
    }
}