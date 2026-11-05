package com.mowmaster.mowlib.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

// A base for block entity renderers that draw items (books on shelves, items on faces, a block's display item).
// It keeps the ItemModelResolver every such renderer needs and adds two shortcuts:
// - resolve(...): fills an ItemStackRenderState for a stack in extractRenderState (EMPTY clears it)
// - newStates(n): an array of n fresh ItemStackRenderStates for a render state class to hold
// Subclasses still write createRenderState, extractRenderState and submit, as the 26.x renderers do:
//
//   public class MyRenderer extends CBItemRenderer<MyBlockEntity, MyRenderer.State> {
//       public MyRenderer(BlockEntityRendererProvider.Context context) { super(context); }
//       @Override public State createRenderState() { return new State(); }
//       @Override public void extractRenderState(MyBlockEntity be, State state, ...) {
//           BlockEntityRenderer.super.extractRenderState(be, state, ...);
//           resolve(state.items[0], be.getItem(), ItemDisplayContext.FIXED, be.getLevel(), 0);
//       }
//       public static class State extends BlockEntityRenderState { final ItemStackRenderState[] items = newStates(1); }
//   }
public abstract class CBItemRenderer<T extends BlockEntity, S extends BlockEntityRenderState> implements BlockEntityRenderer<T, S>
{
    protected final ItemModelResolver itemModelResolver;

    protected CBItemRenderer(BlockEntityRendererProvider.Context context)
    {
        this.itemModelResolver = context.itemModelResolver();
    }

    // seed: varies the model for items whose look is random (give each item drawn its own number)
    protected void resolve(ItemStackRenderState into, ItemStack stack, ItemDisplayContext context, @Nullable Level level, int seed)
    {
        itemModelResolver.updateForTopItem(into, stack, context, level, null, seed);
    }

    public static ItemStackRenderState[] newStates(int count)
    {
        ItemStackRenderState[] states = new ItemStackRenderState[count];
        for (int index = 0; index < count; index++) states[index] = new ItemStackRenderState();
        return states;
    }
}
