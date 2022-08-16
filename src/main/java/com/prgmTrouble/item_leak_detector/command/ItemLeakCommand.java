package com.prgmTrouble.item_leak_detector.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Iterator;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.prgmTrouble.item_leak_detector.util.Broadcast.*;
import static com.prgmTrouble.item_leak_detector.util.ItemLeakDetector.*;
import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.command.argument.BlockPosArgumentType.blockPos;
import static net.minecraft.command.argument.BlockPosArgumentType.getBlockPos;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ItemLeakCommand
{
    private static int stopCMD(final CommandContext<ServerCommandSource> c)
    {
        stop(c.getSource().getWorld());
        return 1;
    }
    private static final HoverEvent CLICK_TO_FIX = new HoverEvent(HoverEvent.Action.SHOW_TEXT,asTxt("Click for template command"));
    private static ClickEvent cmd(final String cmd)
    {
        return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/itemLeak "+cmd+' ');
    }
    private static boolean err(final CommandContext<ServerCommandSource> c,final String errMsg,final String cmd)
    {
        c.getSource().sendError(asErr(errMsg).fillStyle(Style.EMPTY.withHoverEvent(CLICK_TO_FIX).withClickEvent(cmd(cmd))));
        return true;
    }
    private static boolean testActive(final CommandContext<ServerCommandSource> c)
    {
        if(leakDetectorActive())
        {
            err(c,"Leak detector is still running","stop");
            return false;
        }
        return true;
    }
    private static int start(final CommandContext<ServerCommandSource> c)
    {
        if(testActive(c))
        {
            boolean fail = false;
            if(BATCHES == 0)
                fail = err(c,"Unspecified number of batches.","batches");
            if(BATCH_SIZE == 0)
                fail = err(c,"Unspecified batch size.","batchSize");
            if(DATA[POS_X][START] > DATA[POS_X][END])
                fail = err(c,"Minimum X-Pos (" + DATA[POS_X][START] + ") > Maximum X-Pos (" + DATA[POS_X][END] + ").","maxXPos");
            if(DATA[POS_Y][START] > DATA[POS_Y][END])
                fail = err(c,"Minimum Y-Pos (" + DATA[POS_Y][START] + ") > Maximum Y-Pos (" + DATA[POS_Y][END] + ").","maxYPos");
            if(DATA[POS_Z][START] > DATA[POS_Z][END])
                fail = err(c,"Minimum Z-Pos (" + DATA[POS_Z][START] + ") > Maximum Z-Pos (" + DATA[POS_Z][END] + ").","maxZPos");
            if(DATA[VEL_X][START] > DATA[VEL_X][END])
                fail = err(c,"Minimum X-Vel (" + DATA[VEL_X][START] + ") > Maximum X-Vel (" + DATA[VEL_X][END] + ").","maxXVel");
            if(DATA[VEL_Y][START] > DATA[VEL_Y][END])
                fail = err(c,"Minimum Y-Vel (" + DATA[VEL_Y][START] + ") > Maximum Y-Vel (" + DATA[VEL_Y][END] + ").","maxYVel");
            if(DATA[VEL_Z][START] > DATA[VEL_Z][END])
                fail = err(c,"Minimum Z-Vel (" + DATA[VEL_Z][START] + ") > Maximum Z-Vel (" + DATA[VEL_Z][END] + ").","maxZVel");
            if(fail) return 0;
            if(!RANDOM)
            {
                // Determine which and how many dimensions aren't empty.
                byte loadedAxes = 0, axesMask = 0;
                for(final double[] d : DATA)
                {
                    axesMask >>>= 1;
                    if((d[VALUE] = d[START]) != d[END])
                    {
                        ++loadedAxes;
                        axesMask |= 1 << DATA.length;
                    }
                }
                // Evenly divide the domain of all items across the non-empty axes.
                if(loadedAxes == 0) for(final double[] d : DATA) d[INC] = 0;
                else
                {
                    final double incCoeff = Math.pow((long)BATCH_SIZE * (long)BATCHES,-1. / loadedAxes);
                    for(final double[] d : DATA)
                    {
                        d[INC] = (axesMask & 1) != 0? incCoeff * (d[START] - d[END]) : 0;
                        axesMask >>>= 1;
                    }
                }
            }
            TIME = INTERVAL;
            ACTIVE = true;
        }
        return 1;
    }
    private static final HoverEvent CLICK_TO_COPY = new HoverEvent(HoverEvent.Action.SHOW_TEXT,asTxt("Click to copy"));
    private static MutableText vec(final int x,final int y,final int z)
    {
        return asTxt('[').append(asNum(x)).append(asTxt(',')).append(asNum(y)).append(asTxt(',')).append(asNum(z)).append(asTxt(']')).
               fillStyle
               (
                   Style.EMPTY.
                       withClickEvent
                       (
                           new ClickEvent
                           (
                               ClickEvent.Action.SUGGEST_COMMAND,
                               "["+x+','+y+','+z+']'
                           )
                       ).
                       withHoverEvent(CLICK_TO_COPY)
               );
    }
    public static MutableText vec(final double x,final double y,final double z)
    {
        return asTxt('[').append(asNum(x)).append(asTxt(',')).append(asNum(y)).append(asTxt(',')).append(asNum(z)).append(asTxt(']')).
               fillStyle
               (
                   Style.EMPTY.
                       withClickEvent
                       (
                           new ClickEvent
                           (
                               ClickEvent.Action.SUGGEST_COMMAND,
                               "["+x+','+y+','+z+']'
                           )
                       ).
                       withHoverEvent(CLICK_TO_COPY)
               );
    }
    private static MutableText vec(final double a,final double b)
    {
        return asTxt('[').append(asNum(a)).append(asTxt(',')).append(asNum(b)).append(asTxt(']')).
               fillStyle
               (
                   Style.EMPTY.
                       withClickEvent
                       (
                           new ClickEvent
                           (
                               ClickEvent.Action.SUGGEST_COMMAND,
                               "["+a+','+b+']'
                           )
                       ).
                       withHoverEvent(CLICK_TO_COPY)
               );
    }
    private static MutableText vec(final BlockPos p) {return vec(p.getX(),p.getY(),p.getZ());}
    private static MutableText num(final int v)
    {
        return asNum(v).fillStyle
        (
            Style.EMPTY.
                withClickEvent
                (
                    new ClickEvent
                    (
                        ClickEvent.Action.SUGGEST_COMMAND,
                        Integer.toString(v)
                    )
                ).
                withHoverEvent(CLICK_TO_COPY)
        );
    }
    private static MutableText num(final double v)
    {
        return asNum(v).fillStyle
        (
            Style.EMPTY.
                withClickEvent
                (
                    new ClickEvent
                    (
                        ClickEvent.Action.SUGGEST_COMMAND,
                        Double.toString(v)
                    )
                ).
                withHoverEvent(CLICK_TO_COPY)
        );
    }
    private static int printPower(final CommandContext<ServerCommandSource> c)
    {
        final Iterator<BlockPos> p = POWER.iterator();
        if(!p.hasNext()) c.getSource().sendFeedback(asTxt("No blocks specified."),false);
        final MutableText t = asTxt('[');
        t.append(vec(p.next()));
        while(p.hasNext()) t.append(asTxt(',')).append(vec(p.next()));
        c.getSource().sendFeedback(t.append(asTxt(']')),false);
        return 1;
    }
    private static int clearPower(final CommandContext<ServerCommandSource> c)
    {
        if(testActive(c))
        {
            UPDATE.clear();
            POWER.clear();
            c.getSource().sendFeedback(asTxt("Cleared blocks"),false);
            return 1;
        }
        return 0;
    }
    private static int setPower(final CommandContext<ServerCommandSource> c) throws CommandSyntaxException
    {
        if(testActive(c))
        {
            final BlockPos p = getBlockPos(c,"block");
            if(POWER.add(p))
            {
                for(final Direction d : Direction.values())
                    UPDATE.add(p.offset(d));
                c.getSource().sendFeedback(asTxt("Added ").append(vec(p)),false);
            }
            else c.getSource().sendFeedback(asTxt("Position ").append(vec(p)).append(" was already added"),false);
            return 1;
        }
        return 0;
    }
    private static int setPowerVolume(final CommandContext<ServerCommandSource> c) throws CommandSyntaxException
    {
        if(testActive(c))
        {
            final BlockPos f,t;
            {
                final BlockPos a = getBlockPos(c,"from"),b = getBlockPos(c,"to");
                f = new BlockPos(Math.min(a.getX(),b.getX()),
                                 Math.min(a.getY(),b.getY()),
                                 Math.min(a.getZ(),b.getZ()));
                t = new BlockPos(Math.max(a.getX(),b.getX()),
                                 Math.max(a.getY(),b.getY()),
                                 Math.max(a.getZ(),b.getZ()));
            }
            final MutableText out = asTxt("Added: ");
            boolean first = true;
            final World w = c.getSource().getWorld();
            for(int x = f.getX();x <= t.getX();++x)
                for(int y = f.getY();y <= t.getY();++y)
                    for(int z = f.getZ();z <= t.getZ();++z)
                    {
                        final BlockPos p = new BlockPos(x,y,z);
                        if(w.getBlockState(p).getBlock() == Blocks.RED_CONCRETE)
                        {
                            POWER.add(p);
                            if(first) first = false;
                            else out.append(asTxt(','));
                            out.append(vec(p));
                            for(final Direction d : Direction.values())
                                UPDATE.add(p.offset(d));
                        }
                    }
            if(first) out.append(asTxt("none"));
            c.getSource().sendFeedback(out,false);
            return 1;
        }
        return 0;
    }
    private static int printCooldown(final CommandContext<ServerCommandSource> c)
    {
        c.getSource().sendFeedback(asTxt("Current cooldown: ").append(num(INTERVAL)),false);
        return 1;
    }
    private static final String[] cooldownSuggestions = {"2","4","20"};
    private static int setCooldown(final CommandContext<ServerCommandSource> c)
    {
        if(testActive(c))
        {
            c.getSource().sendFeedback(asTxt("Set cooldown to ").append(num(INTERVAL = getInteger(c,"cooldown"))),false);
            return 1;
        }
        return 0;
    }
    private static int printBatches(final CommandContext<ServerCommandSource> c)
    {
        c.getSource().sendFeedback(asTxt("Current number of batches: ").append(num(BATCHES)),false);
        return 1;
    }
    private static final String[] batchesSuggestions = {"100","500","1000"};
    private static int setBatches(final CommandContext<ServerCommandSource> c)
    {
        if(testActive(c))
        {
            c.getSource().sendFeedback(asTxt("Set number of batches to ").append(num(BATCHES = getInteger(c,"batches"))),false);
            return 1;
        }
        return 0;
    }
    private static int printBatchSize(final CommandContext<ServerCommandSource> c)
    {
        c.getSource().sendFeedback(asTxt("Current batch size: ").append(num(BATCH_SIZE)),false);
        return 1;
    }
    private static final String[] batchSizeSuggestions = {"32","64","128"};
    private static int setBatchSize(final CommandContext<ServerCommandSource> c)
    {
        if(testActive(c))
        {
            c.getSource().sendFeedback(asTxt("Batch size set to ").append(num(BATCH_SIZE = getInteger(c,"batchSize"))),false);
            return 1;
        }
        return 0;
    }
    private static void single(final CommandContext<ServerCommandSource> c,final boolean set,
                               final char axis,final boolean pos,final boolean min)
    {
        if(!set || testActive(c))
        {
            final double[] d = DATA[POS_X+(axis-'X')+(pos? 0:3)];
            final String m = (min? "in":"ax"),
                         s = m+"imum "+axis+" "+(pos? "position":"velocity")+" modifier";
            c.getSource().sendFeedback
            (
                set
                    ? asTxt('M'+s+" set to ").
                          append(num(d[min? START:END] = getDouble(c,'m'+m+axis+(pos? "Pos":"Vel"))))
                    : asTxt("Current m"+s+": ").
                          append(num(d[min? START:END]))
                ,
                false
            );
        }
    }
    private static void pair(final CommandContext<ServerCommandSource> c,final boolean set,
                             final char axis,final boolean pos)
    {
        if(!set || testActive(c))
        {
            final double[] d = DATA[POS_X+(axis-'X')+(pos? 0:3)];
            final String s = axis+" "+(pos? "position":"velocity")+" modifier range";
            c.getSource().sendFeedback
            (
                set
                    ? asTxt(s+" set to ").
                          append(vec(d[START] = getDouble(c,"min"),d[END] = getDouble(c,"max")))
                    : asTxt("Current "+s+':').
                          append(vec(d[START],d[END])),
                false
            );
        }
    }
    private static void triple(final CommandContext<ServerCommandSource> c,final boolean set,final boolean pos)
    {
        if(!set || testActive(c))
        {
            final byte offset = pos? POS_X:VEL_X;
            c.getSource().sendFeedback
            (
                set
                    ? asTxt((pos? "Position":"Velocity")+" modifiers set to").
                          append(asTxt(" x:")).append(vec(DATA[offset  ][START] = getDouble(c,"minX"),DATA[offset  ][END] = getDouble(c,"maxX"))).
                          append(asTxt(" y:")).append(vec(DATA[offset+1][START] = getDouble(c,"minY"),DATA[offset+1][END] = getDouble(c,"maxY"))).
                          append(asTxt(" z:")).append(vec(DATA[offset+2][START] = getDouble(c,"minZ"),DATA[offset+2][END] = getDouble(c,"maxZ")))
                    : asTxt("Current "+(pos? "position":"velocity")+" modifiers: ").
                          append(asTxt(" x:")).append(vec(DATA[offset  ][START],DATA[offset  ][END])).
                          append(asTxt(" y:")).append(vec(DATA[offset+1][START],DATA[offset+1][END])).
                          append(asTxt(" z:")).append(vec(DATA[offset+2][START],DATA[offset+2][END])),
                false
            );
        }
    }
    private static int printMinXPos(final CommandContext<ServerCommandSource> c)
    {
        single(c,false,'X',true,true);
        return 1;
    }
    private static int setMinXPos(final CommandContext<ServerCommandSource> c)
    {
        single(c,true,'X',true,true);
        return 1;
    }
    private static int printMinYPos(final CommandContext<ServerCommandSource> c)
    {
        single(c,false,'Y',true,true);
        return 1;
    }
    private static int setMinYPos(final CommandContext<ServerCommandSource> c)
    {
        single(c,true,'Y',true,true);
        return 1;
    }
    private static int printMinZPos(final CommandContext<ServerCommandSource> c)
    {
        single(c,false,'Z',true,true);
        return 1;
    }
    private static int setMinZPos(final CommandContext<ServerCommandSource> c)
    {
        single(c,true,'Z',true,true);
        return 1;
    }
    private static int printMaxXPos(final CommandContext<ServerCommandSource> c)
    {
        single(c,false,'X',true,false);
        return 1;
    }
    private static int setMaxXPos(final CommandContext<ServerCommandSource> c)
    {
        single(c,true,'X',true,false);
        return 1;
    }
    private static int printMaxYPos(final CommandContext<ServerCommandSource> c)
    {
        single(c,false,'Y',true,false);
        return 1;
    }
    private static int setMaxYPos(final CommandContext<ServerCommandSource> c)
    {
        single(c,true,'Y',true,false);
        return 1;
    }
    private static int printMaxZPos(final CommandContext<ServerCommandSource> c)
    {
        single(c,false,'Z',true,false);
        return 1;
    }
    private static int setMaxZPos(final CommandContext<ServerCommandSource> c)
    {
        single(c,true,'Z',true,false);
        return 1;
    }
    private static int printXPos(final CommandContext<ServerCommandSource> c)
    {
        pair(c,false,'X',true);
        return 1;
    }
    private static int setXPos(final CommandContext<ServerCommandSource> c)
    {
        pair(c,true,'X',true);
        return 1;
    }
    private static int printYPos(final CommandContext<ServerCommandSource> c)
    {
        pair(c,false,'Y',true);
        return 1;
    }
    private static int setYPos(final CommandContext<ServerCommandSource> c)
    {
        pair(c,true,'Y',true);
        return 1;
    }
    private static int printZPos(final CommandContext<ServerCommandSource> c)
    {
        pair(c,false,'Z',true);
        return 1;
    }
    private static int setZPos(final CommandContext<ServerCommandSource> c)
    {
        pair(c,true,'Z',true);
        return 1;
    }
    private static int printPos(final CommandContext<ServerCommandSource> c)
    {
        triple(c,false,true);
        return 1;
    }
    private static int setPos(final CommandContext<ServerCommandSource> c)
    {
        triple(c,true,true);
        return 1;
    }
    private static int printMinXVel(final CommandContext<ServerCommandSource> c)
    {
        single(c,false,'X',false,true);
        return 1;
    }
    private static int setMinXVel(final CommandContext<ServerCommandSource> c)
    {
        single(c,true,'X',false,true);
        return 1;
    }
    private static int printMinYVel(final CommandContext<ServerCommandSource> c)
    {
        single(c,false,'Y',false,true);
        return 1;
    }
    private static int setMinYVel(final CommandContext<ServerCommandSource> c)
    {
        single(c,true,'Y',false,true);
        return 1;
    }
    private static int printMinZVel(final CommandContext<ServerCommandSource> c)
    {
        single(c,false,'Z',false,true);
        return 1;
    }
    private static int setMinZVel(final CommandContext<ServerCommandSource> c)
    {
        single(c,true,'Z',false,true);
        return 1;
    }
    private static int printMaxXVel(final CommandContext<ServerCommandSource> c)
    {
        single(c,false,'X',false,false);
        return 1;
    }
    private static int setMaxXVel(final CommandContext<ServerCommandSource> c)
    {
        single(c,true,'X',false,false);
        return 1;
    }
    private static int printMaxYVel(final CommandContext<ServerCommandSource> c)
    {
        single(c,false,'Y',false,false);
        return 1;
    }
    private static int setMaxYVel(final CommandContext<ServerCommandSource> c)
    {
        single(c,true,'Y',false,false);
        return 1;
    }
    private static int printMaxZVel(final CommandContext<ServerCommandSource> c)
    {
        single(c,false,'Z',false,false);
        return 1;
    }
    private static int setMaxZVel(final CommandContext<ServerCommandSource> c)
    {
        single(c,true,'Z',false,false);
        return 1;
    }
    private static int printXVel(final CommandContext<ServerCommandSource> c)
    {
        pair(c,false,'X',false);
        return 1;
    }
    private static int setXVel(final CommandContext<ServerCommandSource> c)
    {
        pair(c,true,'X',false);
        return 1;
    }
    private static int printYVel(final CommandContext<ServerCommandSource> c)
    {
        pair(c,false,'Y',false);
        return 1;
    }
    private static int setYVel(final CommandContext<ServerCommandSource> c)
    {
        pair(c,true,'Y',false);
        return 1;
    }
    private static int printZVel(final CommandContext<ServerCommandSource> c)
    {
        pair(c,false,'Z',false);
        return 1;
    }
    private static int setZVel(final CommandContext<ServerCommandSource> c)
    {
        pair(c,true,'Z',false);
        return 1;
    }
    private static int printVel(final CommandContext<ServerCommandSource> c)
    {
        triple(c,false,false);
        return 1;
    }
    private static int setVel(final CommandContext<ServerCommandSource> c)
    {
        triple(c,true,false);
        return 1;
    }
    private static final String[] scenarios =
    {
        "barrel_destroyed","beehive","boat_drop_boat","boat_drop_materials","brewing_extra_dragons_breath",
        "brewing_stand_destroyed","cat_morning_gifts","campfire_cooking","chest_destroyed","chicken_lay_egg",
        "composter_full","creeper_drop_head","dispenser_fired","dropper_fired","dispenser_destroyed",
        "dropper_destroyed","dolphin","donkey_drop_chest","enderman_drop_held_block","entity_detach_leash",
        "eye_of_ender_drop","falling_block_pop","farmer_drop_bread","fishing_bobber_catch","fox_drop",
        "fox_spit","hoe_on_rooted_dirt_axis-x","hoe_on_rooted_dirt_axis-y","hoe_on_rooted_dirt_axis-z",
        "hopper_destroyed","horse_drop_inventory","item_frame","item_with_inventory_destroyed","jukebox",
        "leash_knot","leashed_mob_loaded_without_holder","lectern_drop_book","loot_command",
        "minecart_destroyed_inventory","minecart_drop_chest","minecart_drop_furnace","minecart_drop_hopper",
        "minecart_drop_minecart","minecart_drop_tnt","mob_drop_equipment","mob_drop_inventory","mob_drop_loot",
        "mooshroom_sheared","painting","panda_sneeze","panda_bamboo","pickup_arrow","pickup_trident",
        "pig_drop_saddle","piglin_barter","piglin_zombify","piston","player_breaks_shulker_box",
        "player_drop_inventory","player_drop_item","pumpkin_sheared","raid_hero","raider_drop_banner",
        "shear_sheep","shear_snow_golem","skeleton_drop_head","strider_drop_saddle","test_context","turtle_scute",
        "villager_gather_items","wither_skeleton_drop_head","wither_rose","wither_star","zombie_conversion",
        "zombie_drop_head"
    };
    private static int scenario(final CommandContext<ServerCommandSource> c)
    {
        if(testActive(c))
        {
            final String s = getString(c,"scenario");
            final double[] data = switch(s)
            {
                case "beehive",
                     "boat_drop_boat",
                     "boat_drop_materials",
                     "cat_morning_gifts",
                     "chicken_lay_egg",
                     "creeper_drop_head",
                     "dolphin",
                     "donkey_drop_chest",
                     "enderman_drop_held_block",
                     "entity_detach_leash",
                     "eye_of_ender_drop",
                     "falling_block_pop",
                     "fox_drop",
                     "fox_spit",
                     "furnace_destroyed",
                     "horse_drop_inventory",
                     "item_frame",
                     "item_with_inventory_destroyed",
                     "leash_knot",
                     "leashed_mob_loaded_without_holder",
                     "lectern_drop_book",
                     "loot_command",
                     "minecart_drop_chest",
                     "minecart_drop_furnace",
                     "minecart_drop_hopper",
                     "minecart_drop_minecart",
                     "minecart_drop_tnt",
                     "mob_drop_equipment",
                     "mob_drop_inventory",
                     "mob_drop_loot",
                     "mooshroom_sheared",
                     "painting",
                     "panda_sneeze",
                     "panda_bamboo",
                     "pickup_arrow",
                     "pickup_trident",
                     "pig_drop_saddle",
                     "piglin_barter",
                     "piglin_zombify",
                     "player_breaks_shulker_box",
                     "raid_hero",
                     "raider_drop_banner",
                     "shear_snow_golem",
                     "skeleton_drop_head",
                     "strider_drop_saddle",
                     "turtle_scute",
                     "villager_gather_items",
                     "wither_skeleton_drop_head",
                     "wither_rose",
                     "wither_star",
                     "zombie_conversion",
                     "zombie_drop_head"
                    -> new double[] { 0.00, 0.00,  0.00, 0.00,  0.00, 0.00, -0.20, 0.20,  0.00, 0.00, -0.20, 0.20};
                case "barrel_destroyed","brewing_extra_dragons_breath",
                     "brewing_stand_destroyed","campfire_cooking",
                     "chest_destroyed","dispenser_destroyed",
                     "dropper_destroyed","hopper_destroyed",
                     "minecart_destroyed_inventory"
                    -> new double[] {-0.75, 0.75, -0.75, 0.75, -0.75, 0.75,
                                     -Double.MAX_VALUE,Double.MAX_VALUE,
                                     -Double.MAX_VALUE,Double.MAX_VALUE,
                                     -Double.MAX_VALUE,Double.MAX_VALUE};
                case "composter_full","jukebox"
                    -> new double[] { 0.00, 0.7F,  0.00, 0.7F,  0.00, 0.7F, -0.20, 0.20,  0.00, 0.00, -0.20, 0.20};
                case "dispenser_fired","dropper_fired"
                    -> new double[] { 0.00, 0.00,  0.00, 0.00,  0.00, 0.00,
                                     -Double.MAX_VALUE,Double.MAX_VALUE,
                                     -Double.MAX_VALUE,Double.MAX_VALUE,
                                     -Double.MAX_VALUE,Double.MAX_VALUE};
                case "fishing_bobber_catch","test_context"
                    -> new double[] { 0.00, 0.00,  0.00, 0.00,  0.00, 0.00,  0.00, 0.00,  0.00, 0.00,  0.00, 0.00};
                case "hoe_on_rooted_dirt_axis-x"
                    -> new double[] { 0.00, 0.00, -0.25, 0.25, -0.25, 0.25,  0.00, 0.00,  0.00, 0.10, -0.10, 0.10};
                case "hoe_on_rooted_dirt_axis-y"
                    -> new double[] {-0.25, 0.25,  0.00, 0.00, -0.25, 0.25, -0.10, 0.10,  0.00, 0.00, -0.10, 0.10};
                case "hoe_on_rooted_dirt_axis-z"
                    -> new double[] {-0.25, 0.25, -0.25, 0.25,  0.00, 0.00, -0.10, 0.10,  0.00, 0.10,  0.00, 0.00};
                case "piston"
                    -> new double[] {-0.25, 0.25, -0.25, 0.25, -0.25, 0.25, -0.20, 0.20,  0.00, 0.00, -0.20, 0.20};
                case "player_drop_inventory"
                    -> new double[] { 0.00, 0.00,  0.00, 0.00,  0.00, 0.00, -0.5F, 0.5F,  0.00, 0.00, -0.5F, 0.5F};
                case "player_drop_item"
                    -> new double[] { 0.00, 0.00,  0.00, 0.00,  0.00, 0.00, -.02F, .02F, -0.1F, 0.1F, -.02F, .02F};
                case "pumpkin_sheared"
                    -> new double[] { 0.00, 0.00,  0.00, 0.00,  0.00, 0.00,  0.00, 0.02,  0.00, 0.00,  0.00, 0.02};
                case "shear_sheep"
                    -> new double[] { 0.00, 0.1F,  0.00, 0.05F, 0.00, 0.1F, -0.20, 0.20,  0.00, 0.00, -0.20, 0.20};
                default
                    -> throw new IllegalArgumentException("Unknown preset '"+s+"'");
            };
            int i = 0;
            for(final double[] d : DATA)
            {
                d[START] = data[i++];
                d[ END ] = data[i++];
            }
            final MutableText warning = switch(s)
            {
                case "barrel_destroyed","brewing_extra_dragons_breath","brewing_stand_destroyed","chest_destroyed",
                     "dispenser_fired","dropper_fired","dispenser_destroyed","dropper_destroyed","hopper_destroyed",
                     "minecart_destroyed_inventory"
                    -> asWarn("Uncapped Gaussian Distribution: The resulting velocity can be any non-NAN 64 bit float value.");
                case "composter_full","dolphin","jukebox","player_drop_inventory","player_drop_item","shear_sheep"
                    -> asWarn("Inconsistent Float Precision: The values in vanilla are converted from 32 bit to 64-bit "+
                              "precision, meaning that some rare values generated by this command may not be possible in " +
                              "vanilla.");
                default
                    -> null;
            };
            if(warning != null) c.getSource().sendFeedback(warning,false);
            c.getSource().sendFeedback(asTxt("Scenario '"+s+"' successfully loaded."),false);
            return 1;
        }
        return 0;
    }
    private static int printItemLifetime(final CommandContext<ServerCommandSource> c)
    {
        c.getSource().sendFeedback(asTxt("Current item lifetime: ").append(num(LIFETIME)),false);
        return 1;
    }
    private static final String[] itemLifetimeSuggestions = {"20","80","6000"};
    private static int setItemLifetime(final CommandContext<ServerCommandSource> c)
    {
        if(testActive(c))
        {
            c.getSource().sendFeedback(asTxt("Item lifetime set to ").append(num(LIFETIME = (short)getInteger(c,"lifetime"))),false);
            return 1;
        }
        return 0;
    }
    private static int printMode(final CommandContext<ServerCommandSource> c)
    {
        c.getSource().sendFeedback(asTxt("Current mode: "+(QUIT_ON_FAIL? "possibility":"probability")),false);
        return 1;
    }
    private static final String[] modes = {"possibility","probability"};
    private static int setMode(final CommandContext<ServerCommandSource> c)
    {
        if(testActive(c))
        {
            final String s = getString(c,"mode");
            QUIT_ON_FAIL = switch(s)
            {
                case "possibility" -> true;
                case "probability" -> false;
                default -> throw new IllegalArgumentException("Invalid mode: '"+s+'\'');
            };
            c.getSource().sendFeedback(asTxt("Mode set to "+s),false);
            return 1;
        }
        return 0;
    }
    private static int printAlgorithm(final CommandContext<ServerCommandSource> c)
    {
        c.getSource().sendFeedback(asTxt("Current algorithm: "+(RANDOM? "random":"uniform")),false);
        return 1;
    }
    private static final String[] algorithms = {"random","uniform"};
    private static int setAlgorithm(final CommandContext<ServerCommandSource> c)
    {
        if(testActive(c))
        {
            final String s = getString(c,"algorithm");
            RANDOM = switch(s)
            {
                case "random" -> true;
                case "uniform" -> false;
                default -> throw new IllegalArgumentException("Invalid algorithm: '"+s+'\'');
            };
            c.getSource().sendFeedback(asTxt("Current algorithm: "+s),false);
            return 1;
        }
        return 0;
    }
    private static int printDeleteDespawns(final CommandContext<ServerCommandSource> c)
    {
        c.getSource().sendFeedback(asTxt("The command will "+(DELETE_DESPAWNS? "":"not ")+"delete artificially despawned items"),false);
        return 1;
    }
    private static int setDeleteDespawns(final CommandContext<ServerCommandSource> c)
    {
        //noinspection AssignmentUsedAsCondition
        c.getSource().sendFeedback
        (
            asTxt("The command now will "+((DELETE_DESPAWNS = getBool(c,"delete"))? "":"not ")+"delete artificially despawned items"),
            false
        );
        return 1;
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
                    literal("power").
                        executes(ItemLeakCommand::printPower).
                        then
                        (
                            literal("clear").
                                executes(ItemLeakCommand::clearPower)
                        ).
                        then
                        (
                            argument("block",blockPos()).
                                executes(ItemLeakCommand::setPower)
                        ).
                        then
                        (
                            argument("from",blockPos()).
                                then
                                (
                                    argument("to",blockPos()).
                                        executes(ItemLeakCommand::setPowerVolume)
                                )
                        )
                ).
                then
                (
                    literal("cooldown").
                        executes(ItemLeakCommand::printCooldown).
                        then
                        (
                            argument("cooldown",integer(1)).
                                suggests((c,b) -> suggestMatching(cooldownSuggestions,b)).
                                executes(ItemLeakCommand::setCooldown)
                        )
                ).
                then
                (
                    literal("batches").
                        executes(ItemLeakCommand::printBatches).
                        then
                        (
                            argument("batches",integer(1)).
                                suggests((c,b) -> suggestMatching(batchesSuggestions,b)).
                                executes(ItemLeakCommand::setBatches)
                        )
                ).
                then
                (
                    literal("batchSize").
                        executes(ItemLeakCommand::printBatchSize).
                        then
                        (
                            argument("batchSize",integer(1)).
                                suggests((c,b) -> suggestMatching(batchSizeSuggestions,b)).
                                executes(ItemLeakCommand::setBatchSize)
                        )
                ).
                then
                (
                    literal("minXPos").
                        executes(ItemLeakCommand::printMinXPos).
                        then
                        (
                            argument("minXPos",doubleArg()).
                                executes(ItemLeakCommand::setMinXPos)
                        )
                ).
                then
                (
                    literal("minYPos").
                        executes(ItemLeakCommand::printMinYPos).
                        then
                        (
                            argument("minYPos",doubleArg()).
                                executes(ItemLeakCommand::setMinYPos)
                        )
                ).
                then
                (
                    literal("minZPos").
                        executes(ItemLeakCommand::printMinZPos).
                        then
                        (
                            argument("minZPos",doubleArg()).
                                executes(ItemLeakCommand::setMinZPos)
                        )
                ).
                then
                (
                    literal("maxXPos").
                        executes(ItemLeakCommand::printMaxXPos).
                        then
                        (
                            argument("maxXPos",doubleArg()).
                                executes(ItemLeakCommand::setMaxXPos)
                        )
                ).
                then
                (
                    literal("maxYPos").
                        executes(ItemLeakCommand::printMaxYPos).
                        then
                        (
                            argument("maxYPos",doubleArg()).
                                executes(ItemLeakCommand::setMaxYPos)
                        )
                ).
                then
                (
                    literal("maxZPos").
                        executes(ItemLeakCommand::printMaxZPos).
                        then
                        (
                            argument("maxZPos",doubleArg()).
                                executes(ItemLeakCommand::setMaxZPos)
                        )
                ).
                then
                (
                    literal("XPos").
                        executes(ItemLeakCommand::printXPos).
                        then
                        (
                            argument("min",doubleArg()).
                                then
                                (
                                    argument("max",doubleArg()).
                                        executes(ItemLeakCommand::setXPos)
                                )
                        )
                ).
                then
                (
                    literal("YPos").
                        executes(ItemLeakCommand::printYPos).
                        then
                        (
                            argument("min",doubleArg()).
                                then
                                (
                                    argument("max",doubleArg()).
                                        executes(ItemLeakCommand::setYPos)
                                )
                        )
                ).
                then
                (
                    literal("ZPos").
                        executes(ItemLeakCommand::printZPos).
                        then
                        (
                            argument("min",doubleArg()).
                                then
                                (
                                    argument("max",doubleArg()).
                                        executes(ItemLeakCommand::setZPos)
                                )
                        )
                ).
                then
                (
                    literal("pos").
                        executes(ItemLeakCommand::printPos).
                        then
                        (
                            argument("minX",doubleArg()).
                                then
                                (
                                    argument("maxX",doubleArg()).
                                        then
                                        (
                                            argument("minY",doubleArg()).
                                                then
                                                (
                                                    argument("maxY",doubleArg()).
                                                        then
                                                        (
                                                            argument("minZ",doubleArg()).
                                                                then
                                                                (
                                                                    argument("maxZ",doubleArg()).
                                                                        executes(ItemLeakCommand::setPos)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                ).
                then
                (
                    literal("minXVel").
                        executes(ItemLeakCommand::printMinXVel).
                        then
                        (
                            argument("minXVel",doubleArg()).
                                executes(ItemLeakCommand::setMinXVel)
                        )
                ).
                then
                (
                    literal("minYVel").
                        executes(ItemLeakCommand::printMinYVel).
                        then
                        (
                            argument("minYVel",doubleArg()).
                                executes(ItemLeakCommand::setMinYVel)
                        )
                ).
                then
                (
                    literal("minZVel").
                        executes(ItemLeakCommand::printMinZVel).
                        then
                        (
                            argument("minZVel",doubleArg()).
                                executes(ItemLeakCommand::setMinZVel)
                        )
                ).
                then
                (
                    literal("maxXVel").
                        executes(ItemLeakCommand::printMaxXVel).
                        then
                        (
                            argument("maxXVel",doubleArg()).
                                executes(ItemLeakCommand::setMaxXVel)
                        )
                ).
                then
                (
                    literal("maxYVel").
                        executes(ItemLeakCommand::printMaxYVel).
                        then
                        (
                            argument("maxYVel",doubleArg()).
                                executes(ItemLeakCommand::setMaxYVel)
                        )
                ).
                then
                (
                    literal("maxZVel").
                        executes(ItemLeakCommand::printMaxZVel).
                        then
                        (
                            argument("maxZVel",doubleArg()).
                                executes(ItemLeakCommand::setMaxZVel)
                        )
                ).
                then
                (
                    literal("XVel").
                        executes(ItemLeakCommand::printXVel).
                        then
                        (
                            argument("min",doubleArg()).
                                then
                                (
                                    argument("max",doubleArg()).
                                        executes(ItemLeakCommand::setXVel)
                                )
                        )
                ).
                then
                (
                    literal("YVel").
                        executes(ItemLeakCommand::printYVel).
                        then
                        (
                            argument("min",doubleArg()).
                                then
                                (
                                    argument("max",doubleArg()).
                                        executes(ItemLeakCommand::setYVel)
                                )
                        )
                ).
                then
                (
                    literal("ZVel").
                        executes(ItemLeakCommand::printZVel).
                        then
                        (
                            argument("min",doubleArg()).
                                then
                                (
                                    argument("max",doubleArg()).
                                        executes(ItemLeakCommand::setZVel)
                                )
                        )
                ).
                then
                (
                    literal("vel").
                        executes(ItemLeakCommand::printVel).
                        then
                        (
                            argument("minX",doubleArg()).
                                then
                                (
                                    argument("maxX",doubleArg()).
                                        then
                                        (
                                            argument("minY",doubleArg()).
                                                then
                                                (
                                                    argument("maxY",doubleArg()).
                                                        then
                                                        (
                                                            argument("minZ",doubleArg()).
                                                                then
                                                                (
                                                                    argument("maxZ",doubleArg()).
                                                                        executes(ItemLeakCommand::setVel)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                ).
                then
                (
                    literal("scenario").
                        then
                        (
                            argument("scenario",word()).
                                suggests((c,b) -> suggestMatching(scenarios,b)).
                                executes(ItemLeakCommand::scenario)
                        )
                ).
                then
                (
                    literal("lifetime").
                        executes(ItemLeakCommand::printItemLifetime).
                        then
                        (
                            argument("lifetime",integer(1,6000)).
                                suggests((c,b) -> suggestMatching(itemLifetimeSuggestions,b)).
                                executes(ItemLeakCommand::setItemLifetime)
                        )
                ).
                then
                (
                    literal("mode").
                        executes(ItemLeakCommand::printMode).
                        then
                        (
                            argument("mode",word()).
                                suggests((c,b) -> suggestMatching(modes,b)).
                                executes(ItemLeakCommand::setMode)
                        )
                ).
                then
                (
                    literal("algorithm").
                        executes(ItemLeakCommand::printAlgorithm).
                        then
                        (
                            argument("algorithm",word()).
                                suggests((c,b) -> suggestMatching(algorithms,b)).
                                executes(ItemLeakCommand::setAlgorithm)
                        )
                ).
                then
                (
                    literal("deleteDespawns").
                        executes(ItemLeakCommand::printDeleteDespawns).
                        then
                        (
                            argument("delete",bool()).
                                executes(ItemLeakCommand::setDeleteDespawns)
                        )
                ).
                then
                (
                    literal("start").
                        executes(ItemLeakCommand::start)
                )
        );
    }
}