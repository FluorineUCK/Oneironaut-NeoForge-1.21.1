package net.beholderface.oneironaut.casting.patterns

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment.HeldItemInfo
import at.petrak.hexcasting.api.casting.iota.BooleanIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.utils.getUUID
import net.minecraft.world.item.Items
import net.beholderface.oneironaut.getSoulprint
import net.minecraft.network.chat.Component
import net.minecraft.core.component.DataComponents

class OpCompareSignature : ConstMediaAction {
    override val argc = 1
    override val mediaCost = MediaConstants.DUST_UNIT / 10
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val checkedSignature = args.getSoulprint(0, argc)
        val readItemInfo : HeldItemInfo = env.getHeldItemToOperateOn {it.item != Items.AIR} ?: throw MishapBadOffhandItem(null, Component.nullToEmpty("no_item.offhand"))
        val readItem = readItemInfo.stack
        val nbt = readItem.get(DataComponents.CUSTOM_DATA)?.copyTag()
        if (nbt != null && nbt.hasUUID("soulprint_signature")){
            if (nbt.getUUID("soulprint_signature") == checkedSignature){
                return listOf(BooleanIota(true))
            }
        }
        return listOf(BooleanIota(false))
    }
}
