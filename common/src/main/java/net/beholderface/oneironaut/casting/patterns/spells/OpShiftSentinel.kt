package net.beholderface.oneironaut.casting.patterns.spells

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.player.Sentinel
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.beholderface.oneironaut.casting.mishaps.MishapNoSentinel
import net.beholderface.oneironaut.getDimIota
import net.minecraft.util.math.Vec3d

class OpShiftSentinel : ConstMediaAction {
    override val argc = 1
    override val mediaCost = MediaConstants.SHARD_UNIT
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        if (env.caster == null){
            throw MishapBadCaster()
        }
        val destDim = args.getDimIota(0, argc).toWorld(env.world.server)
        val worldKey = destDim.registryKey
        if (!HexConfig.server().canTeleportInThisDimension(worldKey)){
            throw MishapLocationInWrongDimension(worldKey.value)
        }
        val sentinel = IXplatAbstractions.INSTANCE.getSentinel(env.caster) ?: throw MishapNoSentinel() //placeholder
        val originDim = env.world.server.getWorld(sentinel.dimension)
        if (originDim == destDim){
            return listOf()
        }
        val compressionFactor = originDim!!.dimension.coordinateScale / destDim.dimension.coordinateScale
        val originalPos = sentinel.position
        val x = (originalPos.x * compressionFactor).coerceIn(destDim.worldBorder.boundWest, destDim.worldBorder.boundEast)
        val z = (originalPos.z * compressionFactor).coerceIn(destDim.worldBorder.boundNorth, destDim.worldBorder.boundSouth)
        val y = originalPos.y.coerceIn(destDim.dimension.minY.toDouble(), (destDim.dimension.minY + destDim.dimension.height).toDouble())
        val newPos = Vec3d(x, y, z)
        IXplatAbstractions.INSTANCE.setSentinel(env.caster, Sentinel(sentinel.extendsRange, newPos, destDim.registryKey))
        return listOf()
    }
}