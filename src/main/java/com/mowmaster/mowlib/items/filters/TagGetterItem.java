package com.mowmaster.mowlib.items.filters;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.filter.TagGetterData;
import com.mowmaster.mowlib.keys.IBlockKeyTool;
import com.mowmaster.mowlib.paging.IPageable;
import com.mowmaster.mowlib.registry.MowLibDataComponents;
import com.mowmaster.mowlib.storage.IInventoryUsable;
import com.mowmaster.mowlib.storage.InventoryClick;
import com.mowmaster.mowlib.storage.InventoryClicks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

// The Tag Getter (mowlib:tag_getter, ported from 1.20): reads the tags of an item (or a block, or the fluid in a
// bucket) and holds one of them selected. Put Tag Getters on a tag filter's list (TagFilterItem): the filter then
// matches anything in each getter's selected tag. The tags live in the mowlib:tag_getter component (TagGetterData).
// - In an inventory: click an item onto it, or it onto an item (left-click), to read that item's tags. The item
//   isn't used up. Scroll over it, or the page keys, to select the next/previous tag (IPageable).
// - In the hand: the pick-up key (MowLibKeyMappings.PICKUP) at a block reads that BLOCK's tags (IBlockKeyTool).
//   Right-click selects the next tag; sneak + right-click clears it. The page keys select the next/previous tag.
// From an item it reads item tags, plus the fluid tags of the fluid inside (a water bucket gives the water tags as
// well), so one getter works for a tag filter in items or fluids mode. From a block it reads block tags (mineable
// with a pickaxe, logs...); the tag filter matches those against the block that an item places.
public class TagGetterItem extends Item implements IPageable, IInventoryUsable, IBlockKeyTool
{
    public TagGetterItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    public static TagGetterData data(ItemStack getter)
    {
        return getter.getOrDefault(MowLibDataComponents.TAG_GETTER.get(), TagGetterData.EMPTY);
    }

    // The selected tag's id, if this is a Tag Getter holding any
    public static Optional<Identifier> selectedTag(ItemStack stack)
    {
        return stack.getItem() instanceof TagGetterItem ? data(stack).selectedTag() : Optional.empty();
    }

    // Every tag of the item, and of the fluid inside it, A to Z. TreeSet: a set kept sorted (and without repeats).
    public static List<Identifier> tagsOf(ItemStack stack)
    {
        Set<Identifier> ids = new TreeSet<>(Comparator.comparing(Identifier::toString));
        // typeHolder: the item's registry entry; tags(): the tags it's in
        stack.typeHolder().tags().forEach(tag -> ids.add(tag.location()));
        FluidStack fluid = FluidUtil.getFirstStackContained(stack);
        if (!fluid.isEmpty()) fluid.getFluid().builtInRegistryHolder().tags().forEach(tag -> ids.add(tag.location()));
        return List.copyOf(ids);
    }

    // A block's tags (its block tags, not its item's), A to Z
    public static List<Identifier> blockTagsOf(BlockState state)
    {
        Set<Identifier> ids = new TreeSet<>(Comparator.comparing(Identifier::toString));
        state.getBlock().builtInRegistryHolder().tags().forEach(tag -> ids.add(tag.location()));
        return List.copyOf(ids);
    }

    // Reads `from`'s tags onto the getter (the first one selected). false if it has none (the getter is left as it was).
    public static boolean readTags(ItemStack getter, ItemStack from)
    {
        if (from.isEmpty()) return false;
        List<Identifier> tags = tagsOf(from);
        if (tags.isEmpty()) return false;
        getter.set(MowLibDataComponents.TAG_GETTER.get(),
                new TagGetterData(Optional.of(BuiltInRegistries.ITEM.getKey(from.getItem())), tags, 0));
        return true;
    }

    // ---- IPageable: the "pages" are the tags; turning a page selects the next/previous tag ----

    @Override
    public int getPageCount(ItemStack stack)
    {
        return Math.max(1, data(stack).tags().size());
    }

    @Override
    public int getPage(ItemStack stack)
    {
        return data(stack).selectedIndex();
    }

    @Override
    public void setPage(ItemStack stack, int page)
    {
        TagGetterData data = data(stack);
        if (!data.isEmpty()) stack.set(MowLibDataComponents.TAG_GETTER.get(), data.withSelected(page));
    }

