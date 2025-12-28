package net.beholderface.oneironaut.casting.patterns.spells.idea

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.misc.MediaConstants
import net.beholderface.oneironaut.casting.idea.IdeaEntry
import net.beholderface.oneironaut.casting.idea.IdeaInscriptionManager
import net.beholderface.oneironaut.casting.idea.IdeaKeyable
import net.beholderface.oneironaut.getIdeaKey
import net.beholderface.oneironaut.registry.OneironautTags
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

class OpStoreEntity() : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val key = args.getIdeaKey(0, argc, env)
        val entity = args.getEntity(1, argc)
        env.assertEntityInRange(entity)
        if (entity is PlayerEntity || entity.type.isIn(OneironautTags.Entities.ideaUnstorable)){
            throw MishapBadEntity(entity, Text.translatable("oneironaut.mishap.unstorable_entity"))
        }
        val cost = if (entity is LivingEntity){
            (MediaConstants.SHARD_UNIT * entity.maxHealth).toLong()
        } else {
            MediaConstants.CRYSTAL_UNIT
        }
        return SpellAction.Result(Spell(entity, key), cost, listOf(ParticleSpray.burst(entity.pos, 2.0, 32)))
    }

    private class Spell(val entity : Entity, val key : IdeaKeyable) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val manager = IdeaInscriptionManager.getServerState(env.world.server)
            val existingEntry = IdeaInscriptionManager.getEntry(key, env.world, IdeaEntry.EntryType.ENTITY)
            if (existingEntry != null){
                val entityPos = entity.pos
                val entityToRelease = existingEntry.payload as Entity
                entityToRelease.setPosition(entityPos)
                env.world.spawnEntity(entityToRelease)
            }
            IdeaInscriptionManager.writeEntry(key, IdeaEntry(entity, env.world.time, env.castingEntity))
            entity.remove(Entity.RemovalReason.DISCARDED) //not sure what exactly removal reasons do but this is the one hex machina uses for its capsule
            manager.markDirty()
        }
    }
}