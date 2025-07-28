package net.beholderface.oneironaut;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class DeepNoosphereDimensionEffects extends DimensionEffects {
    public static final Vec3d fogColor = new Vec3d(96.7, 0.0, 103.6);
    public DeepNoosphereDimensionEffects() {
        super(-2032.0f, false, SkyType.END, false, true);
    }

    @Override
    public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
        return color;
    }

    @Override
    public boolean useThickFog(int camX, int camY) {
        return true;
    }

    @Override
    public float[] getFogColorOverride(float skyAngle, float tickDelta) {
        float divisor = 512f;
        return new float[]{(float)fogColor.x / divisor, (float)fogColor.y / divisor, (float)fogColor.z / divisor, 0.025f};
    }
}
