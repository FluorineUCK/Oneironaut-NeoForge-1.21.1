package net.beholderface.oneironaut

import net.minecraft.client.MinecraftClient

fun getClientTime() : Long{
    val world = MinecraftClient.getInstance().world
    return world?.time ?: -1L
}

fun getClientDayTime() : Long{
    val world = MinecraftClient.getInstance().world
    return world?.timeOfDay ?: -1L
}