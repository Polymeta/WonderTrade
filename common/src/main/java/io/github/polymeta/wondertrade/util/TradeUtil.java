package io.github.polymeta.wondertrade.util;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.permission.CobblemonPermission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.polymeta.wondertrade.WonderTrade;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

public class TradeUtil
{
    private static final Random rng = new Random();
    private static final ConcurrentSkipListSet<UUID> playersOnCooldown = new ConcurrentSkipListSet<>();

    public static boolean isPlayerOnCooldown(UUID playerId, boolean canBypass)
    {
        return playersOnCooldown.contains(playerId) && !canBypass && WonderTrade.config.cooldownEnabled;
    }

    public static void doWonderTrade(ServerPlayer player, Pokemon slot)
    {
        var canBypass = Cobblemon.INSTANCE.getPermissionValidator()
                                .hasPermission(player, new CobblemonPermission("wondertrade.command.trade.bypass",
                                                                               PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS));
        if(isPlayerOnCooldown(player.getUUID(), canBypass)) {
            player.sendSystemMessage(WonderTrade.config.messages.cooldownFeedback(player.registryAccess()));
            return;
        }
        if(isPokemonForbidden(slot) && !canBypass) {
            player.sendSystemMessage(WonderTrade.config.messages.pokemonNotAllowed(player.registryAccess()));
            return;
        }
        var playerParty = Cobblemon.INSTANCE.getStorage().getParty(player);
        var wonderPoke = WonderTrade.pool.pokemon.remove(rng.nextInt(WonderTrade.pool.pokemon.size()));
        var tookPoke = playerParty.remove(slot);
        var pokeAdded = playerParty.add(PokemonProperties.Companion.parse(wonderPoke).create());
        if(WonderTrade.config.adjustNewPokemonToLevelRange) {
            var level = slot.getLevel();
            if(level > WonderTrade.config.poolMaxLevel) {
                slot.setLevel(WonderTrade.config.poolMaxLevel);
            }
            else if (level < WonderTrade.config.poolMinLevel) {
                slot.setLevel(WonderTrade.config.poolMinLevel);
            }
        }
        WonderTrade.pool.pokemon.add(slot.createPokemonProperties(PokemonPropertyExtractor.ALL).asString(" "));
        WonderTrade.savePool();
        if(WonderTrade.config.cooldownEnabled && !canBypass) {
            playersOnCooldown.add(player.getUUID());
            WonderTrade.scheduler.schedule(() -> {playersOnCooldown.remove(player.getUUID());},
                                           WonderTrade.config.cooldown, TimeUnit.MINUTES);
        }
        player.sendSystemMessage(WonderTrade.config.messages.successFeedback(player.registryAccess()));
        var server = player.getServer();
        var broadcastMessage = WonderTrade.config.messages.broadcastPokemon(slot, player.registryAccess());
        if(server != null && !broadcastMessage.equals(Component.empty())) {
            server.getPlayerList().broadcastSystemMessage(broadcastMessage, false);
        }
    }

    public static boolean isPokemonForbidden(Pokemon pokemon) {
        for (String property : WonderTrade.config.blacklist) {
            if(PokemonProperties.Companion.parse(property).matches(pokemon)) {
                return true;
            }
        }
        return false;
    }
}
