package net.beholderface.oneironaut.casting.patterns.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.beholderface.oneironaut.corners
import net.beholderface.oneironaut.getPositionsInCuboid
import net.beholderface.oneironaut.registry.OneironautBlockRegistry
import net.beholderface.oneironaut.toVec3i

class OpAdvanceAutomaton : SpellAction {
    override val argc = 2
    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val box = Box(BlockPos(args.getVec3(0, argc).toVec3i()), BlockPos(args.getVec3(1, argc).toVec3i()))
        val corners = box.corners()
        for(c in corners){
            env.assertVecInRange(c)
        }
        val cost = (box.xLength * box.yLength * box.zLength * (MediaConstants.DUST_UNIT * 0.1)).toLong()
        return SpellAction.Result(
                Spell(box, null, args, false),
                cost,
                listOf(ParticleSpray.cloud(box.center, 2.0))
            )
    }
    private data class Spell(val box : Box, val corner : BlockPos?,
                             val args : List<Iota>?, val execute : Boolean) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            advanceAutomaton(env, box)
        }
    }
}
fun advanceAutomaton(ctx: CastingEnvironment, box : Box){
    val world = ctx.world
    val corner1 = BlockPos(box.minX.toInt(), box.minY.toInt(), box.minZ.toInt())
    val corner2 = BlockPos(box.maxX.toInt(), box.maxY.toInt(), box.maxZ.toInt())
    val positions = getPositionsInCuboid(corner1, corner2)
    val cellsToKill : MutableList<BlockPos> = mutableListOf()
    val cellsToSpawn : MutableList<BlockPos> = mutableListOf()
    val cellsToVerify : MutableList<BlockPos> = mutableListOf()
    for(pos in positions){
        val isCell = world.getBlockState(pos).block.equals(OneironautBlockRegistry.CELL.get())
        val isAir = world.getBlockState(pos).isAir
        val neighbors = getPositionsInCuboid(pos.add(1, 1, 1), pos.add(-1, -1, -1), pos)
        var foundCells = 0
        for (neighbor in neighbors){
            if (world.getBlockState(neighbor).block.equals(OneironautBlockRegistry.CELL.get())){
                foundCells++
            }
        }
        if (isCell){
            if (foundCells in 5..7){
                //cell survives, mark it as having done so
                cellsToVerify.add(pos)
            } else {
                //mark cell as submissive and killable
                cellsToKill.add(pos)
            }
        } else if (isAir && foundCells == 6){
            //mark position as free real estate
            cellsToSpawn.add(pos)
            cellsToVerify.add(pos)
        }
    }
    for (pos in cellsToKill){
        world.setBlockState(pos, Blocks.AIR.defaultState)
    }
    for (pos in cellsToSpawn){
        world.setBlockState(pos, OneironautBlockRegistry.CELL.get().defaultState)
    }
    for (pos in cellsToVerify){
        val mayBE = world.getBlockEntity(pos, OneironautBlockRegistry.CELL_ENTITY.get())
        if (mayBE.isPresent){
            if (!mayBE.get().verified){
                mayBE.get().verified = true
            }
        }
    }
}