package nl.theepicblock.shadowsgate.common;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandBuildContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Commands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandBuildContext buildContext, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("shadowsgate").requires(source -> source.hasPermissionLevel(2))
                .then(literal("debug")
                        .then(literal("currentid").executes(ctx -> {
                            var server = ctx.getSource().getServer();
                            var count = ShadowEntryCount.get(server.getOverworld().getPersistentStateManager());
                            ctx.getSource().sendFeedback(() -> Text.literal(Integer.toString(count.getCount())), false);
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(literal("contents").then(idArg().executes(ctx -> {
                            var server = ctx.getSource().getServer();
                            var entry = ShadowEntry.getExisting(server, IntegerArgumentType.getInteger(ctx, "id"));
                            if (entry == null) {
                                throw new SimpleCommandExceptionType(new LiteralMessage("Couldn't find entry with that id")).create();
                            }
                            var itemNbt = entry.getStack().writeNbt(new NbtCompound());
                            var nbtText = NbtHelper.toPrettyPrintedText(itemNbt);
                            ctx.getSource().sendFeedback(() -> nbtText, false);
                            return Command.SINGLE_SUCCESS;
                        })))
                        .then(literal("locations").then(idArg().executes(ctx -> {
                            var server = ctx.getSource().getServer();
                            var entry = ShadowEntry.getExisting(server, IntegerArgumentType.getInteger(ctx, "id"));
                            if (entry == null) {
                                throw new SimpleCommandExceptionType(new LiteralMessage("Couldn't find entry with that id")).create();
                            }
                            entry.getUsedPositions().forEach((dimension, positions) -> {
                                ctx.getSource().sendFeedback(() -> Text.literal("Dimension: "+dimension.getValue()), false);
                                positions.getPositions().forEach(pos -> {
                                    var cmd = "/execute in "+dimension.getValue()+" run tp @s "+pos.getX()+" "+pos.getY()+" "+pos.getZ();
                                    ctx.getSource().sendFeedback(() -> Text.literal("["+pos.toShortString()+"]")
                                            .styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd))), false);
                                });
                            });
                            return Command.SINGLE_SUCCESS;
                        })))));
    }

    private static RequiredArgumentBuilder<ServerCommandSource, Integer> idArg() {
        return argument("id", IntegerArgumentType.integer());
    }
}
