package net.beholderface.oneironaut

import net.minecraft.client.Minecraft

fun getClientTime() : Long{
    val world = Minecraft.getInstance().level
    return world?.gameTime ?: -1L
}

fun getClientDayTime() : Long{
    val world = Minecraft.getInstance().level
    return world?.dayTime ?: -1L
}