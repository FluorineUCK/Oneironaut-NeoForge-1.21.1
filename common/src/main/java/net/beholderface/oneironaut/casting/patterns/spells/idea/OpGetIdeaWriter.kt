package net.beholderface.oneironaut.casting.patterns.spells.idea

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.getPlayer
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.beholderface.oneironaut.casting.idea.IdeaInscriptionManager
import net.beholderface.oneironaut.getSoulprint
import net.beholderface.oneironaut.registry.OneironautIotaTypeRegistry
import net.beholderface.oneironaut.casting.iotatypes.SoulprintIota
import net.beholderface.oneironaut.getIdeaKey
import net.beholderface.oneironaut.toVec3i

class OpGetIdeaWriter : ConstMediaAction {
    override val argc = 2
    override val mediaCost = 0L
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        //val rawKeyIota = args[0]
        val keyIota = args.getIdeaKey(0, argc, env)
        val suspect = if (args[1].type == OneironautIotaTypeRegistry.UUID){
            args.getSoulprint(1, argc)
        } else {
            args.getPlayer(1, argc).uuid
        }
        val uuid = IdeaInscriptionManager.getEntryWriter(keyIota, env.world)
        return suspect.equals(uuid).asActionResult
        /*val keyEntity : Entity
        val keyPos : BlockPos
        if (rawKeyIota.type == EntityIota.TYPE){
            keyEntity = args.getEntity(0, argc)
            env.assertEntityInRange(keyEntity)
            if (keyEntity.isPlayer || keyEntity.type.equals(EntityType.VILLAGER)){
                output = IdeaInscriptionManager.getEntryWriter(keyEntity.uuid, suspect, env.world)
            } else {
                throw MishapBadEntity(keyEntity, Text.translatable("oneironaut.mishap.badentitykey"))
            }
        } else if (rawKeyIota.type == Vec3Iota.TYPE){
            keyPos = BlockPos(args.getVec3(0, argc).toVec3i())
            output = IdeaInscriptionManager.getEntryWriter(keyPos, suspect, env.world)
        } else if (rawKeyIota.type == SoulprintIota.TYPE){
            val keySoulprint = args.getSoulprint(0, argc).toString() + "soul"
            output = IdeaInscriptionManager.getEntryWriter(keySoulprint, suspect, env.world)
        } else {
            throw MishapInvalidIota(rawKeyIota, 0, Text.translatable("oneironaut.mishap.invalidideakey"));
        }*/
    }
}