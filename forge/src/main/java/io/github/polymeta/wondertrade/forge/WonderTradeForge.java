package io.github.polymeta.wondertrade.forge;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import io.github.polymeta.wondertrade.WonderTrade;
import kotlin.Unit;
import net.minecraftforge.fml.common.Mod;

@Mod(WonderTrade.MOD_ID)
public class WonderTradeForge {

    public WonderTradeForge() {
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, battleVictoryEvent -> {
            battleVictoryEvent.getBattle();
            return Unit.INSTANCE;
        });
        WonderTrade.init();
    }
}
