package com.prgmTrouble.item_leak_detector.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.prgmTrouble.item_leak_detector.util.ItemLeakDetector.*;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements StructureWorldAccess
{
    @Shadow @Final private ServerEntityManager<net.minecraft.entity.Entity> entityManager;
    
    protected ServerWorldMixin(final MutableWorldProperties properties,final RegistryKey<World> registryRef,
                               final DimensionType dimensionType,final Supplier<Profiler> profiler,
                               final boolean isClient,final boolean debugWorld,final long seed)
    {
        super(properties,registryRef,dimensionType,profiler,isClient,debugWorld,seed);
    }
    
    @Redirect
    (
        method = "spawnEntity",
        at = @At
        (
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;addEntity(Lnet/minecraft/entity/Entity;)Z"
        )
    )
    private boolean addEntity(final ServerWorld world,final Entity entity)
    {
        if(BATCHES != 0 && entity instanceof ItemEntity)
        {
            final double x,y,z;
            {
                // TODO Block::dropStack called by piston (random)
                //      ComposterBlock::emptyFullComposter (random)
                //      JukeboxBlock::removeRecord (random)
                //      PumpkinBlock::onUse (weird setVelocity)
                //      ItemDispenserBehavior::spawnItem (gaussian setVelocity)
                //      CatEntity.SleepWithOwnerGoal::dropMorningGifts (depends on cat)
                //      DolphinEntity.PlayWithItemsGoal::spitOutItem (depends on dolphin)
                //      FoxEntity::spit,dropItem (depends on fox)
                //      MooshroomEntity::sheared (depends on mooshroom)
                //      PlayerEntity::dropItem (weird setVelocity + depends on player)
                //      FishingBobberEntity::use (weird setVelocity + depends on player + depends on bobber)
                //      ItemScatterer::spawn (weird setVelocity)
                //
                final Vec3d p = entity.getPos();
                x = p.x;
                y = p.y;
                z = p.z;
            }
            for(int i = 0;i < BATCH_SIZE;++i)
                nextItem(world,entityManager,x,y,z);
            --BATCHES;
            return true;
        }
        else
            return entityManager.addEntity(entity);
    }
    @Inject(method = "tick",at = @At("HEAD"))
    private void tickWorld(final BooleanSupplier shouldKeepTicking,final CallbackInfo ci)
    {
        tick((ServerWorld)(Object)this);
    }
}