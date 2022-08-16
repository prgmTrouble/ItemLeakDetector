package com.prgmTrouble.item_leak_detector.util;

import com.prgmTrouble.item_leak_detector.mixin.ServerWorld_EntityManagerMixin;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

import static com.prgmTrouble.item_leak_detector.command.ItemLeakCommand.vec;
import static com.prgmTrouble.item_leak_detector.util.Broadcast.*;
import static net.minecraft.block.Blocks.RED_CONCRETE;

public class ItemLeakDetector
{
    public static final Set<BlockPos> SCHEDULED = new HashSet<>(),
                                      POWER = new HashSet<>(),
                                      UPDATE = new TreeSet<>();
    //TODO figure out why CME is happening and then maybe optimize
    public static final Object lock = new Object();
    public static boolean ACTIVE = false;
    public static short LIFETIME = (short)6000;
    public static int INTERVAL = 1,BATCHES = 0,BATCH_SIZE = 0,TIME = -1;
    private static int TOTAL = 0,FAILED = 0,END_INTERVAL = 0;
    public static boolean RANDOM = true,QUIT_ON_FAIL = true,DELETE_DESPAWNS = true;
    public static final double[][] DATA = new double[6][4];
    public static final byte POS_X = 0,POS_Y = 1,POS_Z = 2,VEL_X = 3,VEL_Y = 4,VEL_Z = 5,
                             VALUE = 0,START = 1,END = 2,INC = 3;
    private static final Map<Entity,double[]> TRACKED = new HashMap<>();
    private static final ItemStack REPLACEMENT = new ItemStack(Blocks.CAKE);
    public static boolean leakDetectorActive() {synchronized(lock) {return ACTIVE;}}
    public static void nextItem(final World world,final Entity toReplace)
    {
        final Vec3d v = toReplace.getVelocity();
        nextItem(world,toReplace.getX(),toReplace.getY(),toReplace.getZ(),v.x,v.y,v.z);
    }
    public static void nextItem(final World world,
                                final double px,final double py,final double pz,
                                final double vx,final double vy,final double vz)
    {
        synchronized(lock)
        {
            if(ACTIVE && BATCHES != 0)
            {
                for(int b = 0;b < BATCH_SIZE;++b)
                {
                    final double[] item;
                    if(RANDOM)
                    {
                        // Choose a random position from the given range.
                        final Random r = world.getRandom();
                        item = new double[]
                        {
                            // rand[0,1] * (max - min) + min + pos
                            Math.fma(r.nextDouble(),DATA[POS_X][END] - DATA[POS_X][START],DATA[POS_X][START] + px),
                            Math.fma(r.nextDouble(),DATA[POS_Y][END] - DATA[POS_Y][START],DATA[POS_Y][START] + py),
                            Math.fma(r.nextDouble(),DATA[POS_Z][END] - DATA[POS_Z][START],DATA[POS_Z][START] + pz),
                            
                            // rand[0,1] * (max - min) + min + vel
                            Math.fma(r.nextDouble(),DATA[VEL_X][END] - DATA[VEL_X][START],DATA[VEL_X][START] + vx),
                            Math.fma(r.nextDouble(),DATA[VEL_Y][END] - DATA[VEL_Y][START],DATA[VEL_Y][START] + vy),
                            Math.fma(r.nextDouble(),DATA[VEL_Z][END] - DATA[VEL_Z][START],DATA[VEL_Z][START] + vz)
                        };
                    }
                    else
                    {
                        // Increment the data array to the next position.
                        byte i;
                        for(i = 0;i < (byte)DATA.length && DATA[i][VALUE] == DATA[i][END];++i)
                            DATA[i][VALUE] = DATA[i][START];
                        DATA[i][VALUE] = Math.min(DATA[i][VALUE] + DATA[i][INC],DATA[i][END]);
                        item = new double[]
                        {
                            DATA[POS_X][VALUE] + px,
                            DATA[POS_Y][VALUE] + py,
                            DATA[POS_Z][VALUE] + pz,
                            DATA[VEL_X][VALUE] + vx,
                            DATA[VEL_Y][VALUE] + vy,
                            DATA[VEL_Z][VALUE] + vz
                        };
                    }
                    for(byte i = 0;i < (byte)4;++i)
                    {
                        final ItemEntity e = new ItemEntity
                        (
                            world,
                            item[POS_X],item[POS_Y],item[POS_Z],
                            REPLACEMENT,
                            item[VEL_X],item[VEL_Y],item[VEL_Z]
                        );
                        TRACKED.put(e,item);
                        ((ServerWorld_EntityManagerMixin)world).getEntityManager().addEntity(e);
                    }
                }
                if(--BATCHES == 0)
                {
                    ACTIVE = false;
                    END_INTERVAL = LIFETIME;
                }
            }
        }
    }
    public static void tick(final ServerWorld world)
    {
        synchronized(lock)
        {
            if(ACTIVE)
            {
                if(--TIME == 0)
                {
                    TIME = INTERVAL;
                    if(!POWER.isEmpty())
                    {
                        // From my testing, it seems that using a bulk clear is a bit more
                        // efficient than simply removing the block in the neighborUpdate
                        // method. Also, this allows us to cache block updates in the UPDATE
                        // set.
                        SCHEDULED.addAll(POWER);
                        for(final BlockPos p : UPDATE)
                            world.updateNeighbor(p,RED_CONCRETE,p);
                        SCHEDULED.clear();
                        for(final BlockPos p : UPDATE)
                            world.updateNeighbor(p,RED_CONCRETE,p);
                    }
                }
            }
            else if(END_INTERVAL != 0 && --END_INTERVAL == 0)
                stop(world);
        }
    }
    private static void printStatistics(final ServerWorld world)
    {
        synchronized(lock)
        {
            broadcast
            (
                world,
                txt("Total items spawned: ",TEXT).append(txt(TOTAL+"",NUMBER)),
                txt("Probability of failure: ",TEXT).append(txt((FAILED == 0? "< "+(100./TOTAL):("~"+(100.*FAILED/TOTAL)))+"%",NUMBER))
            );
        }
    }
    public static void stop(final ServerWorld world)
    {
        synchronized(lock)
        {
            broadcast(world,asTxt("stopped"));
            ACTIVE = false;
            BATCHES = 0;
            TIME = -1;
            END_INTERVAL = 0;
            for(final Entity e : TRACKED.keySet())
                if(e != null)
                    e.discard();
            TRACKED.clear();
            SCHEDULED.clear();
            POWER.clear();
            for(final BlockPos p : UPDATE)
                world.updateNeighbor(p,RED_CONCRETE,p);
            UPDATE.clear();
            printStatistics(world);
            TOTAL = 0;
            FAILED = 0;
        }
    }
    public static void deleteItem(final Entity id) {synchronized(lock) {if(TRACKED.remove(id) != null) ++TOTAL;}}
    private static Text fmt(final double[] data,final int itemID,final int itemAge,
                            final double despawnX,final double despawnY,final double despawnZ)
    {
        return
            asTxt("id:")                            .append(
            asNum(itemID)                           .append(
            asTxt(" age:")                          .append(
            asNum(itemAge)                          .append(
            asTxt(" pos:")                          .append(
            vec(data[POS_X],data[POS_Y],data[POS_Z]).append(
            asTxt(" vel:")                          .append(
            vec(data[VEL_X],data[VEL_Y],data[VEL_Z]).append(
            asTxt(" found @:")                      .append(
            vec(despawnX,despawnY,despawnZ)))))))))).
            fillStyle
            (
                Style.EMPTY.withClickEvent
                (
                    new ClickEvent
                    (
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/summon item "+data[2]+" "+data[3]+" "+data[4]+" {Item:{id:cake,Count:1},Motion:["+data[0]+"d,0.2d,"+data[1]+"d]}"
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
            );
    }
    public static void fail(final Entity item,final ServerWorld world)
    {
        synchronized(lock)
        {
            final double[] data = TRACKED.remove(item);
            if(data != null)
            {
                ++TOTAL;
                ++FAILED;
                broadcast(world,fmt(data,item.getId(),item.age,item.getX(),item.getY(),item.getZ()));
                if(DELETE_DESPAWNS) item.discard();
                if(QUIT_ON_FAIL) stop(world);
            }
        }
    }
}