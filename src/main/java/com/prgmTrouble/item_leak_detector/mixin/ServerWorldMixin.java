package com.prgmTrouble.item_leak_detector.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
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

/**
 * Adds hooks for spawning and ticking items.
 * Note: There are other things that implement 'ModifiableWorld' and thus have a 'spawnEntity'
 *       method. However, I think all of them just have to do with serialization (the items which
 *       are generated don't need to be saved) which means they can be ignored. Also, the mod seems
 *       to work just fine without touching them anyway.
 */
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements StructureWorldAccess
{
    @Shadow @Final private ServerEntityManager<Entity> entityManager;
    
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
        synchronized(lock)
        {
            if(leakDetectorActive() && entity instanceof ItemEntity)
            {
                nextItem(world,entity);
                return true;
            }
            return entityManager.addEntity(entity); //TODO potential CME?
        }
    }
    @Inject(method = "tick",at = @At("HEAD"))
    private void tickWorld(final BooleanSupplier shouldKeepTicking,final CallbackInfo ci)
    {
        tick((ServerWorld)(Object)this);
    }
}