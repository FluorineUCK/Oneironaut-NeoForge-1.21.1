package net.beholderface.oneironaut.casting.patterns.spells.great

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getLivingEntityButNotArmorStand
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.beholderface.oneironaut.item.MemoryFragmentItem
import net.beholderface.oneironaut.unbrainsweep
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.AllayEntity
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.village.VillagerDataContainer

class OpReviveFlayed : SpellAction {
    override val argc = 1
    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val patient = args.getLivingEntityButNotArmorStand(0, argc)
        env.assertEntityInRange(patient)
        if (patient is MobEntity && IXplatAbstractions.INSTANCE.isBrainswept(patient)){
            val cost = if (patient is VillagerEntity || patient is AllayEntity) {
                MediaConstants.CRYSTAL_UNIT * 16
            } else {
                MediaConstants.SHARD_UNIT * 10
            }
            return SpellAction.Result(Spell(patient), cost, listOf(ParticleSpray.cloud(patient.pos, 1.0)))
        } else {
            throw MishapBadEntity(patient, Text.translatable("oneironaut.mishap.requiresflayedmob"))
        }
    }

    private data class Spell(val patient : MobEntity) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            patient.unbrainsweep()
            if (patient is VillagerDataContainer && env.castingEntity != null && env.castingEntity is ServerPlayerEntity){
                val tracker = (env.castingEntity as ServerPlayerEntity).advancementTracker
                val loader = env.world.server.advancementLoader
                val recyclingAdvancement = loader.get(Identifier.of("oneironaut", "unflay"))
                if (!tracker.getProgress(recyclingAdvancement).isDone){
                    tracker.grantCriterion(recyclingAdvancement, MemoryFragmentItem.CRITEREON_KEY)
                }
            }
        }
    }

}