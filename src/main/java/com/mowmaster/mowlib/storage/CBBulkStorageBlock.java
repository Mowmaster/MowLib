package com.mowmaster.mowlib.storage;

import com.mowmaster.mowlib.tint.IColorableBlock;
import com.mowmaster.mowlib.items.CBColorPickerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;

// Base for bulk storage blocks: one kind of item (matching data), lots of it, no GUI.
// - Right-click with an allowed item: stores as much of the held stack as fits (decorated pot sound and dust)
// - Left-click: takes 1 out; sneak + left-click: a full stack (64). Creative players take items out the same way,
//   and can only break it once it's empty.
// - Breaking it spills the contents. Hoppers and pipes can insert and extract (see MowLibCapabilities).
// Putting in and taking out go through StorageInteractions, the same helpers every MowLib storage uses.
// Subclasses set the shape and tint regions for their model; capacity and the allowed-item tag come in through
// the constructor, so new storage blocks are mostly a model plus a registry line.
public class CBBulkStorageBlock extends Block implements IColorableBlock
{
    // Downward speed of the dust when taking items out (the dust plume particle drifts upward on its own)
    private static final double EXTRACT_DUST_SPEED = 0.3;

    private final int capacity;
    private final TagKey<Item> allowed;

    public CBBulkStorageBlock(Properties properties, int capacity, TagKey<Item> allowed)
    {
        super(properties);
        this.capacity = capacity;
        this.allowed = allowed;
    }



    // ---- What it stores ----

    // Items per slot
    public int capacity()
    {
        return capacity;
    }

    // Number of separate item kinds it can hold at once (1 for single-item storage)
    public int slots()
    {
        return 1;
    }

    public boolean accepts(ItemResource resource)
    {
        return resource.typeHolder().is(allowed);
    }

    // Height (in blocks) of the top of the model, where the dust appears
    protected double topHeight()
    {
        return 1.0;
    }

    // ---- Block entity ----

    @Override
    public boolean usesTintBlockEntity()
    {
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new CBBulkStorageBlockEntity(pos, state);
    }

    // ---- Putting items in ----

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        // Dyes (and the colour picker) colour the block as usual
        if (stack.getItem() instanceof CBColorPickerItem || IColorableBlock.dyeColorOf(stack) != null)
        {
            return tryDyeBlock(stack, state, level, pos, player, hitResult);
        }
        // Anything it can't store behaves normally (e.g. placing a block against it)
        if (stack.isEmpty() || !accepts(ItemResource.of(stack))) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(level.getBlockEntity(pos) instanceof CBBulkStorageBlockEntity storage)) return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (level instanceof ServerLevel serverLevel)
        {
            CBBulkItemHandler items = storage.getItems();
            int inserted = StorageInteractions.insertStack(items, stack, false);
            if (inserted > 0)
            {
                stack.consume(inserted, player); // creative players keep their stack
                // Same pitch rise as the decorated pot as it fills
                level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1f, 0.7f + 0.5f * items.fullness());
                serverLevel.sendParticles(ParticleTypes.DUST_PLUME,
                        pos.getX() + 0.5, pos.getY() + topHeight() + 0.2, pos.getZ() + 0.5, 7, 0, 0, 0, 0);
            }
            else
            {
                // Full, or already holding a different item
                level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1f, 1f);
            }
        }
        return InteractionResult.SUCCESS;
    }

    // ---- Taking items out ----

    // Survival left-click (each click; holding the button still mines the block slowly)
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player)
    {
        if (level instanceof ServerLevel serverLevel) extract(serverLevel, pos, player);
    }

    // Creative players break blocks instantly instead of "attacking" them, so take items out here, and only let
    // the block break once it's empty. On the client we don't know the contents: let the server decide.
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack,
                                       boolean willHarvest, FluidState fluid)
    {
        if (player.isCreative())
        {
            if (level.isClientSide()) return false;
            if (level.getBlockEntity(pos) instanceof CBBulkStorageBlockEntity storage && !storage.getItems().isEmptyStorage())
            {
                extract((ServerLevel) level, pos, player);
                return false;
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, toolStack, willHarvest, fluid);
    }

    private void extract(ServerLevel level, BlockPos pos, Player player)
    {
        if (!(level.getBlockEntity(pos) instanceof CBBulkStorageBlockEntity storage)) return;
        // 1, or a full stack when sneaking, into the inventory or dropped at the player's feet if it's full.
        // (This used to call Inventory#add, which doesn't drop anything: with a full inventory the items were lost.)
        ItemStack taken = StorageInteractions.takeToPlayer(player, storage.getItems());
        if (taken.isEmpty()) return;
        level.playSound(null, pos, SoundEvents.DECORATED_POT_BREAK, SoundSource.BLOCKS, 1f, 0.7f + 0.5f * storage.getItems().fullness());

        // The insert dust in reverse: starts above and around the top, moving down into the pile
        RandomSource random = level.getRandom();
        double centreX = pos.getX() + 0.5;
        double centreZ = pos.getZ() + 0.5;
        double top = pos.getY() + topHeight();
        for (int i = 0; i < 7; i++)
        {
            double dx = (random.nextDouble() - 0.5) * 0.6;
            double dz = (random.nextDouble() - 0.5) * 0.6;
            // A count of 0 spawns one particle and uses the offset as its direction (times the speed)
            level.sendParticles(ParticleTypes.DUST_PLUME, centreX + dx, top + 0.5, centreZ + dz, 0,
                    -dx, -1, -dz, EXTRACT_DUST_SPEED);
        }
    }

    // ---- Comparators: 0 when empty, 1-15 by how full ----

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction)
    {
        if (!(level.getBlockEntity(pos) instanceof CBBulkStorageBlockEntity storage)) return 0;
        CBBulkItemHandler items = storage.getItems();
        return items.isEmptyStorage() ? 0 : 1 + Math.round(items.fullness() * 14);
    }
}
