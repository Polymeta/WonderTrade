package io.github.polymeta.wondertrade.commands;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.permission.CobblemonPermission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.polymeta.wondertrade.WonderTrade;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

public class Trade {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("wondertrade")
                        .requires(req -> Cobblemon.INSTANCE.getPermissionValidator().hasPermission(req,
                                new CobblemonPermission("wondertrade.command.trade.base", PermissionLevel.NONE)))
                        .then(Commands.argument("slot", PartySlotArgumentType.Companion.partySlot())
                                .then(Commands.argument("confirmation", StringArgumentType.greedyString()).executes(ExecuteWithConfirm))
                                .executes(Execute))
        );
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
            player.sendSystemMessage(WonderTrade.PREFIX.copy().append(Component.literal("You are on cooldown!").withStyle(ChatFormatting.RED)));
            return Command.SINGLE_SUCCESS;
        }

        var text = WonderTrade.PREFIX.copy().append(Component
                .literal("Are you sure you want to trade your").withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" lvl: " + slot.getLevel() + " ").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)))
                .append(slot.getDisplayName().withStyle(ChatFormatting.AQUA))
                .append(Component.literal("? ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("Click here to confirm!").setStyle(
                        Style.EMPTY.withColor(ChatFormatting.YELLOW)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wondertrade " + (slotNo + 1) + " --confirm")))));
        player.sendSystemMessage(text);

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
            player.sendSystemMessage(WonderTrade.PREFIX.copy().append(Component.literal("You are on cooldown!").withStyle(ChatFormatting.RED)));
            return Command.SINGLE_SUCCESS;
        }
        var playerParty = Cobblemon.INSTANCE.getStorage().getParty(player);
        var wonderPoke = WonderTrade.pool.pokemon.remove(rng.nextInt(WonderTrade.pool.pokemon.size()));
        var tookPoke = playerParty.remove(slot);
        var pokeAdded = playerParty.add(PokemonProperties.Companion.parse(wonderPoke, " ", "=").create());
        WonderTrade.pool.pokemon.add(slot.createPokemonProperties(PokemonPropertyExtractor.Companion.getALL()).asString(" "));
        WonderTrade.savePool();
        if(WonderTrade.config.cooldownEnabled && !canBypass) {
            playersOnCooldown.add(player.getUUID());
            WonderTrade.scheduler.schedule(() -> {playersOnCooldown.remove(player.getUUID());}, WonderTrade.config.cooldown, TimeUnit.MINUTES);
        }
        player.sendSystemMessage(WonderTrade.PREFIX.copy().append(Component.literal("Successfully traded!").withStyle(ChatFormatting.GREEN)));
        return Command.SINGLE_SUCCESS;
    };

}
