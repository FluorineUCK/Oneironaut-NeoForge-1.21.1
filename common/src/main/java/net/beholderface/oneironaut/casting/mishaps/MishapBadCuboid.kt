package net.beholderface.oneironaut.casting.mishaps

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.world.World

class MishapBadCuboid(val stub : String) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment = dyeColor(DyeColor.PURPLE)

    override fun particleSpray(ctx: CastingEnvironment) =
        ParticleSpray.burst(ctx.mishapSprayPos(), 1.0)

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Text =
        error("oneironaut:badcuboid.$stub")

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        stack.add(GarbageIota())
        env.world.createExplosion(null, env.mishapSprayPos().x, env.mishapSprayPos().y, env.mishapSprayPos().z, 0.25f, World.ExplosionSourceType.NONE)
    }

    companion object {
        @JvmStatic
        fun of(stub : String): MishapBadCuboid {
            return MishapBadCuboid(stub)
        }
    }

}