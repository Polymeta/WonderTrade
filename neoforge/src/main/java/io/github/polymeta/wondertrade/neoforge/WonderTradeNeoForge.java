package io.github.polymeta.wondertrade.neoforge;

import io.github.polymeta.wondertrade.WonderTrade;
import net.neoforged.fml.common.Mod;

@Mod(WonderTrade.MOD_ID)
public class WonderTradeNeoForge {

    public WonderTradeNeoForge() {
        WonderTrade.init();
    }
}
