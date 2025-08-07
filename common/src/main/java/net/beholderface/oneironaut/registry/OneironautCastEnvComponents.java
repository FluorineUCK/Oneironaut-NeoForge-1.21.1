package net.beholderface.oneironaut.registry;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.casting.NoosphereAmbitExtensionComponent;
import net.beholderface.oneironaut.casting.lichdom.LichMediaExtractComponent;
import net.beholderface.oneironaut.casting.environments.LichPassiveHexEnv;

public class OneironautCastEnvComponents {
    public static void init(){
        CastingEnvironment.addCreateEventListener((env, nbt)->{
            if (env instanceof PlayerBasedCastEnv){
                if (!(env instanceof LichPassiveHexEnv)){
                    env.addExtension(new LichMediaExtractComponent(env));
                }
                if (Oneironaut.isWorldNoosphere(env.getWorld())){
                    env.addExtension(new NoosphereAmbitExtensionComponent(env));
                }
            }
        });
    }
}