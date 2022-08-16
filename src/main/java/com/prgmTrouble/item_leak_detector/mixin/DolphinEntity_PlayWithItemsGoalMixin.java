package com.prgmTrouble.item_leak_detector.mixin;

import com.prgmTrouble.item_leak_detector.util.RandomHelper;
import net.minecraft.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(targets = "net.minecraft.entity.passive.DolphinEntity.PlayWithItemsGoal")
public abstract class DolphinEntity_PlayWithItemsGoalMixin extends Goal
{
    @Redirect
    (
        method = "spitOutItem",
        at = @At
        (
            value = "INVOKE",
            target = "Ljava/util/Random;nextFloat()F"
        )
    )
    private float nextFloat(final Random random) {return RandomHelper.nextFloat(random);}
}