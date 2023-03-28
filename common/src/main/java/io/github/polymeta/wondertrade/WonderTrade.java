package io.github.polymeta.wondertrade;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;


public class WonderTrade {
    public static final String MOD_ID = "wondertrade";
    
    public static void init() {
        System.out.println("Hello from common");
        //register command for trade and regeneration of pool
        //CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
        //
        //});
        //load config and pool and message configuration
    }
}
