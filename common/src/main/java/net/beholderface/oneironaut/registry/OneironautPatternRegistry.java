package net.beholderface.oneironaut.registry;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.casting.actions.spells.OpMakePackagedSpell;
import at.petrak.hexcasting.common.lib.hex.HexActions;
import dev.architectury.registry.registries.RegistrySupplier;
import kotlin.Triple;
import net.beholderface.oneironaut.Oneironaut;
import net.beholderface.oneironaut.OneironautConfig;
import net.beholderface.oneironaut.casting.patterns.*;
import net.beholderface.oneironaut.casting.patterns.rod.*;
import net.beholderface.oneironaut.casting.patterns.spells.*;
import net.beholderface.oneironaut.casting.patterns.spells.great.*;
import net.beholderface.oneironaut.casting.patterns.spells.idea.*;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class OneironautPatternRegistry {
    public static List<Triple<HexPattern, Identifier, Action>> PATTERNS = new ArrayList<>();
    public static List<Triple<HexPattern, Identifier, Action>> PER_WORLD_PATTERNS = new ArrayList<>();
    //operators/other actions
    public static HexPattern GETDIM_SELF = register(HexPattern.fromAngles("wqwqwqwqwqwaeqqe", HexDir.WEST), "getdim1", new OpGetDim(false));
    public static HexPattern GETDIM_SENTINEL = register(HexPattern.fromAngles("wqwqwqwqwqwaqeeq", HexDir.WEST), "getdim2", new OpGetDim(true));
    public static HexPattern GETDIM_OVERWORLD = register(HexPattern.fromAngles("wqwqwqwqwqwawedewdwedew", HexDir.NORTH_EAST), "getdim/overworld", new OpSpecificDim(new Identifier("minecraft:overworld"), false, ()->{return OneironautConfig.getServer().getAllowOverworldReflection();}));
    public static HexPattern GETDIM_NETHER = register(HexPattern.fromAngles("wqwqwqwqwqwaqaaqaw", HexDir.NORTH_EAST), "getdim/nether", new OpSpecificDim(new Identifier("minecraft:the_nether"), true, ()->{return OneironautConfig.getServer().getAllowNetherReflection();}));
    public static HexPattern GET_DIMHEIGHT = register(HexPattern.fromAngles("awqqqwqwqwqwqwq", HexDir.NORTH_EAST), "getdimheight", new OpDimHeight());
    public static HexPattern GET_DIMSCALE = register(HexPattern.fromAngles("wawawqwqwqwqwqw", HexDir.NORTH_WEST), "getdimscale", new OpDimScale());
    public static HexPattern ROD_LOOK = register(HexPattern.fromAngles("qwqqqwqawa", HexDir.SOUTH_EAST), "getrodlook", new OpGetInitialRodState(1));
    public static HexPattern ROD_POS = register(HexPattern.fromAngles("qwqqqwqawaa", HexDir.SOUTH_EAST), "getrodpos", new OpGetInitialRodState(2));
    public static HexPattern ROD_STAMP = register(HexPattern.fromAngles("qwqqqwqawaaw", HexDir.SOUTH_EAST), "getrodstamp", new OpGetInitialRodState(3));
    public static HexPattern ROD_RAM_READ = register(HexPattern.fromAngles("qeeweeewddw", HexDir.NORTH_EAST), "readrodram", new OpAccessRAM(false));
    public static HexPattern ROD_RAM_WRITE = register(HexPattern.fromAngles("eqqwqqqwaaw", HexDir.NORTH_WEST), "writerodram", new OpAccessRAM(true));
    public static HexPattern ROD_RAM_READ_REMOTE = register(HexPattern.fromAngles("qwaqawewewaqawewddw", HexDir.NORTH_EAST), "readrodramremote", new OpAccessRAMRemote(false));
    public static HexPattern ROD_RAM_WRITE_REMOTE = register(HexPattern.fromAngles("ewdedwqwqwdedwqwaaw", HexDir.NORTH_WEST), "writerodramremote", new OpAccessRAMRemote(true));

    public static HexPattern READ_IDEA = register(HexPattern.fromAngles("qwqwqwqwqwqqqwedewq", HexDir.WEST), "readidea", new OpGetIdea());
    public static HexPattern READ_IDEA_TIME = register(HexPattern.fromAngles("qwqwqwqwqwqqqeqaqeq", HexDir.WEST), "readideatime", new OpGetIdeaTimestamp());
    public static HexPattern COMPARE_IDEA_WRITER = register(HexPattern.fromAngles("qwqwqwqwqwqaeqedeqe", HexDir.WEST), "readideawriter", new OpGetIdeaWriter());
    public static HexPattern READ_IDEA_TYPE = register(HexPattern.fromAngles("qwqwqqqaqqqwqwqqqaq", HexDir.WEST), "readideatype", new OpGetIdeaType());
    public static HexPattern READ_SENTINEL = register(HexPattern.fromAngles("waeawaeddwwd", HexDir.EAST), "readsentinel", new OpReadSentinel());
    public static HexPattern DETECT_SHROUDED = register(HexPattern.fromAngles("qqqqqwwaawewaawdww", HexDir.SOUTH_EAST), "detectshroud", new OpDetectShrouded());
    //normal spells
    public static HexPattern DELAY_ROD = register(HexPattern.fromAngles("qwqqqwqaqddq", HexDir.SOUTH_EAST), "delayrod", new OpDelayRod());
    public static HexPattern HALT_ROD = register(HexPattern.fromAngles("aqdeeweeew", HexDir.SOUTH_WEST), "haltrod", new OpHaltRod(0));
    public static HexPattern RESET_ROD = register(HexPattern.fromAngles("deaqqwqqqw", HexDir.SOUTH_EAST), "resetrod", new OpHaltRod(1));
    public static HexPattern QUERY_ROD = register(HexPattern.fromAngles("qwqqqwqaeaqa", HexDir.SOUTH_EAST), "queryrod", new OpCheckForRod());
    public static HexPattern ROD_LOOP_ACTIVE = register(HexPattern.fromAngles("qwqqqwqaqded", HexDir.SOUTH_EAST), "rodloopactive", new OpCheckForRodOther());
    public static HexPattern WRITE_IDEA_IOTA = register(HexPattern.fromAngles("eweweweweweeewqaqwe", HexDir.EAST), "writeidea", new OpStoreIota());
    public static HexPattern GET_SOULPRINT = register(HexPattern.fromAngles("qqaqwedee", HexDir.EAST), "getsoulprint", new OpGetSoulprint());
    public static HexPattern SIGN_ITEM = register(HexPattern.fromAngles("qqaqwedeea", HexDir.EAST), "signitem", new OpSignItem());
    public static HexPattern CHECK_SIGNATURE = register(HexPattern.fromAngles("qqaqwedeed", HexDir.EAST), "checksignature", new OpCompareSignature());
    public static HexPattern CIRCLE = register(HexPattern.fromAngles("wwwwwwqwwwwwwqwwwwwwqwwwwwwqwwwwwwqwwwwww", HexDir.SOUTH_EAST), "circle", new OpCircle());
    //it's supposed to look like a classic game of life glider
    public static HexPattern ADVANCE_AUTOMATON = register(HexPattern.fromAngles("qqwqwqwaqeee", HexDir.SOUTH_WEST), "advanceautomaton", new OpAdvanceAutomaton());
    //public static HexPattern TRIGGER_AUTOMATON = regi(not actually, hexdoc regex, this is commented out)ster(HexPattern.fromAngles("eewewewdeqqq", HexDir.SOUTH_EAST), "triggerautomaton", new OpTriggerAutomaton());

    /*dang you hexdoc
    public static HexPattern CRAFT_ROD = register(HexPattern.fromAngles("eqqqqqawweqqqqqawweqqqqqawwdeqewwwwweqeeeqewwwwweqe", HexDir.EAST), "craftrod", new OpMakePackagedSpell<>((ItemPackagedHex) OneironautThingRegistry.REVERBERATION_ROD.get(), MediaConstants.CRYSTAL_UNIT
    public static HexPattern CRAFT_BOTTOMLESS_TRINKET = register(HexPattern.fromAngles("wwqeeeeewqqqqqewwaqeqwqeqqqeqwqeq", HexDir.EAST), "craftbottomlesstrinket", new OpWriteBottomlessTrinket()*/
    //public static HexPattern MUFFLE_WISP = dontdoithexdocilleatyourknees(HexPattern.fromAngles("aaqdwaaqaweewaqawee", HexDir.WEST), "mufflewisp", new OpSetWispVolume());

    //great spells
    public static HexPattern DIM_TELEPORT = registerPerWorld(HexPattern.fromAngles("qeewwwweeqeqeewwwweeqdqqdwwwdqeqdwwwdqdadwwdqdwwddadaqadaawww", HexDir.NORTH_EAST), "dimteleport", new OpDimTeleport());
    public static HexPattern INFUSE_MEDIA = registerPerWorld(HexPattern.fromAngles("wwaqqqqqeqqqwwwqqeqqwwwqqweqadadadaqeqeqadadadaqe", HexDir.EAST), "infusemedia", new OpInfuseMedia());
    public static HexPattern SWAP_SPACE = registerPerWorld(HexPattern.fromAngles("wqqqwwwwwqqqwwwqdaqadwqqwdaqadweqeqqqqeqeqaqeqedeqeqa", HexDir.EAST), "swapspace", new OpSwapSpace());
    public static HexPattern RESIST_DETECTION = registerPerWorld(HexPattern.fromAngles("wawwwdwdwwaqqqqqe", HexDir.EAST), "resistdetection", new OpResistDetection());
    public static HexPattern APPLY_NOT_MISSING = registerPerWorld(HexPattern.fromAngles("qdaeqeawaeqeadqqdeed", HexDir.SOUTH_WEST), "applynotmissing", new OpMarkEntity());
    public static HexPattern APPLY_MIND_RENDER = registerPerWorld(HexPattern.fromAngles("qweqadeqadeqadqqqwdaqedaqedaqeqaqdwawdwawdwaqawdwawdwawddwwwwwqdeddw", HexDir.EAST), "applymindrender", new OpApplyOvercastDamage());
    public static HexPattern REVIVE_FLAYED = registerPerWorld(HexPattern.fromAngles("qeqwqqedeeeeeaqwqeqaqedqde", HexDir.NORTH_EAST), "reviveflayed", new OpReviveFlayed());

    public static HexPattern WRITE_IDEA_ENTITY = register(HexPattern.fromAngles("weweeedeeewewdqqaw", HexDir.SOUTH_EAST), "writeidea_entity", new OpStoreEntity());
    public static HexPattern RELEASE_ENTITY = register(HexPattern.fromAngles("wqwqdqaqdqwqwaeedw", HexDir.SOUTH_WEST), "releaseidea_entity", new OpReleaseEntity());

    public static HexPattern EVAL_EXTRADIMENSIONAL = register(HexPattern.fromAngles("wqwqwqwqwqwaqdeaqqe", HexDir.WEST), "extradimensionaleval", new OpEvalExtradimensional());
    public static HexPattern SHIFT_SENTINEL = register(HexPattern.fromAngles("wwaeawwaeqqwqwqwqwqwq", HexDir.EAST), "shiftsentinel", new OpShiftSentinel());
    public static HexPattern EROSION_SHIELD = register(HexPattern.fromAngles("wwqwwqwwqwwqwwqwwaeqwwqqqwwqaeadaqadaawww", HexDir.WEST), "erosionshield", new OpErosionShield());

    public static void init() {
        try {
            for (Triple<HexPattern, Identifier, Action> patternTriple : PATTERNS) {
                Registry.register(HexActions.REGISTRY, patternTriple.getSecond(), new ActionRegistryEntry(patternTriple.getFirst(), patternTriple.getThird()));
            }
            for (Triple<HexPattern, Identifier, Action> patternTriple : PER_WORLD_PATTERNS) {
                Registry.register(HexActions.REGISTRY, patternTriple.getSecond(), new ActionRegistryEntry(patternTriple.getFirst(), patternTriple.getThird()));
            }
            /*for (Triple<String[][], Identifier, ICellSpell> cellTriple : CELL_PATTERNS){
                CellSpellManager.registerCellSpell(cellTriple.getFirst(), cellTriple.getSecond(), cellTriple.getThird());
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }        registerItemDependentPatterns();

    }
//stolen from gloop
    private static final Map<RegistrySupplier<? extends Item>, UncheckedPatternRegister> itemDependentPatternRegisterers = new HashMap<>();

    static {
        itemDependentPatternRegisterers.put(OneironautItemRegistry.REVERBERATION_ROD, () -> {
            Registry.register(HexActions.REGISTRY, new Identifier(Oneironaut.MOD_ID, "craftrod"),
                    new ActionRegistryEntry(HexPattern.fromAngles("eqqqqqawweqqqqqawweqqqqqawwdeqewwwwweqeeeqewwwwweqe", HexDir.EAST),
                            new OpMakePackagedSpell(OneironautItemRegistry.REVERBERATION_ROD.get(), MediaConstants.CRYSTAL_UNIT * 10)));});
        itemDependentPatternRegisterers.put(OneironautItemRegistry.BOTTOMLESS_CASTING_ITEM, () -> {
            Registry.register(HexActions.REGISTRY, new Identifier(Oneironaut.MOD_ID, "craftbottomlesstrinket"),
                    new ActionRegistryEntry(HexPattern.fromAngles("wwqeeeeewqqqqqewwaqeqwqeqqqeqwqeq", HexDir.EAST), new OpWriteBottomlessTrinket()));});
    }

    private static void registerItemDependentPatterns(){
        for(Map.Entry<RegistrySupplier<? extends Item>, UncheckedPatternRegister> entry : itemDependentPatternRegisterers.entrySet()){
            entry.getKey().listen(item -> {
                try{
                    entry.getValue().register();
                } catch (Exception exn) {
                    exn.printStackTrace();
                }
            });
        }
    }

    private static HexPattern register(HexPattern pattern, String name, Action action) {
        Triple<HexPattern, Identifier, Action> triple = new Triple<>(pattern, Oneironaut.id(name), action);
        PATTERNS.add(triple);
        return pattern;
    }

    private static HexPattern registerPerWorld(HexPattern pattern, String name, Action action) {
        return register(pattern, name, action);
    }

    @FunctionalInterface
    public static interface UncheckedPatternRegister{
        public void register();
    }
}