    // Over the hotbar whenever the selected tag changes in the hand (right-click, the page keys): "Selected tag:
    // #c:ingots", instead of MowLib's plain "Page 2/5" (client/HeldPageMessages)
    @Override
    public Component describePage(ItemStack stack, int page)
    {
        List<Identifier> tags = data(stack).tags();
        if (page < 0 || page >= tags.size()) return Component.translatable(MowLibReferences.MODID + ".taggetter.empty");
        return Component.translatable(MowLibReferences.MODID + ".taggetter.selected", "#" + tags.get(page)).withStyle(ChatFormatting.GOLD);
    }

    // ---- In an inventory: click an item onto it (or it onto an item) to read its tags (InventoryClicks) ----

    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction clickAction, Player player)
    {
        return InventoryClicks.stackedOnOther(this, self, slot, clickAction, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction,
                                            Player player, SlotAccess carriedItem)
    {
        return InventoryClicks.otherStackedOnMe(this, self, other, slot, clickAction, player, carriedItem);
    }

    // Left-click with/onto an item: read its tags (both sides, so the tooltip changes straight away). The item is only
    // looked at. Anything else: the click does what it normally would.
    @Override
    public boolean usedWith(ItemStack self, ItemStack other, ClickAction action, Player player, InventoryClick click)
    {
        if (action != ClickAction.PRIMARY || other.isEmpty()) return false;
        if (readTags(self, other)) InventoryClicks.playInsert(player);
        else InventoryClicks.playFail(player);
        return true;
    }

    // ---- In the hand ----

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack getter = player.getMainHandItem();
        // Nothing to cycle (no tags, not sneaking): decided the same way on both sides
        if (!player.isShiftKeyDown() && data(getter).isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide())
        {
            if (player.isShiftKeyDown())
            {
                getter.remove(MowLibDataComponents.TAG_GETTER.get());
                player.sendOverlayMessage(Component.translatable(MowLibReferences.MODID + ".taggetter.clear").withStyle(ChatFormatting.RED));
            }
            // The next tag. The message over the hotbar comes from describePage (HeldPageMessages sees the change).
            else setPage(getter, data(getter).selectedIndex() + 1);
        }
        return InteractionResult.SUCCESS;
    }

    // ---- The pick-up key at a block: read its block tags (IBlockKeyTool; the place key isn't used) ----

    @Override
    public boolean usesBlockKey(ItemStack stack, Level level, BlockHitResult hit, Player player, BlockKey key)
    {
        return key == BlockKey.PICKUP && !level.getBlockState(hit.getBlockPos()).isAir();
    }

    @Override
    public void onBlockKey(ItemStack stack, ServerPlayer player, BlockHitResult hit, BlockKey key)
    {
        BlockState state = player.level().getBlockState(hit.getBlockPos());
        List<Identifier> tags = blockTagsOf(state);
        if (tags.isEmpty())
        {
            player.sendOverlayMessage(Component.translatable(MowLibReferences.MODID + ".taggetter.none").withStyle(ChatFormatting.RED));
            return;
        }
        // The source is the block's id (the tooltip names the block)
        stack.set(MowLibDataComponents.TAG_GETTER.get(),
                new TagGetterData(Optional.of(BuiltInRegistries.BLOCK.getKey(state.getBlock())), tags, 0));
        player.sendOverlayMessage(Component.translatable(MowLibReferences.MODID + ".taggetter.created", tags.size()).withStyle(ChatFormatting.GREEN));
        player.level().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 1.2f);
    }

    // ---- Tooltip: where the tags came from, then every tag, the selected one in gold ----

    // The source's name: an item's, or for a block without an item of the same id (water, fire...) the block's
    private static Component sourceName(Identifier id)
    {
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item != Items.AIR) return new ItemStack(item).getHoverName();
        return BuiltInRegistries.BLOCK.getValue(id).getName();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        TagGetterData data = data(stack);
        if (data.isEmpty())
        {
            tooltip.accept(Component.translatable(MowLibReferences.MODID + ".taggetter.tooltip_howto").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        data.source().ifPresent(id -> tooltip.accept(Component.translatable(MowLibReferences.MODID + ".taggetter.tooltip_header",
                sourceName(id)).withStyle(ChatFormatting.GRAY)));
        int selected = data.selectedIndex();
        for (int index = 0; index < data.tags().size(); index++)
        {
            boolean isSelected = index == selected;
            tooltip.accept(Component.literal((isSelected ? "> #" : "  #") + data.tags().get(index))
                    .withStyle(isSelected ? ChatFormatting.GOLD : ChatFormatting.WHITE));
        }
    }
}
