package com.prgmTrouble.item_leak_detector.mixin;

import com.prgmTrouble.item_leak_detector.util.RandomHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity
{
    protected PlayerEntityMixin(final EntityType<? extends LivingEntity> entityType,final World world)
    {
        super(entityType,world);
    }
    
    @Redirect
    (
        method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;",
        at = @At
        (
            value = "INVOKE",
            target = "Ljava/util/Random;nextFloat()F"
        )
    )
    private float nextFloat(final Random random) {return RandomHelper.nextFloat(random);}
}