package com.prgmTrouble.item_leak_detector.mixin;

import com.prgmTrouble.item_leak_detector.util.RandomHelper;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(ItemDispenserBehavior.class)
public abstract class ItemDispenserBehaviorMixin implements DispenserBehavior
{
    @Redirect
    (
        method = "spawnItem",
        at = @At
        (
            value = "INVOKE",
            target = "Ljava/util/Random;nextGaussian()D"
        )
    )
    private static double nextGaussian(final Random random) {return RandomHelper.nextGaussian(random);}
    @Redirect
    (
        method = "spawnItem",
        at = @At
        (
            value = "INVOKE",
            target = "Ljava/util/Random;nextDouble()D"
        )
    )
    private static double nextDouble(final Random random) {return RandomHelper.nextDouble(random);}
}