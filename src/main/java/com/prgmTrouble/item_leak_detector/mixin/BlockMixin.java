package com.prgmTrouble.item_leak_detector.mixin;

import com.prgmTrouble.item_leak_detector.util.RandomHelper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

import static com.prgmTrouble.item_leak_detector.util.ItemLeakDetector.SCHEDULED;
import static com.prgmTrouble.item_leak_detector.util.ItemLeakDetector.lock;

@Mixin(Block.class)
public abstract class BlockMixin extends AbstractBlock implements ItemConvertible
{
    public BlockMixin(final Settings settings) {super(settings);}
    
    @Override
    public int getWeakRedstonePower(final BlockState state,final BlockView world,
                                    final BlockPos pos,final Direction direction)
    {
        synchronized(lock) {return SCHEDULED.contains(pos)? 15:0;}
    }
    @Redirect
    (
        method =
        {
            "dropStack(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)V",
            "dropStack(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/item/ItemStack;)V"
        },
        at = @At
        (
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/MathHelper;nextDouble(Ljava/util/Random;DD)D"
        )
    )
    private static double nextDouble(final Random random,final double min,final double max)
    {
        return RandomHelper.nextDouble(random,min,max);
    }
}