package net.beholderface.oneironaut.neo;

import net.beholderface.oneironaut.OneironautConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class OneironautNeoConfig {
    public static final OneironautConfig.CommonConfigAccess COMMON = new OneironautConfig.CommonConfigAccess() {};
    public static final OneironautConfig.ClientConfigAccess CLIENT = new OneironautConfig.ClientConfigAccess() {};

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.BooleanValue PLANE_SHIFT_OTHER_PLAYERS;
    private static final ModConfigSpec.BooleanValue PLANE_SHIFT_NONLIVING;
    private static final ModConfigSpec.IntValue IDEA_LIFETIME;
    private static final ModConfigSpec.BooleanValue SWAP_REQUIRES_NOOSPHERE;
    private static final ModConfigSpec.BooleanValue SWAP_SWAPS_BLOCK_ENTITIES;
    private static final ModConfigSpec.BooleanValue REDIRECT_FIREBALL;
    private static final ModConfigSpec.BooleanValue INFUSE_ETERNAL_CHORUS;
    private static final ModConfigSpec.BooleanValue ALLOW_OVERWORLD_REFLECTION;
    private static final ModConfigSpec.BooleanValue ALLOW_NETHER_REFLECTION;
    private static final ModConfigSpec.DoubleValue STALE_IPHIAL_LENIENCE;

    static {
        BUILDER.push("server");
        PLANE_SHIFT_OTHER_PLAYERS = BUILDER.define("planeShiftOtherPlayers",
            OneironautConfig.ServerConfigAccess.DEFAULT_ALLOW_PLANESHIFT_OTHERS);
        PLANE_SHIFT_NONLIVING = BUILDER.define("planeShiftNonliving",
            OneironautConfig.ServerConfigAccess.DEFAULT_ALLOW_PLANESHIT_NONLIVING);
        IDEA_LIFETIME = BUILDER.defineInRange("ideaLifetime",
            OneironautConfig.ServerConfigAccess.DEFAULT_IDEA_LIFETIME, 1, 20 * 60 * 60 * 24 * 7);
        SWAP_REQUIRES_NOOSPHERE = BUILDER.define("swapRequiresNoosphere",
            OneironautConfig.ServerConfigAccess.DEFAULT_SWAP_NOOSPHERE);
        SWAP_SWAPS_BLOCK_ENTITIES = BUILDER.define("swapSwapsBlockEntities",
            OneironautConfig.ServerConfigAccess.DEFAULT_SWAP_BES);
        REDIRECT_FIREBALL = BUILDER.define("impulseRedirectsFireball",
            OneironautConfig.ServerConfigAccess.DEFAULT_REDIRECT_FIREBALL);
        INFUSE_ETERNAL_CHORUS = BUILDER.define("infusionEternalChorus",
            OneironautConfig.ServerConfigAccess.DEFAULT_INFUSE_CHORUS);
        ALLOW_OVERWORLD_REFLECTION = BUILDER.define("allowOverworldReflection",
            OneironautConfig.ServerConfigAccess.DEFAULT_OVERWORLD_REFLECTION);
        ALLOW_NETHER_REFLECTION = BUILDER.define("allowNetherReflection",
            OneironautConfig.ServerConfigAccess.DEFAULT_NETHER_REFLECTION);
        STALE_IPHIAL_LENIENCE = BUILDER.defineInRange("staleIPhialLenience",
            OneironautConfig.ServerConfigAccess.DEFAULT_STALE_IPHIAL_LENIENCE, 0.0, 1.0);
        BUILDER.pop();
    }

    public static final ModConfigSpec SERVER_SPEC = BUILDER.build();

    public static final OneironautConfig.ServerConfigAccess SERVER = new OneironautConfig.ServerConfigAccess() {
        @Override public boolean getPlaneShiftOtherPlayers() { return PLANE_SHIFT_OTHER_PLAYERS.get(); }
        @Override public boolean getPlaneShiftNonliving() { return PLANE_SHIFT_NONLIVING.get(); }
        @Override public int getIdeaLifetime() { return IDEA_LIFETIME.get(); }
        @Override public boolean getSwapRequiresNoosphere() { return SWAP_REQUIRES_NOOSPHERE.get(); }
        @Override public boolean getSwapSwapsBEs() { return SWAP_SWAPS_BLOCK_ENTITIES.get(); }
        @Override public boolean getImpulseRedirectsFireball() { return REDIRECT_FIREBALL.get(); }
        @Override public boolean getInfusionEternalChorus() { return INFUSE_ETERNAL_CHORUS.get(); }
        @Override public boolean getAllowOverworldReflection() { return ALLOW_OVERWORLD_REFLECTION.get(); }
        @Override public boolean getAllowNetherReflection() { return ALLOW_NETHER_REFLECTION.get(); }
        @Override public float getStaleIPhialLenience() { return STALE_IPHIAL_LENIENCE.get().floatValue(); }
    };

    private OneironautNeoConfig() {}
}
