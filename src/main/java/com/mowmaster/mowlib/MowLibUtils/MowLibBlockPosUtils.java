package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;

public class MowLibBlockPosUtils {
    // Returns true if `posOne` and `posTwo` are at most `range` blocks apart.
    public boolean arePositionsInRange(BlockPos posOne, BlockPos posTwo, int range) {
        BlockPos distanceVector = posOne.subtract(posTwo);
        return Math.abs(distanceVector.getX()) <= range &&
            Math.abs(distanceVector.getY()) <= range &&
            Math.abs(distanceVector.getZ()) <= range;
    }

    public BlockPos getPosOfBlockBelow(Level level, BlockPos posOfPedestal, int numBelow)
    {
        BlockState state = level.getBlockState(posOfPedestal);

        Direction enumfacing = (state.hasProperty(BlockStateProperties.FACING))?(state.getValue(BlockStateProperties.FACING)):(Direction.UP);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.offset(0,-numBelow,0);
            case DOWN:
                return blockBelow.offset(0,numBelow,0);
            case NORTH:
                return blockBelow.offset(0,0,numBelow);
            case SOUTH:
                return blockBelow.offset(0,0,-numBelow);
            case EAST:
                return blockBelow.offset(-numBelow,0,0);
            case WEST:
                return blockBelow.offset(numBelow,0,0);
            default:
                return blockBelow;
        }
    }

    //ToDo: Add to mowlib and remove from here
    //returns true for an add, false for a remove.
    public static boolean addBlockPosToList(String ModID, String identifier, ItemStack upgrade, BlockPos posOfBlock)
    {
        List<BlockPos> currentList = readBlockPosListFromNBT(ModID, identifier, upgrade);
        if(currentList.contains(posOfBlock))
        {
            currentList.remove(posOfBlock);
            saveBlockPosListCustomToNBT(ModID, identifier, upgrade,currentList);
            return false;
        }
        else
        {
            currentList.add(posOfBlock);
            saveBlockPosListCustomToNBT(ModID, identifier, upgrade,currentList);
            return true;
        }
    }

    //ToDo: Add to mowlib and remove from here
    public static List<BlockPos> readBlockPosListFromNBT(String ModID, String identifier, ItemStack upgrade) {
        List<BlockPos> posList = new ArrayList<>();
        if(upgrade.hasTag())
        {
            String tagX = ModID + identifier +"_X";
            String tagY = ModID + identifier +"_Y";
            String tagZ = ModID + identifier +"_Z";
            CompoundTag getCompound = upgrade.getTag();
            if(upgrade.getTag().contains(tagX) && upgrade.getTag().contains(tagY) && upgrade.getTag().contains(tagZ))
            {
                int[] storedIX = getCompound.getIntArray(tagX);
                int[] storedIY = getCompound.getIntArray(tagY);
                int[] storedIZ = getCompound.getIntArray(tagZ);

                for(int i=0;i<storedIX.length;i++)
                {
                    BlockPos gotPos = new BlockPos(storedIX[i],storedIY[i],storedIZ[i]);
                    posList.add(gotPos);
                }
            }
        }
        return posList;
    }

    public static void saveBlockPosListCustomToNBT(String ModID, String identifier, ItemStack upgrade, List<BlockPos> posListToSave)
    {
        CompoundTag compound = new CompoundTag();
        if(upgrade.hasTag())
        {
            compound = upgrade.getTag();
        }
        List<Integer> storedX = new ArrayList<Integer>();
        List<Integer> storedY = new ArrayList<Integer>();
        List<Integer> storedZ = new ArrayList<Integer>();

        for(int i=0;i<posListToSave.size();i++)
        {
            storedX.add(posListToSave.get(i).getX());
            storedY.add(posListToSave.get(i).getY());
            storedZ.add(posListToSave.get(i).getZ());
        }

        compound.putIntArray(ModID + identifier +"_X",storedX);
        compound.putIntArray(ModID + identifier +"_Y",storedY);
        compound.putIntArray(ModID + identifier +"_Z",storedZ);
        upgrade.setTag(compound);
    }

    public static List<BlockPos> readBlockPosListCustomFromNBT(String ModID, String identifier, ItemStack upgrade) {
        List<BlockPos> posList = new ArrayList<>();
        if(upgrade.hasTag())
        {
            String tagX = ModID + identifier +"_X";
            String tagY = ModID + identifier +"_Y";
            String tagZ = ModID + identifier +"_Z";
            CompoundTag getCompound = upgrade.getTag();
            if(upgrade.getTag().contains(tagX) && upgrade.getTag().contains(tagY) && upgrade.getTag().contains(tagZ))
            {
                int[] storedIX = getCompound.getIntArray(tagX);
                int[] storedIY = getCompound.getIntArray(tagY);
                int[] storedIZ = getCompound.getIntArray(tagZ);

                for(int i=0;i<storedIX.length;i++)
                {
                    BlockPos gotPos = new BlockPos(storedIX[i],storedIY[i],storedIZ[i]);
                    posList.add(gotPos);
                }
            }
        }
        return posList;
    }

    public void removeBlockListCustomNBTTags(String ModID, String identifier, ItemStack upgrade)
    {
        String tagX = ModID + identifier +"_X";
        String tagY = ModID + identifier +"_Y";
        String tagZ = ModID + identifier +"_Z";
        CompoundTag getTags = upgrade.getTag();
        if(getTags.contains(tagX))getTags.remove(tagX);
        if(getTags.contains(tagY))getTags.remove(tagY);
        if(getTags.contains(tagZ))getTags.remove(tagZ);
        upgrade.setTag(getTags);
    }

    public boolean hasBlockListCustomNBTTags(String ModID, String identifier, ItemStack upgrade)
    {
        String tagX = ModID + identifier +"_X";
        String tagY = ModID + identifier +"_Y";
        String tagZ = ModID + identifier +"_Z";
        CompoundTag getTags = upgrade.getTag();

        return getTags.contains(tagX) && getTags.contains(tagY) && getTags.contains(tagZ);
    }

    //ToDo: Add to mowlib and remove from here
    public static void saveBlockPosToNBT(String ModID, String identifier, ItemStack upgrade, int num, BlockPos posToSave)
    {
        CompoundTag compound = new CompoundTag();
        if(upgrade.hasTag())
        {
            compound = upgrade.getTag();
        }
        List<Integer> listed = new ArrayList<>();
        listed.add(posToSave.getX());
        listed.add(posToSave.getY());
        listed.add(posToSave.getZ());
        compound.putIntArray(ModID + identifier +num, listed);
        upgrade.setTag(compound);
    }

    //ToDo: Add to mowlib and remove from here
    public static BlockPos readBlockPosFromNBT(String ModID, String identifier, ItemStack upgrade, int num) {
        if(upgrade.hasTag())
        {
            String tag = ModID + identifier + num;
            CompoundTag getCompound = upgrade.getTag();
            if(upgrade.getTag().contains(tag))
            {
                int[] listed = getCompound.getIntArray(tag);
                if(listed.length>=3)return new BlockPos(listed[0],listed[1],listed[2]);
            }
        }
        return BlockPos.ZERO;
    }

    //ToDo: Add to mowlib and remove from here
    public static BlockPos getBlockPosOnUpgrade(String ModID, String identifier, ItemStack stack, int num) {

        return readBlockPosFromNBT(ModID, identifier, stack,num);
    }
}
