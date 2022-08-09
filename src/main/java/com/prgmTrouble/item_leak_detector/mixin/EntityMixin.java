package com.prgmTrouble.item_leak_detector.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.prgmTrouble.item_leak_detector.command.SetItemVelocityCommand.modify;
import static com.prgmTrouble.item_leak_detector.command.SetItemVelocityCommand.velocityModifier;
import static com.prgmTrouble.item_leak_detector.util.ItemLeakDetector.BATCHES;
import static com.prgmTrouble.item_leak_detector.util.ItemLeakDetector.deleteItem;

@Mixin(Entity.class)
public abstract class EntityMixin implements Nameable,EntityLike,CommandOutput
{
    @Shadow @Final private EntityType<?> type;
    @Shadow private Vec3d velocity;
    
    @Inject(method = "<init>", at = @At("RETURN"))
    private void ctor(final EntityType<?> type,final World world,final CallbackInfo ci)
    {
        if(type == EntityType.ITEM)
            velocity = new Vec3d(velocityModifier.x,velocityModifier.y,velocityModifier.z);
    }
    @Redirect
    (
        method = "readNbt",
        at = @At
        (
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;setVelocity(DDD)V"
        )
    )
    private void readNBT(final Entity entity,final double x,final double y,final double z)
    {
        if(type == EntityType.ITEM)
            entity.setVelocity(modify(x,y,z));
    }
    @Inject(method = "remove",at = @At("HEAD"))
    private void remove(final Entity.RemovalReason reason,final CallbackInfo ci)
    {
        if(BATCHES != 0) deleteItem((Entity)(Object)this);
    }
}