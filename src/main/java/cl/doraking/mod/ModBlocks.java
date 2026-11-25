package cl.doraking.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
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

    /**
     * Puesto Doraking: estructura de 3 de ancho x 3 de alto x 5 de largo (45 bloques).
     * Se coloca UN solo item; el bloque donde haces clic es el centro del frente en la base.
     * El frente mira hacia ti. Necesita todo ese espacio libre. Si se rompe una parte, desaparece todo.
     */
    public static class PuestoBlock extends HorizontalDirectionalBlock {
        public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
        public static final IntegerProperty PART = IntegerProperty.create("part", 0, 44);
        public static final int W = 3, H = 3, D = 5;
        /** Indice del bloque ancla (ix=1, iy=0, iz=0): el que suelta el item al romperse. */
        public static final int ANCHOR = 1;

        // cajas con colision (x1,y1,z1,x2,y2,z2) en pixeles, mirando al norte; estructura de 48 x 48 x 80
        private static final double[][] BOXES = {
                {0, 0, 0, 48, 18, 10},      // mostrador
                {0, 0, 74, 48, 44, 80},     // pared del fondo
                {0, 0, 10, 48, 1, 74},      // piso
                {6, 0, 40, 26, 15, 52},     // mesa con plancha
                {36, 0, 40, 44, 11, 48}     // canasto
        };
        private static final VoxelShape[][] SHAPES = new VoxelShape[4][W * H * D];

        static {
            for (int turns = 0; turns < 4; turns++) {
                for (int idx = 0; idx < W * H * D; idx++) {
                    int ix = idx % W, iy = (idx / W) % H, iz = idx / (W * H);
                    VoxelShape shape = Shapes.empty();
                    for (double[] b : BOXES) {
                        double x1 = Math.max(b[0], ix * 16), x2 = Math.min(b[3], ix * 16 + 16);
                        double y1 = Math.max(b[1], iy * 16), y2 = Math.min(b[4], iy * 16 + 16);
                        double z1 = Math.max(b[2], iz * 16), z2 = Math.min(b[5], iz * 16 + 16);
                        if (x2 - x1 <= 0 || y2 - y1 <= 0 || z2 - z1 <= 0) continue;
                        x1 -= ix * 16; x2 -= ix * 16; y1 -= iy * 16; y2 -= iy * 16; z1 -= iz * 16; z2 -= iz * 16;
                        for (int t = 0; t < turns; t++) {
                            double nx1 = 16 - z2, nx2 = 16 - z1, nz1 = x1, nz2 = x2;
                            x1 = nx1; x2 = nx2; z1 = nz1; z2 = nz2;
                        }
                        shape = Shapes.or(shape, Block.box(x1, y1, z1, x2, y2, z2));
                    }
                    SHAPES[turns][idx] = shape;
                }
            }
        }

        public PuestoBlock(Properties props) {
            super(props);
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, ANCHOR));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING, PART);
        }

        public static int index(int ix, int iy, int iz) {
            return ix + W * (iy + H * iz);
        }

        /** ix crece hacia la derecha del frente (sentido horario), iy hacia arriba, iz hacia el fondo. */
        private static BlockPos cellPos(BlockPos anchor, Direction facing, int ix, int iy, int iz) {
            return anchor.relative(facing.getClockWise(), ix - 1).above(iy).relative(facing.getOpposite(), iz);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext ctx) {
            Direction facing = ctx.getHorizontalDirection().getOpposite();
            BlockPos anchor = ctx.getClickedPos();
            Level level = ctx.getLevel();
            for (int iz = 0; iz < D; iz++)
                for (int iy = 0; iy < H; iy++)
                    for (int ix = 0; ix < W; ix++) {
                        BlockPos p = cellPos(anchor, facing, ix, iy, iz);
                        if (level.isOutsideBuildHeight(p) || !level.getBlockState(p).canBeReplaced(ctx)) return null;
                    }
            return defaultBlockState().setValue(FACING, facing).setValue(PART, ANCHOR);
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
            if (level.isClientSide) return;
            Direction facing = state.getValue(FACING);
            for (int iz = 0; iz < D; iz++)
                for (int iy = 0; iy < H; iy++)
                    for (int ix = 0; ix < W; ix++) {
                        int idx = index(ix, iy, iz);
                        if (idx == ANCHOR) continue;
                        // flags 2|16: sin actualizar vecinos mientras se construye
                        level.setBlock(cellPos(pos, facing, ix, iy, iz), state.setValue(PART, idx), 18);
                    }
        }

        @Override
        public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor,
                                      LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
            Direction f = state.getValue(FACING);
            int p = state.getValue(PART);
            int ix = p % W, iy = (p / W) % H, iz = p / (W * H);
            int dx = 0, dy = 0, dz = 0;
            if (dir == f.getClockWise()) dx = 1;
            else if (dir == f.getCounterClockWise()) dx = -1;
            else if (dir == Direction.UP) dy = 1;
            else if (dir == Direction.DOWN) dy = -1;
            else if (dir == f.getOpposite()) dz = 1;
            else if (dir == f) dz = -1;
            int nx = ix + dx, ny = iy + dy, nz = iz + dz;
            if (nx >= 0 && nx < W && ny >= 0 && ny < H && nz >= 0 && nz < D) {
                boolean ok = neighbor.is(this) && neighbor.getValue(FACING) == f
                        && neighbor.getValue(PART) == index(nx, ny, nz);
                if (!ok) return Blocks.AIR.defaultBlockState();
            }
            return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
        }

        /** El ancla suelta el item por loot table; cualquier otra parte lo suelta aqui (una sola vez). */
        @Override
        public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
            if (!level.isClientSide && !player.isCreative() && state.getValue(PART) != ANCHOR) {
                popResource(level, pos, new ItemStack(ModItems.PUESTO.get()));
            }
            super.playerWillDestroy(level, pos, state, player);
        }

        private static int turns(Direction f) {
            return switch (f) { case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; };
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
            return SHAPES[turns(state.getValue(FACING))][state.getValue(PART)];
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
            return SHAPES[turns(state.getValue(FACING))][state.getValue(PART)];
        }
    }
}
