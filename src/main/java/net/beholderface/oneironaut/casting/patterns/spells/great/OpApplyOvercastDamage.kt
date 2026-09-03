package net.beholderface.oneironaut.casting.patterns.spells.great

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.EnchantedBookItem
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.Registries
import net.beholderface.oneironaut.network.ItemUpdatePacket
import net.beholderface.oneironaut.casting.mishaps.MishapMissingEnchant
import net.beholderface.oneironaut.registry.OneironautMiscRegistry
import ram.talia.hexal.api.getItemEntityOrItemFrame
//import ram.talia.hexal.api.getItemEntityOrItemFrame
import kotlin.math.pow

class OpApplyOvercastDamage : SpellAction {
    override val argc = 1
    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val holder = args.getEntity(env.world, 0, argc)
        val target = args.getItemEntityOrItemFrame(0, argc, env.world)
        env.assertEntityInRange(holder)
        val stack : ItemStack = if (!(target.left().isEmpty)){
            target.left().get().item
        } else {
            target.right().get().item
        }
        val sharpness = env.world.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
            .getHolderOrThrow(Enchantments.SHARPNESS)
        val stackEnchants = EnchantmentHelper.getEnchantmentsForCrafting(stack)
        val level = stackEnchants.getLevel(sharpness)
        if (level <= 0) {
            throw MishapMissingEnchant(stack, sharpness)
        }
        val book = stack.item is EnchantedBookItem
        val multiplier = if (book) 15 else 10
        return SpellAction.Result(
            Spell(stack, level, stackEnchants, holder),
            (level.toDouble().pow(2) * MediaConstants.CRYSTAL_UNIT * multiplier).toLong(),
            listOf(ParticleSpray.cloud(holder.position(), 2.0))
        )
    }

    private data class Spell(val stack : ItemStack, val level : Int, val existingEnchantments : ItemEnchantments, val holder : Entity) : RenderedSpell{
        override fun cast(env: CastingEnvironment) {
            val overcastDamage = env.world.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(OneironautMiscRegistry.OVERCAST_DAMAGE_ENCHANT)
            val rewritten = ItemEnchantments.Mutable(ItemEnchantments.EMPTY)
            rewritten.set(overcastDamage, level)
            for (entry in existingEnchantments.entrySet()) {
                if (Enchantment.areCompatible(overcastDamage, entry.key)) {
                    rewritten.set(entry.key, entry.intValue)
                }
            }
            EnchantmentHelper.setEnchantments(stack, rewritten.toImmutable())
            IXplatAbstractions.INSTANCE.sendPacketNear(holder.position(), 128.0, env.world, ItemUpdatePacket(stack, holder))
        }

    }
}
