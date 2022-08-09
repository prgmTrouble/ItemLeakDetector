package com.prgmTrouble.item_leak_detector.util;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

import static com.prgmTrouble.item_leak_detector.util.Broadcast.*;

public class ItemLeakDetector
{
    public static final Set<BlockPos> SCHEDULED = new HashSet<>();
    private static final Set<BlockPos> UPDATE = new HashSet<>();
    public static short LIFETIME = (short)6000;
    public static int INTERVAL = 1,BATCHES = 0,BATCH_SIZE = 0,TIME = -1;
    private static int TOTAL = 0,FAILED = 0,END_INTERVAL = 0;
    public static boolean RANDOM = false,QUIT_ON_FAIL = true;
    public static double ANGLE = 0,START_ANGLE = 0,END_ANGLE = 0,ANGLE_INC = 0,
                         SPEED = 0,START_SPEED = 0,END_SPEED = 0,SPEED_INC = 0;
    private static final Map<Entity,double[]> TRACKED = new HashMap<>();
    private static List<BlockPos> POWER = null;
    public static double[] toV(final double angle,final double speed) {return new double[] {speed*Math.cos(angle),speed*Math.sin(angle)};}
    public static double[] randomV(final Random r)
    {
        return toV(Math.fma(r.nextDouble(),END_ANGLE-START_ANGLE,START_ANGLE),Math.fma(r.nextDouble(),END_SPEED-START_SPEED,START_SPEED));
    }
    private static final ItemStack REPLACEMENT = new ItemStack(Blocks.CAKE);
    public static void nextItem(final World world,final ServerEntityManager<Entity> manager,
                                final double x,final double y,final double z)
    {
        final double[] v;
        if(RANDOM) v = randomV(world.getRandom());
        else
        {
            v = toV(ANGLE,SPEED);
            if(ANGLE == END_ANGLE) {if((SPEED += SPEED_INC) > END_SPEED) SPEED = END_SPEED;}
            else if((ANGLE += ANGLE_INC) > END_ANGLE) ANGLE = END_ANGLE;
        }
        for(int i = 0;i < 4;++i)
        {
            final ItemEntity e = new ItemEntity(world,x,y,z,REPLACEMENT,v[0],0.2,v[1]);
            TRACKED.put(e,new double[] {v[0],v[1],x,y,z});
            manager.addEntity(e);
        }
    }
    public static void toPower(final BlockPos from,final BlockPos to,final World world)
    {
        UPDATE.clear();
        final List<BlockPos> blocks = new ArrayList<>();
        for(int x = from.getX();x <= to.getX();++x)
            for(int y = from.getY();y <= from.getY();++y)
                for(int z = from.getZ();z <= from.getZ();++z)
                {
                    final BlockPos p = new BlockPos(x,y,z);
                    if(world.getBlockState(p).getBlock() == Blocks.RED_CONCRETE)
                    {
                        blocks.add(p);
                        for(final Direction d : Direction.values())
                            UPDATE.add(p.offset(d));
                    }
                }
        POWER = List.copyOf(blocks);
    }
    public static void tick(final ServerWorld world)
    {
        if(BATCHES > 0)
        {
            if(--TIME == 0)
            {
                END_INTERVAL = LIFETIME;
                TIME = INTERVAL;
                assert POWER != null;
                SCHEDULED.addAll(POWER);
                for(final BlockPos p : POWER)
                    world.updateNeighbor(p,Blocks.RED_CONCRETE,p);
                // From my testing, it seems that using a bulk clear is a bit more
                // efficient than simply removing the block in the neighborUpdate
                // method. Also, this allows us to cache block updates in the UPDATE
                // set.
                SCHEDULED.clear();
                for(final BlockPos p : UPDATE)
                    world.updateNeighbor(p,Blocks.RED_CONCRETE,p);
            }
        }
        else if(END_INTERVAL != 0 && --END_INTERVAL == 0)
            stop(world);
        /*
        if(TIME != -1 && --TIME == 0)
            if(BATCHES == 0 && --END_INTERVAL <= 0) stop(world);
            else
            {
                END_INTERVAL = TIME = INTERVAL;
                assert POWER != null;
                SCHEDULED.addAll(POWER);
                for(final BlockPos p : POWER)
                    world.updateNeighbor(p,Blocks.RED_CONCRETE,p);
                // From my testing, it seems that using a bulk clear is a bit more
                // efficient than simply removing the block in the neighborUpdate
                // method. Also, this allows us to cache block updates in the UPDATE
                // set.
                SCHEDULED.clear();
                for(final BlockPos p : UPDATE)
                    world.updateNeighbor(p,Blocks.RED_CONCRETE,p);
            }
        */
    }
    private static void printStatistics(final ServerWorld world)
    {
        broadcast
        (
            world,
            txt("Total items spawned: ",TEXT).append(txt(TOTAL+"",NUMBER)),
            txt("Probability of failure: ",TEXT).append(txt((FAILED == 0? "< "+(100./TOTAL):("~"+(100.*FAILED/TOTAL)))+"%",NUMBER))
        );
    }
    public static void stop(final ServerWorld world)
    {
        BATCHES = 0;
        TIME = -1;
        END_INTERVAL = 0;
        for(final Entity e : TRACKED.keySet())
            if(e != null)
                e.discard();
        TRACKED.clear();
        for(final BlockPos p : POWER)
            world.updateNeighbors(p,Blocks.AIR);
        printStatistics(world);
        TOTAL = 0;
        FAILED = 0;
    }
    public static void deleteItem(final Entity id) {if(TRACKED.remove(id) != null) ++TOTAL;}
    public static void fail(final Entity item,final ServerWorld world)
    {
        final double[] v = TRACKED.remove(item);
        if(v != null)
        {
            ++TOTAL;
            ++FAILED;
            broadcast
            (
                world,
                txt
                (
                    "id:"+item.getId() +
                    " age:"+item.age +
                    " velocity:["+v[0]+",0.2,"+v[1]+"]" +
                    " pos:["+v[2]+","+v[3]+","+v[4]+"]" +
                    " despawned:["+item.getX()+","+item.getY()+","+item.getZ()+"]",
                    NUMBER
                ).
                getWithStyle
                (
                    Style.EMPTY.withClickEvent
                    (
                        new ClickEvent
                        (
                            ClickEvent.Action.SUGGEST_COMMAND,
                            "/summon item "+v[2]+" "+v[3]+" "+v[4]+" {Item:{id:cake,Count:1},Motion:["+v[0]+"d,0.2d,"+v[1]+"d]}"
                        )
                    ).
                    withHoverEvent
                    (
                        new HoverEvent
                        (
                            HoverEvent.Action.SHOW_TEXT,
                            txt("click to copy summon command (doesn't account for entity age and id)",TEXT)
                        )
                    )
                ).toArray(Text[]::new)
            );
            if(QUIT_ON_FAIL) stop(world);
        }
    }
}




































