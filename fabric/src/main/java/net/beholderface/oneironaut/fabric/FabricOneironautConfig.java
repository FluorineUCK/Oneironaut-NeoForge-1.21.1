package net.beholderface.oneironaut.fabric;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.OneironautConfig;
import net.minecraft.util.Identifier;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.OneironautConfig;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Config(name = Oneironaut.MOD_ID)
@Config.Gui.Background("oneironaut:textures/block/noosphere_basalt.png")
public class FabricOneironautConfig extends PartitioningSerializer.GlobalData {

    @ConfigEntry.Category("common")
    @ConfigEntry.Gui.TransitiveObject
    public final Common common = new Common();
    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.TransitiveObject
    public final Client client = new Client();
    @ConfigEntry.Category("server")
    @ConfigEntry.Gui.TransitiveObject
    public final Server server = new Server();

    public static FabricOneironautConfig setup() {
        AutoConfig.register(FabricOneironautConfig.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        var instance = AutoConfig.getConfigHolder(FabricOneironautConfig.class).getConfig();

        OneironautConfig.setCommon(instance.common);
        // We care about the client only on the *physical* client ...
        if (IXplatAbstractions.INSTANCE.isPhysicalClient()) {
            OneironautConfig.setClient(instance.client);
        }
        // but we care about the server on the *logical* server
        // i believe this should Just Work without a guard? assuming we don't access it from the client ever
        OneironautConfig.setServer(instance.server);

        return instance;
    }

    @Config(name = "common")
    private static class Common implements ConfigData, OneironautConfig.CommonConfigAccess { }

    @Config(name = "client")
    private static class Client implements ConfigData, OneironautConfig.ClientConfigAccess { }

    @Config(name = "server")
    private static class Server implements ConfigData, OneironautConfig.ServerConfigAccess {


        @ConfigEntry.Gui.CollapsibleObject
        private MiscConfig miscConfig = new MiscConfig();

        static class MiscConfig {
            int ideaLifetime = DEFAULT_IDEA_LIFETIME;
            boolean planeShiftOtherPlayers = DEFAULT_ALLOW_PLANESHIFT_OTHERS;
            //boolean reduceEverbookLogSpam = DEFAULT_REDUCE_EVERBOOK_SPAM;
            boolean swapRequiresNoosphere = DEFAULT_SWAP_NOOSPHERE;
            boolean swapSwapsBEs = DEFAULT_SWAP_BES;
            boolean impulseRedirectsFireball = DEFAULT_REDIRECT_FIREBALL;
            boolean infusionEternalChorus = DEFAULT_INFUSE_CHORUS;
            boolean allowOverworldReflection = DEFAULT_OVERWORLD_REFLECTION;
            boolean allowNetherReflection = DEFAULT_NETHER_REFLECTION;
            float staleIPhialLenience = DEFAULT_STALE_IPHIAL_LENIENCE;
        }
        @Override
        public boolean getPlaneShiftOtherPlayers() {
            return miscConfig.planeShiftOtherPlayers;
        }

        @Override
        public void validatePostLoad() throws ValidationException {
            this.miscConfig.ideaLifetime = bound(this.miscConfig.ideaLifetime, 1, 20 * 60 * 60 * 24 * 7); //one IRL week
            this.miscConfig.staleIPhialLenience = bound(this.miscConfig.staleIPhialLenience, 0.0f, 1.0f);
        }

        private int bound(int toBind, int lower, int upper) {
            return Math.min(Math.max(toBind, lower), upper);
        }
        private float bound(float toBind, float lower, float upper) {
            return Math.min(Math.max(toBind, lower), upper);
        }
        private double bound(double toBind, double lower, double upper) {
            return Math.min(Math.max(toBind, lower), upper);
        }

        @Override
        public int getIdeaLifetime() {
            return miscConfig.ideaLifetime;
        }

        /*@Override
        public boolean getReduceEverbookLogSpam() {
            return miscConfig.reduceEverbookLogSpam;
        }*/

        public boolean getSwapRequiresNoosphere(){
            return miscConfig.swapRequiresNoosphere;
        }

        @Override
        public boolean getSwapSwapsBEs() {
            return miscConfig.swapSwapsBEs;
        }

        @Override
        public boolean getImpulseRedirectsFireball() {
            return miscConfig.impulseRedirectsFireball;
        }

        @Override
        public boolean getInfusionEternalChorus() {
            return miscConfig.infusionEternalChorus;
        }

        @Override
        public boolean getAllowOverworldReflection() {
            return miscConfig.allowOverworldReflection;
        }

        @Override
        public boolean getAllowNetherReflection() {
            return miscConfig.allowNetherReflection;
        }

        @Override
        public float getStaleIPhialLenience() {
            return miscConfig.staleIPhialLenience;
        }

        private static boolean isValidID(Object o) {
            return o instanceof String s && Identifier.isValid(s);
        }
    }
}
