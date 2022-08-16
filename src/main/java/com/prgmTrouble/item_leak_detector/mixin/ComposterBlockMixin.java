package com.prgmTrouble.item_leak_detector.mixin;

import com.prgmTrouble.item_leak_detector.util.RandomHelper;
import net.minecraft.block.Block;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.InventoryProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(ComposterBlock.class)
public abstract class ComposterBlockMixin extends Block implements InventoryProvider
{
    public ComposterBlockMixin(final Settings settings) {super(settings);}
    
    @Redirect
    (
        method = "emptyFullComposter",
        at = @At
        (
            value = "INVOKE",
            target = "Ljava/util/Random;nextFloat()F"
        )
    )
    private static float nextFloat(final Random random) {return RandomHelper.nextFloat(random);}
}