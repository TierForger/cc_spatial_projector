package dev.tierforger.ccspatialprojector.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import dev.tierforger.ccspatialprojector.visual.ServerVisualStore;
import dev.tierforger.ccspatialprojector.item.SpatialProjectorBlockItem;
import dev.tierforger.ccspatialprojector.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import java.util.List;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SpatialProjectorBlock extends BaseEntityBlock {
    public static final MapCodec<SpatialProjectorBlock> CODEC = simpleCodec(SpatialProjectorBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    private static final Box[] MODEL_BOXES = new Box[] {
        // Matches the Blockbench Java model dimensions. The model front/lens points NORTH.
        new Box(4.0, 1.0, 1.0, 12.0, 7.0, 15.0),
        new Box(6.25, 3.0, 0.0, 9.75, 6.5, 1.0),
        new Box(9.5, 0.0, 13.0, 11.5, 1.0, 14.0),
        new Box(4.5, 0.0, 13.0, 6.5, 1.0, 14.0),
        new Box(4.5, 0.0, 2.0, 6.5, 1.0, 3.0),
        new Box(9.5, 0.0, 2.0, 11.5, 1.0, 3.0),
    };

    private static final VoxelShape FLOOR_NORTH = makeShape(AttachFace.FLOOR, Direction.NORTH);
    private static final VoxelShape FLOOR_EAST = makeShape(AttachFace.FLOOR, Direction.EAST);
    private static final VoxelShape FLOOR_SOUTH = makeShape(AttachFace.FLOOR, Direction.SOUTH);
    private static final VoxelShape FLOOR_WEST = makeShape(AttachFace.FLOOR, Direction.WEST);

    private static final VoxelShape CEILING_NORTH = makeShape(AttachFace.CEILING, Direction.NORTH);
    private static final VoxelShape CEILING_EAST = makeShape(AttachFace.CEILING, Direction.EAST);
    private static final VoxelShape CEILING_SOUTH = makeShape(AttachFace.CEILING, Direction.SOUTH);
    private static final VoxelShape CEILING_WEST = makeShape(AttachFace.CEILING, Direction.WEST);

    public SpatialProjectorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(FACE, AttachFace.FLOOR));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction playerDirection = context.getHorizontalDirection();
        boolean placeAwayFromPlayer = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
        Direction facing = placeAwayFromPlayer ? playerDirection : playerDirection.getOpposite();
        AttachFace face = context.getClickedFace() == Direction.DOWN ? AttachFace.CEILING : AttachFace.FLOOR;

        return defaultBlockState()
            .setValue(FACE, face)
            .setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(level.getBlockEntity(pos) instanceof SpatialProjectorBlockEntity projector)) return;

        projector.restoreOrAllocateProjectorId(serverLevel, SpatialProjectorBlockItem.projectorId(stack));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpatialProjectorBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && !movedByPiston
            && level instanceof ServerLevel serverLevel
            && level.getBlockEntity(pos) instanceof SpatialProjectorBlockEntity projector) {
            // The projector id is stable and goggles remain bound to it, but visuals stop immediately
            // when this block is actually broken/dropped into the world.
            projector.projectorKeyIfPresent().ifPresent(key -> ServerVisualStore.clearSource(serverLevel, key));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }


    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity rawBlockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (rawBlockEntity instanceof SpatialProjectorBlockEntity projector) {
            for (ItemStack stack : drops) {
                if (stack.is(ModItems.SPATIAL_PROJECTOR.get())) {
                    SpatialProjectorBlockItem.setProjectorId(stack, projector.getProjectorId());
                }
            }
        }
        return drops;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getProjectorShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getProjectorShape(state);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    private static VoxelShape getProjectorShape(BlockState state) {
        AttachFace face = state.getValue(FACE);
        Direction facing = state.getValue(FACING);

        if (face == AttachFace.CEILING) {
            return switch (facing) {
                case EAST -> CEILING_EAST;
                case SOUTH -> CEILING_SOUTH;
                case WEST -> CEILING_WEST;
                default -> CEILING_NORTH;
            };
        }

        return switch (facing) {
            case EAST -> FLOOR_EAST;
            case SOUTH -> FLOOR_SOUTH;
            case WEST -> FLOOR_WEST;
            default -> FLOOR_NORTH;
        };
    }

    private static VoxelShape makeShape(AttachFace face, Direction facing) {
        VoxelShape shape = Shapes.empty();
        for (Box original : MODEL_BOXES) {
            Box box = original;
            if (face == AttachFace.CEILING) {
                box = box.mirrorY();
            }
            box = box.rotateFromNorthTo(facing);
            shape = Shapes.or(shape, Block.box(box.x1, box.y1, box.z1, box.x2, box.y2, box.z2));
        }
        return shape.optimize();
    }

    private record Box(double x1, double y1, double z1, double x2, double y2, double z2) {
        Box mirrorY() {
            return new Box(x1, 16.0 - y2, z1, x2, 16.0 - y1, z2);
        }

        Box rotateFromNorthTo(Direction direction) {
            return switch (direction) {
                case EAST -> new Box(16.0 - z2, y1, x1, 16.0 - z1, y2, x2);
                case SOUTH -> new Box(16.0 - x2, y1, 16.0 - z2, 16.0 - x1, y2, 16.0 - z1);
                case WEST -> new Box(z1, y1, 16.0 - x2, z2, y2, 16.0 - x1);
                default -> this;
            };
        }
    }
}
