package io.github.polymeta.wondertrade.commands;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.permission.CobblemonPermission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.polymeta.wondertrade.WonderTrade;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class RegeneratePool {
    private static final Command<CommandSourceStack> Execute = (context) -> Regenerate(context, IntegerArgumentType.getInteger(context, "size"));

    private static final Command<CommandSourceStack> ExecuteDefault = (context) -> Regenerate(context, WonderTrade.config.poolSize);

    private static int Regenerate(CommandContext<CommandSourceStack> context, int size) {
        WonderTrade.regeneratePool(size);
        context.getSource().sendSystemMessage(Component.literal("WonderTrade pool regenerated!"));
        return size;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var regenCommand = dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("regenerate")
                        .requires(req -> Cobblemon.INSTANCE.getPermissionValidator().hasPermission(req,
                                new CobblemonPermission("wondertrade.command.regenerate", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)))
                        .then(Commands.argument("size", IntegerArgumentType.integer(1)).executes(Execute))
                        .executes(ExecuteDefault)
        );
        dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("regeneratepool").redirect(regenCommand));
    }
}
