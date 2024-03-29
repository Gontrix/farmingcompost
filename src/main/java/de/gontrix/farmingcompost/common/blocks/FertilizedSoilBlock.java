package de.gontrix.farmingcompost.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolActions;

public class FertilizedSoilBlock extends Block {
    public static final String NAME = "fertilized_soil";

    public FertilizedSoilBlock() {
        super(BlockBehaviour.Properties.of(Material.DIRT, MaterialColor.DIRT)
                .randomTicks()
                .strength(0.5F)
                .sound(SoundType.GRAVEL)
        );
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND && held.canPerformAction(ToolActions.HOE_DIG)){
            BlockState blockstate = pushEntitiesUp(blockState, Blocks.FERTILIZED_FIELD.get().defaultBlockState(), level, blockPos);

            level.setBlockAndUpdate(blockPos, blockstate);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of((Entity)null, blockstate));

            return InteractionResult.SUCCESS;
        }

        return super.use(blockState, level, blockPos, player, hand, hit);
    }

}
