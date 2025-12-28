package net.beholderface.oneironaut.casting.patterns.spells.idea

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.beholderface.oneironaut.casting.idea.IdeaEntry
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.beholderface.oneironaut.casting.idea.IdeaInscriptionManager
import net.beholderface.oneironaut.getSoulprint
import net.beholderface.oneironaut.isPlayerEnlightened
import net.beholderface.oneironaut.casting.iotatypes.SoulprintIota
import net.beholderface.oneironaut.getIdeaKey
import net.beholderface.oneironaut.toVec3i

class OpWriteIdea : ConstMediaAction {
    override val argc = 2
    override val mediaCost = MediaConstants.DUST_UNIT / 4
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val iotaToWrite = args[1]
        val truename = MishapOthersName.getTrueNameFromDatum(iotaToWrite, env.caster)
        if (truename != null){
            throw MishapOthersName(truename)
        }
        val ideaState = IdeaInscriptionManager.getServerState(env.world.server)
        val keyIota = args.getIdeaKey(0, argc, env)
        IdeaInscriptionManager.writeEntry(keyIota, IdeaEntry(iotaToWrite, env.world.time, env.castingEntity))
        /*val rawKeyIota = args[0]
        val keyEntity : Entity
        val keyPos : BlockPos
        if (rawKeyIota.type == EntityIota.TYPE){
            keyEntity = args.getEntity(0, argc)
            env.assertEntityInRange(keyEntity)
            if (keyEntity.type.equals(EntityType.VILLAGER)){
                if (IXplatAbstractions.INSTANCE.isBrainswept(keyEntity as VillagerEntity)){
                    IdeaInscriptionManager.writeEntry(keyEntity.uuid, iotaToWrite, env.caster, env.world)
                } else {
                    throw MishapBadEntity(keyEntity, Text.translatable("oneironaut.mishap.notbrainswept"))
                }
            } else if (keyEntity.isPlayer){
                if (isPlayerEnlightened(keyEntity as ServerPlayerEntity)){
                    IdeaInscriptionManager.writeEntry(keyEntity.uuid, iotaToWrite, env.caster, env.world)
                } else {
                    throw MishapBadEntity(keyEntity, Text.translatable("oneironaut.mishap.unenlightenedtarget"))
                }
            } else {
                throw MishapBadEntity(keyEntity, Text.translatable("oneironaut.mishap.badentitykey"))
            }
        } else if (rawKeyIota.type == Vec3Iota.TYPE){
            keyPos = BlockPos(args.getVec3(0, argc).toVec3i())
            val worldborder = env.world.server.overworld.worldBorder
            if (keyPos.y < -64 || keyPos.y > 320 || !(worldborder.contains(keyPos))){
                throw MishapBadLocation(args.getVec3(0, argc), "out_of_world")
            }
            IdeaInscriptionManager.writeEntry(keyPos, iotaToWrite, env.caster, env.world)
        }else if (rawKeyIota.type == SoulprintIota.TYPE){
            val keySoulprint = args.getSoulprint(0, argc).toString() + "soul"
            IdeaInscriptionManager.writeEntry(keySoulprint, iotaToWrite, env.caster, env.world)
        } else {
            throw MishapInvalidIota(rawKeyIota, 1, Text.translatable("oneironaut.mishap.invalidideakey"));
        }*/
        ideaState.markDirty()
        return listOf()
    }
}