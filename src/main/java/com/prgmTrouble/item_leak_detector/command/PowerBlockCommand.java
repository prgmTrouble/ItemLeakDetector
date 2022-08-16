package com.prgmTrouble.item_leak_detector.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.prgmTrouble.item_leak_detector.util.ItemLeakDetector.SCHEDULED;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class PowerBlockCommand
{
    private static int power(final CommandContext<ServerCommandSource> c) throws CommandSyntaxException
    {
        final BlockPos pos = BlockPosArgumentType.getBlockPos(c,"pos");
        SCHEDULED.add(pos);
        final World w = c.getSource().getWorld();
        w.updateNeighborsAlways(pos,Blocks.RED_CONCRETE);
        SCHEDULED.remove(pos);
        w.updateNeighborsAlways(pos,Blocks.RED_CONCRETE);
        return 1;
    }
    public static void register(final CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register
        (
            literal("power").
                then
                (
                    argument("pos",BlockPosArgumentType.blockPos()).
                        executes(PowerBlockCommand::power)
                )
        );
    }
}