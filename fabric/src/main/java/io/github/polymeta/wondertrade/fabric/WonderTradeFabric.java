package io.github.polymeta.wondertrade.fabric;

import io.github.polymeta.wondertrade.WonderTrade;
import net.fabricmc.api.ModInitializer;

public class WonderTradeFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WonderTrade.init();
    }
}
