package io.github.polymeta.wondertrade.commands;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.permission.CobblemonPermission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;
import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.polymeta.wondertrade.WonderTrade;
import io.github.polymeta.wondertrade.gui.PoolGui;
import io.github.polymeta.wondertrade.gui.TradePartyGui;
import io.github.polymeta.wondertrade.util.TradeUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class Trade {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var tradeCommand = dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("wondertrade")
                        .requires(req -> Cobblemon.INSTANCE.getPermissionValidator().hasPermission(req,
                                new CobblemonPermission("wondertrade.command.trade.base", PermissionLevel.NONE)))
                        .then(Commands.argument("slot", PartySlotArgumentType.Companion.partySlot())
                                .then(Commands.argument("confirmation", StringArgumentType.greedyString()).executes(ExecuteWithConfirm))
                                .executes(Execute))
                        .then(Commands.literal("pool")
                                      .executes(ctx -> {
                                          if(WonderTrade.regenerating.get()) {
                                              ctx.getSource().sendSystemMessage(Component.literal("The WonderTrade pool is being regenerated!"));
                                              return Command.SINGLE_SUCCESS;
                                          }
                                          var player = ctx.getSource().getPlayerOrException();
                                          player.openMenu(new PoolGui(player, 0));
                                          return Command.SINGLE_SUCCESS;
                                      })
                        )
                        .executes(ctx ->  {
                            if(WonderTrade.regenerating.get()) {
                                ctx.getSource().sendSystemMessage(Component.literal("The WonderTrade pool is being regenerated!"));
                                return Command.SINGLE_SUCCESS;
                            }
                            var player = ctx.getSource().getPlayerOrException();
                            var canBypass = Cobblemon.INSTANCE.getPermissionValidator().hasPermission(player,
                                                                                                      new CobblemonPermission("wondertrade.command.trade.bypass",
                                                                                                                              PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS));
                            if(TradeUtil.isPlayerOnCooldown(player.getUUID(), canBypass)) {
                                player.sendSystemMessage(WonderTrade.config.messages.cooldownFeedback(player.registryAccess()));
                                return Command.SINGLE_SUCCESS;
                            }
                            player.openMenu(new TradePartyGui(player));
                            return 1;
                        })
        );
        dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("wt").redirect(tradeCommand));
    }

    private static final Command<CommandSourceStack> Execute = context -> {
        if(WonderTrade.regenerating.get()) {
            context.getSource().sendSystemMessage(Component.literal("The WonderTrade pool is being regenerated!"));
            return Command.SINGLE_SUCCESS;
        }
        var slot = PartySlotArgumentType.Companion.getPokemon(context, "slot");
        var player = context.getSource().getPlayerOrException();
        var slotNo = ((PartyPosition)slot.getStoreCoordinates().get().getPosition()).getSlot();
        var canBypass = Cobblemon.INSTANCE.getPermissionValidator().hasPermission(player,
                new CobblemonPermission("wondertrade.command.trade.bypass",
                        PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS));
        if(TradeUtil.isPlayerOnCooldown(player.getUUID(), canBypass)) {
            player.sendSystemMessage(WonderTrade.config.messages.cooldownFeedback(player.registryAccess()));
            return Command.SINGLE_SUCCESS;
        }
        if(TradeUtil.isPokemonForbidden(slot) && !canBypass) {
            player.sendSystemMessage(WonderTrade.config.messages.pokemonNotAllowed(player.registryAccess()));
            return Command.SINGLE_SUCCESS;
        }
        player.sendSystemMessage(WonderTrade.config.messages.wonderTradeFeedback(slot, slotNo, player.registryAccess()));

        return Command.SINGLE_SUCCESS;
    };

    private static final Command<CommandSourceStack> ExecuteWithConfirm = context -> {
        if(WonderTrade.regenerating.get()) {
            context.getSource().sendSystemMessage(Component.literal("The WonderTrade pool is being regenerated!"));
            return Command.SINGLE_SUCCESS;
        }
        var slot = PartySlotArgumentType.Companion.getPokemon(context, "slot");
        var confirmation = StringArgumentType.getString(context, "confirmation");
        if(!confirmation.trim().equals("--confirm")){
            return Execute.run(context);
        }
        var player = context.getSource().getPlayerOrException();
        TradeUtil.doWonderTrade(player, slot);
        return Command.SINGLE_SUCCESS;
    };
}
