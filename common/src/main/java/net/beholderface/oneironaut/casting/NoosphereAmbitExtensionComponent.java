package net.beholderface.oneironaut.casting;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent;
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv;
import at.petrak.hexcasting.common.lib.HexAttributes;
import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class NoosphereAmbitExtensionComponent implements CastingEnvironmentComponent.IsVecInRange {
    public final Key<IsVecInRange> key;
    public final CastingEnvironment environment;

    public NoosphereAmbitExtensionComponent(CastingEnvironment env){
        this.key = new NoosphereAmbitKey();
        this.environment = env;
    }
    @Override
    public boolean onIsVecInRange(Vec3d vec, boolean current) {
        if (!current && environment.getCastingEntity() instanceof ServerPlayerEntity serverPlayer && Oneironaut.isWorldNoosphere(environment.getWorld())){
            double ambitRadius = serverPlayer.getAttributeValue(HexAttributes.AMBIT_RADIUS);
            double doubledAmbitRadius = ambitRadius * 2;
            //make sure the environment can access normal ambit before extending it
            if (((PlayerBasedCastEnv) environment).isVecInRangeEnvironment(serverPlayer.getPos().add(0.0, ambitRadius - 0.001, 0.0))){
                return vec.squaredDistanceTo(serverPlayer.getPos()) <= doubledAmbitRadius * doubledAmbitRadius + 0.00000000001;
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
