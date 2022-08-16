package com.prgmTrouble.item_leak_detector.mixin;

import com.prgmTrouble.item_leak_detector.util.RandomHelper;
import net.minecraft.block.GourdBlock;
import net.minecraft.block.PumpkinBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(PumpkinBlock.class)
public abstract class PumpkinBlockMixin extends GourdBlock
{
    public PumpkinBlockMixin(final Settings settings) {super(settings);}
    
    @Redirect
    (
        method = "onUse",
        at = @At
        (
            value = "INVOKE",
            target = "Ljava/util/Random;nextDouble()D"
        )
    )
    private double nextDouble(final Random random) {return RandomHelper.nextDouble(random);}
}