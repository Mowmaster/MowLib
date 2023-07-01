package com.mowmaster.mowlib.EventHandlers;

import com.mowmaster.mowlib.Items.BaseUseInteractionItem;
import com.mowmaster.mowlib.MowLibUtils.MowLibContainerUtils;
import com.mowmaster.mowlib.Recipes.ToolSwapCrafting;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber
public class ToolSwapCraftingHandler
{

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void toolSwapCrafting(PlayerInteractEvent.RightClickItem event) {

        Level level = event.getLevel();
        Player player = event.getEntity();

        if(!level.isClientSide())
        {
            HitResult result = player.pick(5,0,false);
            if(result.getType().equals(HitResult.Type.MISS))
            {
                if(player.getMainHandItem() != null && event.getHand().equals(InteractionHand.MAIN_HAND))
                {
                    if(player.getMainHandItem().getItem() instanceof BaseUseInteractionItem)
                    {
                        if(player.isShiftKeyDown())
                        {
                            ToolSwapCrafting getRecipe = getRecipe(level,player.getMainHandItem());
                            if(getRecipe != null)
                            {
                                ItemStack getResultItem = getBlockItemResult(getRecipe).stream().findFirst().get().copy();
                                if(getResultItem != null)
                                {
                                    player.setItemInHand(InteractionHand.MAIN_HAND,getResultItem);
                                }
                            }
                        }
                    }
                }
            }
        }

    }



    @Nullable
    protected static ToolSwapCrafting getRecipe(Level level, ItemStack mainHandItem) {
        Container cont = MowLibContainerUtils.getContainer(1);
        cont.setItem(-1,mainHandItem);
        List<ToolSwapCrafting> recipes = level.getRecipeManager().getRecipesFor(ToolSwapCrafting.Type.INSTANCE,cont,level);
        return recipes.size() > 0 ? level.getRecipeManager().getRecipesFor(ToolSwapCrafting.Type.INSTANCE,cont,level).get(0) : null;
    }

    protected static Collection<ItemStack> getBlockItemResult(ToolSwapCrafting recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResultItem()));
    }
}