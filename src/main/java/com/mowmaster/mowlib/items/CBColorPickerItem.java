package com.mowmaster.mowlib.items;

import com.mowmaster.mowlib.tint.CBTintBlockEntity;
import com.mowmaster.mowlib.tint.IColorableBlock;
import com.mowmaster.mowlib.datamaps.CBBlockColor;
import com.mowmaster.mowlib.datamaps.CBDataMaps;
import com.mowmaster.mowlib.registry.MowLibDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.mowmaster.mowlib.tint.ITintHolder;
import org.jetbrains.annotations.Nullable;

// Right-click a block to store its colour (and which block it was); craft the picker on its own for a Custom Dye of
// that colour, named after the block ("Diamond Block Dye"). Sneak + right-click the air to clear it.
public class CBColorPickerItem extends Item
{
    public CBColorPickerItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside());
        Integer color = pickColor(level, pos, state, hit);
        Player player = context.getPlayer();

        if (!level.isClientSide())
        {
            if (color == null)
            {
                if (player != null) player.sendOverlayMessage(Component.translatable("message.mowlib.color_picker.none"));
            }
            else
            {
                context.getItemInHand().set(DataComponents.DYED_COLOR, new DyedItemColor(color));
                context.getItemInHand().set(MowLibDataComponents.PICKED_BLOCK.get(), state.getBlock());
                if (player != null)
                {
                    player.sendOverlayMessage(Component.translatable("message.mowlib.color_picker.picked",
                            String.format("#%06X", color)));
                }
                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1f, 1.5f);
            }
        }
        return InteractionResult.SUCCESS;
    }

    // Right-click with nothing targeted. Clicking a block goes to useOn instead, so this only runs on air
    // (or an entity that doesn't react to the click).
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        ItemStack picker = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !picker.has(DataComponents.DYED_COLOR)) return InteractionResult.PASS;

        if (!level.isClientSide())
        {
            // Removing the component makes it a plain picker again (and the item model goes back to the empty one)
            picker.remove(DataComponents.DYED_COLOR);
            picker.remove(MowLibDataComponents.PICKED_BLOCK.get());
            player.sendOverlayMessage(Component.translatable("message.mowlib.color_picker.cleared"));
            level.playSound(null, player.blockPosition(), SoundEvents.DYE_USE, SoundSource.PLAYERS, 1f, 0.5f);
        }
        // SUCCESS on both sides so the client swings the arm
        return InteractionResult.SUCCESS;
    }

    // A colourable block gives the colour of the clicked region; anything else uses the block_colors data map
    @Nullable
    public static Integer pickColor(Level level, BlockPos pos, BlockState state, BlockHitResult hit)
    {
        if (state.getBlock() instanceof IColorableBlock colorable && level.getBlockEntity(pos) instanceof ITintHolder tintEntity)
        {
            int region = colorable.getTintRegion(state, hit);
            int color = tintEntity.getColor(region);
            return color == CBTintBlockEntity.UNDYED ? colorable.getDefaultColor(region) : color;
        }
        CBBlockColor entry = state.getBlock().builtInRegistryHolder().getData(CBDataMaps.BLOCK_COLORS);
        return entry != null ? entry.rgb() : null;
    }

    // Left in the crafting grid after making a dye: a fresh picker with no colour stored
    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance)
    {
        return new ItemStackTemplate(this, 1);
    }
}
