package com.prgmTrouble.item_leak_detector.mixin;

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

import static com.prgmTrouble.item_leak_detector.command.SetItemVelocityCommand.modify;
import static com.prgmTrouble.item_leak_detector.util.ItemLeakDetector.*;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity
{
    public ItemEntityMixin(final EntityType<?> type,final World world) {super(type,world);}
    
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
    {//TODO set age to 6000-itemLifetime
        super.setVelocity(modify(x,y,z));
    }
    @Inject(method = "tick",at = @At("HEAD"))
    private void tick(final CallbackInfo ci)
    {
        if(BATCHES != 0 && ((ItemEntity)(Object)this).getItemAge() >= LIFETIME)
            fail(this,(ServerWorld)getEntityWorld());
    }
}