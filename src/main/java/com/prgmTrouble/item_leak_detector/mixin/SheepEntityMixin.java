package com.prgmTrouble.item_leak_detector.mixin;

import com.prgmTrouble.item_leak_detector.util.RandomHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(SheepEntity.class)
public abstract class SheepEntityMixin extends AnimalEntity implements Shearable
{
    protected SheepEntityMixin(final EntityType<? extends AnimalEntity> entityType,final World world)
    {
        super(entityType,world);
    }
    
    @Redirect
    (
        method = "sheared",
        at = @At
        (
            value = "INVOKE",
            target = "Ljava/util/Random;nextFloat()F"
        )
    )
    private float nextFloat(final Random random) {return RandomHelper.nextFloat(random);}
}