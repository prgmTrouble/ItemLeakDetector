package com.prgmTrouble.item_leak_detector.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;
import static com.prgmTrouble.item_leak_detector.util.ItemLeakDetector.*;

public final class ItemLeakCommand
{
    private static int stopCMD(final CommandContext<ServerCommandSource> c)
    {
        stop(c.getSource().getWorld());
        return 1;
    }
    private static int start(final CommandContext<ServerCommandSource> c,
                             final int itemLifetime,final boolean probability,
                             final boolean uniform) throws CommandSyntaxException
    {
        if(BATCHES != 0) stopCMD(c);
        final BlockPos from,to;
        {
            final BlockPos a = BlockPosArgumentType.getBlockPos(c,"from"),
                           b = BlockPosArgumentType.getBlockPos(c,"to");
            from = new BlockPos(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()));
            to   = new BlockPos(Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ()));
        }
        TIME       =
        INTERVAL   = IntegerArgumentType.getInteger(c,"cooldown");
        BATCH_SIZE = IntegerArgumentType.getInteger(c,"batchSize");
        final int batches = IntegerArgumentType.getInteger(c,"batches");
        final double minAngle = DoubleArgumentType.getDouble(c,"minAngle"),
                     maxAngle = DoubleArgumentType.getDouble(c,"maxAngle"),
                     minSpeed = DoubleArgumentType.getDouble(c,"minSpeed"),
                     maxSpeed = DoubleArgumentType.getDouble(c,"maxSpeed");
        if(minAngle > maxAngle) thrw("minAngle("+minAngle+") > maxAngle("+maxAngle+")");
        if(minSpeed > maxSpeed) thrw("minSpeed("+minSpeed+") > maxSpeed("+maxSpeed+")");
        toPower(from,to,c.getSource().getWorld());
           LIFETIME = (short)itemLifetime;
              ANGLE =
        START_ANGLE = Math.toRadians(minAngle);
          END_ANGLE = Math.toRadians(maxAngle);
              SPEED =
        START_SPEED = minSpeed;
          END_SPEED = maxSpeed;
             RANDOM = !uniform;
        if(uniform)
        {
            final double sqrt = 2*Math.sqrt(BATCH_SIZE*batches);
            ANGLE_INC = (END_ANGLE-START_ANGLE)/sqrt;
            SPEED_INC = (END_SPEED-START_SPEED)/sqrt;
            // The assertion should always be true because the batch size and number of batches are at least 1 each.
            assert Double.isFinite(ANGLE_INC) && Double.isFinite(SPEED_INC);
        }
        QUIT_ON_FAIL = !probability;
        BATCHES = batches;
        return 1;
    }
    private static void thrw(final String msg) throws CommandSyntaxException
    {
        throw new SimpleCommandExceptionType(() -> msg).create();
    }
    private static int getAllowed(final CommandContext<ServerCommandSource> c,
                                  final String name,final String...allowed)
                                  throws CommandSyntaxException
    {
        final String str = StringArgumentType.getString(c,name);
        int i = 0;
        for(final String a : allowed)
        {
            if(str.equals(a)) break;
            ++i;
        }
        if(i == allowed.length) thrw("invalid value for '" + name + "' argument: '" + str + "'");
        return i;
    }
    private static int start(final CommandContext<ServerCommandSource> c,
                             final boolean probability,final boolean uniform)
                             throws CommandSyntaxException
    {
        return start(c,IntegerArgumentType.getInteger(c,"itemLifetime"),probability,uniform);
    }
    private static int start(final CommandContext<ServerCommandSource> c,final boolean uniform) throws CommandSyntaxException
    {
        return start(c,getAllowed(c,"mode","possibility","probability") == 1,uniform);
    }
    private static int start(final CommandContext<ServerCommandSource> c) throws CommandSyntaxException
    {
        return start(c,getAllowed(c,"algorithm","random","uniform") == 1);
    }
    public static void register(final CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register
        (
            literal("itemLeak").
                then
                (
                    literal("stop").
                        executes(ItemLeakCommand::stopCMD)
                ).
                then
                (
                    argument("from",BlockPosArgumentType.blockPos()).
                        then
                        (
                            argument("to",BlockPosArgumentType.blockPos()).
                                then
                                (
                                    argument("cooldown",IntegerArgumentType.integer(1)).
                                        suggests((c,b) -> suggestMatching(new String[] {"2","4","20"},b)).
                                        then
                                        (
                                            argument("batches",IntegerArgumentType.integer(1)).
                                                suggests((c,b) -> suggestMatching(new String[] {"100","500","1000"},b)).
                                                then
                                                (
                                                    argument("batchSize",IntegerArgumentType.integer(1)).
                                                        suggests((c,b) -> suggestMatching(new String[] {"32","64","128"},b)).
                                                        then
                                                        (
                                                            argument("minAngle",DoubleArgumentType.doubleArg(0,360)).
                                                                suggests((c,b) -> suggestMatching(new String[] {"0","90","180","270"},b)).
                                                                then
                                                                (
                                                                    argument("maxAngle",DoubleArgumentType.doubleArg(0,720)).
                                                                        suggests((c,b) -> suggestMatching(new String[] {"0","90","180","270","360","450","540","630","720"},b)).
                                                                        then
                                                                        (
                                                                            argument("minSpeed",DoubleArgumentType.doubleArg(0)).
                                                                                suggests((c,b) -> suggestMatching(new String[] {"0.1","0.5","1.0"},b)).
                                                                                then
                                                                                (
                                                                                    argument("maxSpeed",DoubleArgumentType.doubleArg(0)).
                                                                                        suggests((c,b) -> suggestMatching(new String[] {"0.1","0.5","1.0"},b)).
                                                                                        executes(c -> start(c,6000,false,false)).
                                                                                        then
                                                                                        (
                                                                                            argument("itemLifetime",IntegerArgumentType.integer(1,6000)).
                                                                                                suggests((c,b) -> suggestMatching(new String[] {"20","100","6000"},b)).
                                                                                                executes(c -> start(c,false,false)).
                                                                                                then
                                                                                                (
                                                                                                    argument("mode",StringArgumentType.word()).
                                                                                                        suggests((c,b) -> suggestMatching(new String[] {"possibility","probability"},b)).
                                                                                                        executes(c -> start(c,false)).
                                                                                                        then
                                                                                                        (
                                                                                                            argument("algorithm",StringArgumentType.word()).
                                                                                                                suggests((c,b) -> suggestMatching(new String[] {"random","uniform"},b)).
                                                                                                                executes(ItemLeakCommand::start)
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }
}