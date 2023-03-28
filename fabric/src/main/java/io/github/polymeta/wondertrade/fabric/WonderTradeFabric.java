package io.github.polymeta.wondertrade.fabric;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import io.github.polymeta.wondertrade.WonderTrade;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;

public class WonderTradeFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, battleVictoryEvent -> {
            battleVictoryEvent.getBattle();
            return Unit.INSTANCE;
        });
        WonderTrade.init();
    }
}
