package cl.doraking.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Doraking.MODID);

    public static final RegistryObject<Block> PUESTO = BLOCKS.register("puesto_doraking",
            () -> new PuestoBlock(Block.Properties.of()
                    .mapColor(MapColor.COLOR_RED).strength(2.0f).sound(SoundType.WOOD)
                    .noOcclusion().pushReaction(PushReaction.BLOCK)));

    public enum Part implements StringRepresentable {
        LEFT("left"), CENTER("center"), RIGHT("right");
        private final String name;
        Part(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    /**
     * Puesto Doraking de 3 bloques de ancho. Se coloca el bloque central y aparecen
     * automaticamente los dos laterales. Si se rompe uno, desaparece todo.
     */
    public static class PuestoBlock extends HorizontalDirectionalBlock {
        public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
        public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

        public PuestoBlock(Properties props) {
            super(props);
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, Part.CENTER));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING, PART);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext ctx) {
            Direction facing = ctx.getHorizontalDirection().getOpposite();
            BlockPos pos = ctx.getClickedPos();
            Level level = ctx.getLevel();
            BlockPos left = pos.relative(facing.getCounterClockWise());
            BlockPos right = pos.relative(facing.getClockWise());
            if (!level.getBlockState(left).canBeReplaced(ctx) || !level.getBlockState(right).canBeReplaced(ctx)) {
                return null;
            }
            return defaultBlockState().setValue(FACING, facing).setValue(PART, Part.CENTER);
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
            if (level.isClientSide) return;
            Direction facing = state.getValue(FACING);
            level.setBlock(pos.relative(facing.getCounterClockWise()),
                    state.setValue(PART, Part.LEFT), 3);
            level.setBlock(pos.relative(facing.getClockWise()),
                    state.setValue(PART, Part.RIGHT), 3);
        }

        @Override
        public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor,
                                      LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
            Direction facing = state.getValue(FACING);
            Part part = state.getValue(PART);
            if (part == Part.CENTER) {
                if (dir == facing.getCounterClockWise() && !isPart(neighbor, Part.LEFT, facing)) return Blocks.AIR.defaultBlockState();
                if (dir == facing.getClockWise() && !isPart(neighbor, Part.RIGHT, facing)) return Blocks.AIR.defaultBlockState();
            } else {
                Direction toCenter = part == Part.LEFT ? facing.getClockWise() : facing.getCounterClockWise();
                if (dir == toCenter && !isPart(neighbor, Part.CENTER, facing)) return Blocks.AIR.defaultBlockState();
            }
            return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
        }

        private boolean isPart(BlockState s, Part part, Direction facing) {
            return s.is(this) && s.getValue(PART) == part && s.getValue(FACING) == facing;
        }

        /** Si el jugador rompe un lateral, igual recibe el puesto (el central lo suelta por loot table). */
        @Override
        public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
            if (!level.isClientSide && !player.isCreative() && state.getValue(PART) != Part.CENTER) {
                popResource(level, pos, new ItemStack(ModItems.PUESTO.get()));
            }
            super.playerWillDestroy(level, pos, state, player);
        }

        // ---- forma: mostrador al frente + pared del fondo (se rota segun FACING)
        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
            return shape(state.getValue(FACING));
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
            return shape(state.getValue(FACING));
        }

        private static VoxelShape shape(Direction facing) {
            VoxelShape counter = rotated(facing, 0, 0, 0, 16, 12, 7);
            VoxelShape back = rotated(facing, 0, 0, 14, 16, 22, 16);
            return Shapes.or(counter, back);
        }

        /** Caja definida mirando al norte, rotada 90 grados por cada paso hacia el este/sur/oeste. */
        private static VoxelShape rotated(Direction facing, double x1, double y1, double z1,
                                          double x2, double y2, double z2) {
            int turns = switch (facing) { case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; };
            for (int i = 0; i < turns; i++) {
                double nx1 = 16 - z2, nx2 = 16 - z1, nz1 = x1, nz2 = x2;
                x1 = nx1; x2 = nx2; z1 = nz1; z2 = nz2;
            }
            return Block.box(x1, y1, z1, x2, y2, z2);
        }
    }
}
