package com.prgmTrouble.item_leak_detector.mixin;

import com.prgmTrouble.item_leak_detector.util.RandomHelper;
import net.minecraft.util.ItemScatterer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(ItemScatterer.class)
public abstract class ItemScattererMixin
{
    @Redirect
    (
        method = "spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V",
        at = @At
        (
            value = "INVOKE",
            target = "Ljava/util/Random;nextDouble()D"
        )
    )
    private static double nextDouble(final Random random) {return RandomHelper.nextDouble(random);}
    @Redirect
    (
        method = "spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V",
        at = @At
        (
            value = "INVOKE",
            target = "Ljava/util/Random;nextGaussian()D"
        )
    )
    private static double nextGaussian(final Random random) {return RandomHelper.nextGaussian(random);}
}