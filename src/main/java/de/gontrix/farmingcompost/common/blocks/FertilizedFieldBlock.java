package de.gontrix.farmingcompost.common.blocks;

import de.gontrix.farmingcompost.FarmingCompost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FertilizedFieldBlock extends Block {
    public static final String NAME = "fertilized_field";

    public static IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);


    public FertilizedFieldBlock() {
        super(BlockBehaviour.Properties.of(Material.DIRT, MaterialColor.DIRT)
                .randomTicks()
                .strength(0.6F)
                .sound(SoundType.GRAVEL)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, 0));
        FarmingCompost.LOGGER.info("FarmingCompost FertilizedField");
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        int i = blockState.getValue(MOISTURE);

        if (!isNearWater(serverLevel, blockPos) && !serverLevel.isRainingAt(blockPos.above())) {
            if (i > 0) {
                serverLevel.setBlock(blockPos, blockState.setValue(MOISTURE, Integer.valueOf(i - 1)), 2);
            } else if (!isUnderCrops(serverLevel, blockPos)) {
                turnToSoil(blockState, serverLevel, blockPos);
            }

        } else if (i < 7) {
            serverLevel.setBlock(blockPos, blockState.setValue(MOISTURE, Integer.valueOf(7)), 2);
        }
    }

    private static boolean isUnderCrops(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState plant = blockGetter.getBlockState(blockPos.above());
        BlockState state = blockGetter.getBlockState(blockPos);

        return plant.getBlock() instanceof net.minecraftforge.common.IPlantable && state.canSustainPlant(blockGetter, blockPos, Direction.UP, (net.minecraftforge.common.IPlantable)plant.getBlock());
    }

    private static boolean isNearWater(LevelReader levelReader, BlockPos blockPos) {
        BlockState state = levelReader.getBlockState(blockPos);

        for(BlockPos blockpos : BlockPos.betweenClosed(
            blockPos.offset(-4, -1, -4),
            blockPos.offset(4, 1, 4)
        )) {
            if (state.canBeHydrated(levelReader, blockPos, levelReader.getFluidState(blockpos), blockpos)) {
                return true;
            }
        }

        return net.minecraftforge.common.FarmlandWaterManager.hasBlockWaterTicket(levelReader, blockPos);
    }

    public static void turnToSoil(BlockState blockState, Level level, BlockPos blockPos) {
        BlockState blockstate = pushEntitiesUp(blockState, Blocks.FERTILIZED_SOIL.get().defaultBlockState(), level, blockPos);

        level.setBlockAndUpdate(blockPos, blockstate);
        level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of((Entity)null, blockstate));
    }

    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
