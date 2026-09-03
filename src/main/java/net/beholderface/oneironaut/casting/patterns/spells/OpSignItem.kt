package net.beholderface.oneironaut.casting.patterns.spells

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment.HeldItemInfo
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.network.chat.Component
import net.beholderface.oneironaut.casting.mishaps.MishapNoStaff
import net.minecraft.server.level.ServerPlayer
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.CustomData


class OpSignItem : ConstMediaAction {
    override val argc = 0
    override val mediaCost = MediaConstants.DUST_UNIT
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        if (env.castingEntity == null || env.castingEntity !is ServerPlayer){
            throw MishapBadCaster()
        }
        if (env !is StaffCastEnv){
            throw MishapNoStaff(Component.translatable("hexcasting.spell.oneironaut:signitem"))
        }
        //can be used on any item
        val heldInfo : HeldItemInfo = env.getHeldItemToOperateOn{it != null} ?: throw MishapBadOffhandItem(null, Component.nullToEmpty("no_item.offhand"))
        val itemToSign = heldInfo.stack
        val nbt = itemToSign.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: net.minecraft.nbt.CompoundTag()
        val existingSignature = if(nbt.contains("soulprint_signature")){
            nbt.getUUID("soulprint_signature")
        } else {
            null
        }
        nbt.putUUID("soulprint_signature", env.castingEntity!!.uuid)
        if (existingSignature != null){
            if (existingSignature.equals(env.castingEntity!!.uuid)){
                nbt.remove("soulprint_signature")
            }
        }
        CustomData.set(DataComponents.CUSTOM_DATA, itemToSign, nbt)
        return listOf()
    }
}
