package com.prgmTrouble.item_leak_detector.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import static com.prgmTrouble.item_leak_detector.util.ItemLeakDetector.SCHEDULED;
import static net.minecraft.block.Blocks.RED_CONCRETE;

@Mixin(Block.class)
public abstract class BlockMixin extends AbstractBlock implements ItemConvertible
{
    public BlockMixin(final Settings settings) {super(settings);}
    
    @Override
    public int getWeakRedstonePower(final BlockState state,final BlockView world,
                                    final BlockPos pos,final Direction direction)
    {
        return state.getBlock() == RED_CONCRETE && SCHEDULED.contains(pos)? 15:0;
    }
    @Override
    public void neighborUpdate(final BlockState state,final World world,final BlockPos pos,
                               final Block block,final BlockPos fromPos,final boolean notify)
    {
        if(state.getBlock() == RED_CONCRETE && SCHEDULED.contains(pos))
            world.updateNeighborsAlways(pos,RED_CONCRETE);
        super.neighborUpdate(state,world,pos,block,fromPos,notify);
    }
}