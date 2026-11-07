package com.mowmaster.mowlib.link;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.client.MowLibKeyMappings;
import com.mowmaster.mowlib.keys.IActivatable;
import com.mowmaster.mowlib.registry.MowLibDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

// A linking tool for any ILinkable (mods register their own item with this class, or a subclass):
//   Right-click a linkable:          save it for one link (used up by the next link)
//   Sneak + right-click a linkable:  save it for many links, until sneak + right-clicked again (the same one or the
//                                    air clears it; another linkable is saved instead)
//   Right-click another linkable with one saved: link them (again removes the link). Which way depends on the mode:
//     Send To      (default)  the saved one sends to the clicked one
//     Receive From            the clicked one sends to the saved one
//   The activation key (MowLibKeyMappings.ACTIVATE, V by default) switches the mode (IActivatable;
//   mowlib:activated = Receive From).
// While one is held, client/LinkBeams shows link beams. The saved block is mowlib:linked_pos, mowlib:link_sticky
// marks a many-links save, and the glint shows something is saved.
// A block whose right-click would take the tool (an inventory) should hand the click on: return PASS from its
// useItemOn when the stack is a LinkingToolItem.
public class LinkingToolItem extends Item implements IActivatable
{
    private static final String MODID = MowLibReferences.MODID;

    public LinkingToolItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    // ---- What's saved ----

    public static @Nullable BlockPos getSelected(ItemStack tool)
    {
        return tool.get(MowLibDataComponents.LINKED_POS.get());
    }

    public static boolean isSticky(ItemStack tool)
    {
        return tool.has(MowLibDataComponents.LINK_STICKY.get());
    }

    // Receive From mode (the activation key's on/off state)
    public static boolean isReceiving(ItemStack tool)
    {
        return IActivatable.isActive(tool);
    }

    public static void select(ItemStack tool, BlockPos pos, boolean sticky)
    {
        tool.set(MowLibDataComponents.LINKED_POS.get(), pos.immutable());
        if (sticky) tool.set(MowLibDataComponents.LINK_STICKY.get(), Unit.INSTANCE);
        else tool.remove(MowLibDataComponents.LINK_STICKY.get());
        tool.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    public static void clear(ItemStack tool)
    {
        tool.remove(MowLibDataComponents.LINKED_POS.get());
        tool.remove(MowLibDataComponents.LINK_STICKY.get());
        tool.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }

    // ---- Linking ----

    // Links the saved block and the one at pos, the way the mode says (again: removes the link). A one-link save is
    // used up when something changed. Returns false if nothing is saved. Also useful when placing a linkable with
    // the tool in the offhand.
    public boolean linkWith(Level level, Player player, ItemStack tool, BlockPos pos)
    {
        BlockPos selected = getSelected(tool);
        if (selected == null) return false;
        boolean receiving = isReceiving(tool);
        BlockPos senderPos = receiving ? pos : selected;
        BlockPos receiverPos = receiving ? selected : pos;
        if (!(level.getBlockEntity(senderPos) instanceof ILinkable sender))
        {
            // The saved block was broken
            clearWithMessage(player, tool);
            return true;
        }
        LinkResult result = sender.getLinkList().toggle(receiverPos);
        player.sendOverlayMessage(result.message(receiving));
        if (result.changed() && !isSticky(tool)) clear(tool);
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        ItemStack tool = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        BlockPos selected = getSelected(tool);

        // Not a linkable: sneaking clears the save, otherwise nothing happens
        if (!(level.getBlockEntity(pos) instanceof ILinkable))
        {
            if (!player.isShiftKeyDown() || selected == null) return InteractionResult.PASS;
            if (!level.isClientSide()) clearWithMessage(player, tool);
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown())
        {
            if (selected != null && isSticky(tool) && selected.equals(pos)) clearWithMessage(player, tool);
            else saveWithMessage(player, tool, pos, true);
        }
        else if (selected == null)
        {
            saveWithMessage(player, tool, pos, false);
        }
        else if (selected.equals(pos))
        {
            // The saved block itself: a one-link save is cancelled; a block can't link to itself
            if (isSticky(tool)) player.sendOverlayMessage(LinkResult.SELF.message(false));
            else clearWithMessage(player, tool);
        }
        else
        {
            linkWith(level, player, tool, pos);
        }
        return InteractionResult.SUCCESS;
    }

    // Sneak + right-click the air: clear the save
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        ItemStack tool = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || getSelected(tool) == null) return super.use(level, player, hand);
        if (!level.isClientSide()) clearWithMessage(player, tool);
        return InteractionResult.SUCCESS;
    }

    protected void saveWithMessage(Player player, ItemStack tool, BlockPos pos, boolean sticky)
    {
        select(tool, pos, sticky);
        player.sendOverlayMessage(Component.translatable("message." + MODID + ".link." + (sticky ? "saved_sticky" : "saved_once"),
                pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.AQUA));
    }

    protected void clearWithMessage(Player player, ItemStack tool)
    {
        clear(tool);
        player.sendOverlayMessage(Component.translatable("message." + MODID + ".link.cleared"));
    }

    // ---- The activation key: Send To <-> Receive From ----

    @Override
    public boolean canActivate(ItemStack stack)
    {
        return true;
    }

    @Override
    public void activate(ItemStack stack, ServerPlayer player)
    {
        IActivatable.toggle(stack, player, "message." + MODID + ".link.mode_receive", "message." + MODID + ".link.mode_send");
    }

    // ---- Tooltip ----

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("tooltip." + MODID + ".link." + (isReceiving(stack) ? "mode_receive" : "mode_send")).withStyle(ChatFormatting.GOLD));
        BlockPos selected = getSelected(stack);
        tooltip.accept(selected == null
                ? Component.translatable("tooltip." + MODID + ".link.unselected").withStyle(ChatFormatting.GRAY)
                : Component.translatable("tooltip." + MODID + ".link." + (isSticky(stack) ? "selected_sticky" : "selected"),
                        selected.getX(), selected.getY(), selected.getZ()).withStyle(ChatFormatting.GRAY));
        // Component.keybind: the key's current name (whatever the player bound it to). ACTIVATE_NAME is a constant
        // string, so this doesn't load the client-only key mappings class on a server.
        tooltip.accept(Component.translatable("tooltip." + MODID + ".link.howto", Component.keybind(MowLibKeyMappings.ACTIVATE_NAME)).withStyle(ChatFormatting.DARK_GRAY));
    }
}
