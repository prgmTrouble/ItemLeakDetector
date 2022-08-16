package com.prgmTrouble.item_leak_detector.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerWorld.class)
public interface ServerWorld_EntityManagerMixin
{
    @Accessor ServerEntityManager<Entity> getEntityManager();
}