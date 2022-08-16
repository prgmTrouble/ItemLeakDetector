package com.prgmTrouble.item_leak_detector.util;

import net.minecraft.util.math.MathHelper;

import java.util.Random;

import static com.prgmTrouble.item_leak_detector.util.ItemLeakDetector.leakDetectorActive;

/*/
List of places where ItemEntity instances are created:
 - AbstractDecorationEntity       ::dropStack
 - BeehiveBlock                   ::onBreak
 - Block                          ::dropStack* (random)
 - CatEntity.SleepWithOwnerGoal   ::dropMorningGifts (depends on cat)
 - ComposterBlock                 ::emptyFullComposter (random)
 - DolphinEntity.PlayWithItemsGoal::spitOutItem (depends on dolphin)
 - Entity                         ::dropStack
 - EyeOfEnderEntity               ::tick
 - FishingBobberEntity            ::use (weird setVelocity + depends on player + depends on bobber)
 - FoxEntity                      ::spit,dropItem (depends on fox)
 - ItemDispenserBehavior          ::spawnItem
 - ItemScatterer                  ::spawn (weird setVelocity)
 - ItemUsage                      ::spawnItemContents
 - JukeboxBlock                   ::removeRecord (random)
 - LecternBlock                   ::dropBook
 - LivingEntity                   ::onKilledBy
 - LookTargetUtil                 ::give
 - LootCommand                    ::executeSpawn
 - MooshroomEntity                ::sheared (depends on mooshroom)
 - PlayerEntity                   ::dropItem (weird setVelocity + depends on player)
 - PumpkinBlock                   ::onUse (weird setVelocity)
 - ShulkerBoxBlock                ::onBreak
 - TestContext                    ::spawnItem

The only ones which set random positions are Block, ComposterBlock, ItemScatterer, and JukeboxBlock.

*The simpler one is used by pistons and the more complex one is used when rooted dirt is tilled.
/*/

/** Eliminates the variability in position when an item spawns. */
public final class RandomHelper
{
    private RandomHelper() {}
    
    public static float nextFloat(final Random random) {return leakDetectorActive()? 0F : random.nextFloat();}
    public static double nextDouble(final Random random,final double min,final double max)
    {
        return leakDetectorActive()? 0D : MathHelper.nextDouble(random,min,max);
    }
    public static double nextDouble(final Random random) {return leakDetectorActive()? 0D : random.nextDouble();}
    public static double nextGaussian(final Random random) {return leakDetectorActive()? 0D : random.nextGaussian();}
}