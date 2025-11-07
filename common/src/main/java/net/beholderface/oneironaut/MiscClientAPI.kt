package net.beholderface.oneironaut

import net.minecraft.client.MinecraftClient

fun getClientTime() : Long{
    val world = MinecraftClient.getInstance().world
    return world?.time ?: -1L
}