package io.github.polymeta.wondertrade.commands;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.permission.CobblemonPermission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.polymeta.wondertrade.WonderTrade;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

public class Trade {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var tradeCommand = dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("wondertrade")
                        .requires(req -> Cobblemon.INSTANCE.getPermissionValidator().hasPermission(req,
                                new CobblemonPermission("wondertrade.command.trade.base", PermissionLevel.NONE)))
                        .then(Commands.argument("slot", PartySlotArgumentType.Companion.partySlot())
                                .then(Commands.argument("confirmation", StringArgumentType.greedyString()).executes(ExecuteWithConfirm))
                                .executes(Execute))
        );
        dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("wt").redirect(tradeCommand));
    }

    private static final Random rng = new Random();

    private static final ConcurrentSkipListSet<UUID> playersOnCooldown = new ConcurrentSkipListSet<>();

    private static final Command<CommandSourceStack> Execute = context -> {
        var slot = PartySlotArgumentType.Companion.getPokemon(context, "slot");
        var player = context.getSource().getPlayerOrException();
        var slotNo = ((PartyPosition)slot.getStoreCoordinates().get().getPosition()).getSlot();
        var canBypass = Cobblemon.INSTANCE.getPermissionValidator().hasPermission(player,
                new CobblemonPermission("wondertrade.command.trade.bypass",
                        PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS));
        if(playersOnCooldown.contains(player.getUUID()) && !canBypass && WonderTrade.config.cooldownEnabled) {
            player.sendSystemMessage(WonderTrade.config.messages.cooldownFeedback(player.registryAccess()));
            return Command.SINGLE_SUCCESS;
        }
        if(isPokemonForbidden(slot) && !canBypass) {
            player.sendSystemMessage(WonderTrade.config.messages.pokemonNotAllowed(player.registryAccess()));
            return Command.SINGLE_SUCCESS;
        }
        player.sendSystemMessage(WonderTrade.config.messages.wonderTradeFeedback(slot, slotNo, player.registryAccess()));

        return Command.SINGLE_SUCCESS;
    };

    private static final Command<CommandSourceStack> ExecuteWithConfirm = context -> {
        var slot = PartySlotArgumentType.Companion.getPokemon(context, "slot");
        var confirmation = StringArgumentType.getString(context, "confirmation");
        if(!confirmation.trim().equals("--confirm")){
            return Execute.run(context);
        }
        var player = context.getSource().getPlayerOrException();
        var canBypass = Cobblemon.INSTANCE.getPermissionValidator().hasPermission(player,
                new CobblemonPermission("wondertrade.command.trade.bypass",
                        PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS));
        if(playersOnCooldown.contains(player.getUUID()) && !canBypass && WonderTrade.config.cooldownEnabled) {
            player.sendSystemMessage(WonderTrade.config.messages.cooldownFeedback(player.registryAccess()));
            return Command.SINGLE_SUCCESS;
        }
        if(isPokemonForbidden(slot) && !canBypass) {
            player.sendSystemMessage(WonderTrade.config.messages.pokemonNotAllowed(player.registryAccess()));
            return Command.SINGLE_SUCCESS;
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
            WonderTrade.scheduler.schedule(() -> {playersOnCooldown.remove(player.getUUID());}, WonderTrade.config.cooldown, TimeUnit.MINUTES);
        }
        player.sendSystemMessage(WonderTrade.config.messages.successFeedback(player.registryAccess()));
        var server = player.getServer();
        var broadcastMessage = WonderTrade.config.messages.broadcastPokemon(slot, player.registryAccess());
        if(server != null && !broadcastMessage.equals(Component.empty())) {
            server.getPlayerList().broadcastSystemMessage(broadcastMessage, false);
        }
        return Command.SINGLE_SUCCESS;
    };

    private static boolean isPokemonForbidden(Pokemon pokemon) {
        for (String property : WonderTrade.config.blacklist) {
            if(PokemonProperties.Companion.parse(property).matches(pokemon)) {
                return true;
            }
        }
        return false;
    }

}
