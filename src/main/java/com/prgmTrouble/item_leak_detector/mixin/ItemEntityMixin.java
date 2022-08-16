package com.prgmTrouble.item_leak_detector.mixin;

import com.prgmTrouble.item_leak_detector.util.RandomHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import static com.prgmTrouble.item_leak_detector.command.SetItemVelocityCommand.modify;
import static com.prgmTrouble.item_leak_detector.util.ItemLeakDetector.*;

/** Sets the velocity for the item velocity command and handles despawns for the item leak command. */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity
{
    public ItemEntityMixin(final EntityType<?> type,final World world) {super(type,world);}
    
    @Redirect
    (
        method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V",
        at = @At
        (
            value = "INVOKE",
            target = "Ljava/util/Random;nextDouble()D"
        )
    )
    private static double nextDouble(final Random random) {return RandomHelper.nextDouble(random);}
    @Redirect
    (
        method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;DDD)V",
        at = @At
        (
            value = "INVOKE",
            target = "Lnet/minecraft/entity/ItemEntity;setVelocity(DDD)V"
        )
    )
    private void setVelocity(final ItemEntity entity,final double x,final double y,final double z)
    {
        super.setVelocity(modify(x,y,z));
    }
    @Inject(method = "tick",at = @At("HEAD"))
    private void tick(final CallbackInfo ci)
    {
        if(leakDetectorActive() && ((ItemEntity)(Object)this).getItemAge() >= LIFETIME)
            fail(this,(ServerWorld)getEntityWorld());
    }
}