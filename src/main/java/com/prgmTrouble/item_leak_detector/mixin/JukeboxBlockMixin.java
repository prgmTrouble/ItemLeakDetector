package com.prgmTrouble.item_leak_detector.mixin;

import com.prgmTrouble.item_leak_detector.util.RandomHelper;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.JukeboxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(JukeboxBlock.class)
public abstract class JukeboxBlockMixin extends BlockWithEntity
{
    protected JukeboxBlockMixin(final Settings settings) {super(settings);}
    
    @Redirect
    (
        method = "removeRecord",
        at = @At
        (
            value = "INVOKE",
            target = "Ljava/util/Random;nextFloat()F"
        )
    )
    private float nextFloat(final Random random) {return RandomHelper.nextFloat(random);}
}