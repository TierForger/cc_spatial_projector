package dev.tierforger.ccspatialprojector.item;

import dev.tierforger.ccspatialprojector.block.SpatialProjectorBlockEntity;
import dev.tierforger.ccspatialprojector.util.ProjectorIdSavedData;
import dev.tierforger.ccspatialprojector.util.StackData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;

public class SpatialProjectorBlockItem extends BlockItem {
    private static final String PROJECTOR_ID = SpatialProjectorBlockEntity.TAG_PROJECTOR_ID;

    public SpatialProjectorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Placement restores ids through the block so duplicate-id checks always run.
        context.getItemInHand().remove(DataComponents.BLOCK_ENTITY_DATA);
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        projectorId(stack).ifPresentOrElse(
            id -> tooltip.add(Component.translatable("item.cc_spatial_projector.spatial_projector.projector_id", id).withStyle(ChatFormatting.GRAY)),
            () -> tooltip.add(Component.translatable("item.cc_spatial_projector.spatial_projector.new_id_hint").withStyle(ChatFormatting.DARK_GRAY))
        );
    }

    public static void setProjectorId(ItemStack stack, String projectorId) {
        if (ProjectorIdSavedData.isValidProjectorId(projectorId)) {
            StackData.putString(stack, PROJECTOR_ID, projectorId);
        } else {
            StackData.remove(stack, PROJECTOR_ID);
        }
    }

    public static Optional<String> projectorId(ItemStack stack) {
        return StackData.string(stack, PROJECTOR_ID).filter(ProjectorIdSavedData::isValidProjectorId);
    }
}
