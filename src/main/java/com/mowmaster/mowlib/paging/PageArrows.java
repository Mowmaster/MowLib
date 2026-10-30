package com.mowmaster.mowlib.paging;

import com.mowmaster.mowlib.util.CBPixelBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

// Clickable "turn the page" arrows on a block model: one box for back, one for forward, in pixels as modelled
// facing north. arrowAt rotates them to the block's facing (the same way as a blockstate "y" rotation) and says
// which one the click hit. Use it from useItemOn:
//
//     int turn = ARROWS.arrowAt(state.getValue(FACING), hitResult);
//     if (turn != 0) { if (!level.isClientSide()) { holder.turnPage(turn); PageArrows.playTurnSound(level, pos); }
//                      return InteractionResult.SUCCESS; }
//
// A record is an immutable class whose fields, constructor and getters are generated from its header.
public record PageArrows(CBPixelBox back, CBPixelBox forward, boolean topOnly)
{
    // The arrows on Bibliomania's books: the two far corners of the open pages (model arrows at x 0-2 and 14-16,
    // z 12-14), half a pixel bigger so they're easy to hit, top face only
    public static final PageArrows BOOK_CORNERS = new PageArrows(
            new CBPixelBox(-0.5, 0, 11.5, 2.5, 16, 14.5),
            new CBPixelBox(13.5, 0, 11.5, 16.5, 16, 14.5),
            true);

    // +1 for the forward arrow, -1 for the back arrow, 0 for anywhere else.
    // facing is the block's horizontal facing (north = as modelled).
    public int arrowAt(Direction facing, BlockHitResult hitResult)
    {
        if (topOnly && hitResult.getDirection() != Direction.UP) return 0;
        Vec3 pixel = CBPixelBox.pixelInside(hitResult);
        int turns = CBPixelBox.quarterTurns(facing);
        if (back.rotateY(turns).contains(pixel)) return -1;
        if (forward.rotateY(turns).contains(pixel)) return 1;
        return 0;
    }

    // The vanilla page turn sound, for everyone nearby
    public static void playTurnSound(Level level, BlockPos pos)
    {
        level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1f, 1f);
    }
}
