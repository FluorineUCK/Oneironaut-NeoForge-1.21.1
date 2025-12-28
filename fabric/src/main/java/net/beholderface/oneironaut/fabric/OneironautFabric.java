package net.beholderface.oneironaut.fabric;

import at.petrak.hexcasting.common.items.ItemLens;
import at.petrak.hexcasting.common.lib.HexItems;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.ModInitializer;
import net.beholderface.oneironaut.Oneironaut;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * This is your loading entrypoint on fabric(-likes), in case you need to initialize
 * something platform-specific.
 * <br/>
 * Since quilt can load fabric mods, you develop for two platforms in one fell swoop.
 * Feel free to check out the <a href="https://github.com/architectury/architectury-templates">Architectury templates</a>
 * if you want to see how to add quilt-specific code.
 */
public class OneironautFabric implements ModInitializer {
    FabricOneironautConfig config = FabricOneironautConfig.setup();
    @Override
    public void onInitialize() {
        Oneironaut.init();
        TickEvent.SERVER_POST.register((server)->{
            if (Platform.isModLoaded("trinkets")){
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
                    var trinketComponentMaybe = TrinketsApi.getTrinketComponent(player);
                    if (trinketComponentMaybe.isPresent()){
                        TrinketComponent component = trinketComponentMaybe.get();
                        var lenses = component.getEquipped(HexItems.SCRYING_LENS);
                        for (var lensPair : lenses){
                            ItemStack stack = lensPair.getRight();
                            if (stack.getItem() instanceof ItemLens lens){
                                lens.inventoryTick(stack, player.getWorld(), player, 0, false);
                            }
                        }
                    }
                }
            }
        });
    }
}
