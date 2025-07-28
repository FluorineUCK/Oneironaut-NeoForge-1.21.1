package net.beholderface.oneironaut.casting;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent;
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv;
import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

import static at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv.AMBIT_RADIUS;

public class NoosphereAmbitExtensionComponent implements CastingEnvironmentComponent.IsVecInRange {
    public final Key<IsVecInRange> key;
    public final CastingEnvironment environment;
    public static final double DOUBLED_AMBIT_RADIUS = AMBIT_RADIUS * 2;

    public NoosphereAmbitExtensionComponent(CastingEnvironment env){
        this.key = new NoosphereAmbitKey();
        this.environment = env;
    }
    @Override
    public boolean onIsVecInRange(Vec3d vec, boolean current) {
        if (!current && environment.getCastingEntity() instanceof ServerPlayerEntity serverPlayer && Oneironaut.isWorldNoosphere(environment.getWorld())){
            //make sure the environment can access normal ambit before extending it
            if (((PlayerBasedCastEnv) environment).isVecInRangeEnvironment(serverPlayer.getPos().add(0.0, AMBIT_RADIUS - 0.001, 0.0))){
                return vec.squaredDistanceTo(serverPlayer.getPos()) <= DOUBLED_AMBIT_RADIUS * DOUBLED_AMBIT_RADIUS + 0.00000000001;
            }
        }
        return current;
    }

    @Override
    public Key<IsVecInRange> getKey() {
        return this.key;
    }

    private static class NoosphereAmbitKey implements Key<IsVecInRange>{
        private final UUID uuid;
        public NoosphereAmbitKey(){
            uuid = UUID.randomUUID();
        }
    }
}
