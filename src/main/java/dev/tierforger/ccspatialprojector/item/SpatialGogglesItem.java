package dev.tierforger.ccspatialprojector.item;

import dev.tierforger.ccspatialprojector.CcSpatialProjector;
import dev.tierforger.ccspatialprojector.block.SpatialProjectorBlockEntity;
import dev.tierforger.ccspatialprojector.registry.ModArmorMaterials;
import dev.tierforger.ccspatialprojector.registry.ModBlocks;
import dev.tierforger.ccspatialprojector.util.GoggleLink;
import dev.tierforger.ccspatialprojector.util.ProjectorKey;
import dev.tierforger.ccspatialprojector.visual.ServerVisualStore;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class SpatialGogglesItem extends ArmorItem {
    public SpatialGogglesItem(Properties properties) {
        super(ModArmorMaterials.SPATIAL_GOGGLES, ArmorItem.Type.HELMET, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return super.use(level, player, hand);

        unbind(level, player, stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        if (player != null && player.isShiftKeyDown()) {
            unbind(level, player, stack);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.getBlockState(context.getClickedPos()).is(ModBlocks.SPATIAL_PROJECTOR.get())) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide
            && player instanceof ServerPlayer serverPlayer
            && level.getBlockEntity(context.getClickedPos()) instanceof SpatialProjectorBlockEntity projector) {
            bindToProjector(stack, serverPlayer, projector);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return CcSpatialProjector.id("textures/models/armor/spatial_goggles_layer_1.png");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        GoggleLink.appendTooltip(stack, tooltip);
    }

    private static void bindToProjector(ItemStack stack, ServerPlayer player, SpatialProjectorBlockEntity projector) {
        ProjectorKey key = projector.projectorKey(player.serverLevel());
        GoggleLink.bind(stack, key);
        ServerVisualStore.syncSourceTo(player, key);
        player.displayClientMessage(Component.translatable("message.cc_spatial_projector.goggles_bound", key.id()), true);
    }

    private static void unbind(Level level, Player player, ItemStack stack) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) return;

        GoggleLink.clear(stack);
        ServerVisualStore.syncCurrentTo(serverPlayer);
        serverPlayer.displayClientMessage(Component.translatable("message.cc_spatial_projector.goggles_unbound"), true);
    }
}
