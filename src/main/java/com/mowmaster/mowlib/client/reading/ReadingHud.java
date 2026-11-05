package com.mowmaster.mowlib.client.reading;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.client.HudAnchor;
import com.mowmaster.mowlib.client.MowLibClientConfig;
import com.mowmaster.mowlib.client.SlotGrid;
import com.mowmaster.mowlib.paging.IPagedStorageItem;
import com.mowmaster.mowlib.paging.PagedContents;
import com.mowmaster.mowlib.reading.IReadableItem;
import com.mowmaster.mowlib.reading.ReadingContext;
import com.mowmaster.mowlib.reading.ReadingLabels;
import com.mowmaster.mowlib.reading.ReadingPaper;
import com.mowmaster.mowlib.reading.ReadingPapers;
import com.mowmaster.mowlib.reading.ReadingPayloads;
import com.mowmaster.mowlib.reading.ReadingRegistry;
import com.mowmaster.mowlib.reading.ReadingSight;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// The reading HUD, for every mod: shown while the player can read (reading/ReadingSight). Two panels, placed by the
// client config (MowLibClientConfig):
// - the paper (by default the middle of the left edge): a looked-at block's ReadingPaper (reading/ReadingRegistry:
//   IReadableBlock on its block entity or block, then registered readers), with the server's lines added when it
//   asks for them (IServerReadable); or else, when not looking at one, a held readable item's (IReadableItem, or any
//   paged storage item). A paper with pages gets a grid of them at the bottom, no wider than a third of the screen.
// - the label (by default the top middle): any other block or a mob: its icon and name, then what the server says it
//   holds (reading/ReadingLabels: energy, fluids, experience; a chiseled bookshelf's book; a mob's effects...)
// The text is worked out once a tick and only drawn each frame. 26.1+ GUI drawing goes through
// GuiGraphicsExtractor (it was GuiGraphics: drawString -> text, renderItem -> item...).
@EventBusSubscriber(modid = MowLibReferences.MODID, value = Dist.CLIENT)
public final class ReadingHud
{
    private ReadingHud() {}

    private static final Identifier LAYER = MowLibReferences.id("reading_hud");
    // GUI sprites: textures/gui/sprites/<name>.png, stretched by their .png.mcmeta ("nine_slice": the corners and edges
    // keep their size, the middle is tiled)
    private static final Identifier PAPER = MowLibReferences.id("reading_paper");
    private static final Identifier LABEL = MowLibReferences.id("reading_label");

    // Dark brown ink (0xAARRGGBB); text colours that would be too pale on paper are darkened (see ink)
    private static final int INK_RGB = 0x3B2A1A;
    private static final int INK = 0xFF000000 | INK_RGB;
    private static final int LINE_HEIGHT = 10;
    private static final int PAPER_PADDING = 10;
    private static final int LABEL_PADDING = 6;
    // Ask the server about the looked-at block or mob every 10 ticks (twice a second)
    private static final int ASK_EVERY = 10;

    // The page grid (slots drawn by SlotGrid): at most 9 across, never wider than this part of the screen
    private static final int SLOT = SlotGrid.SLOT;
    private static final int MAX_COLUMNS = 9;
    private static final float GRID_SCREEN_SHARE = 0.33f;
    private static final int GRID_GAP = 4;
    // Slots drawn as a darker patch of paper, the open page ringed in ink
    private static final int SLOT_FILL = 0x40553A1C;
    private static final int SLOT_EDGE = 0x80553A1C;

    // ---- What's shown (worked out each tick) ----

    private static List<FormattedCharSequence> paperLines = List.of();
    private static int paperWidth;
    private static @Nullable PagedContents paperPages;
    private static boolean labelShown;
    private static ItemStack labelIcon = ItemStack.EMPTY;
    private static Component labelName = Component.empty();
    private static @Nullable BlockPos labelPos;
    private static int labelSlot = -1;
    private static int labelEntity = -1;
    private static int ticks;

    @SubscribeEvent
    public static void registerLayer(RegisterGuiLayersEvent event)
    {
        // Under the chat, so chat messages are never covered
        event.registerBelow(VanillaGuiLayers.CHAT, LAYER, ReadingHud::render);
    }

    // Whether the paper shows this held item (so others, like HeldPageMessages, can stay quiet about it)
    public static boolean showsHeld(Player player, ItemStack stack)
    {
        if (stack.getCount() != 1 || !MowLibClientConfig.PAPER_ENABLED.get() || !MowLibClientConfig.PAPER_HELD.get() || !ReadingSight.canRead(player)) return false;
        if (stack.getItem() instanceof IReadableItem readable) return readable.readWhenHeld(stack);
        return stack.getItem() instanceof IPagedStorageItem;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        ticks++;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Level level = minecraft.level;
        ReadingPaper paper = null;
        boolean newLabel = false;
        BlockPos newPos = null;
        int newSlot = -1;
        int newEntity = -1;
        if (player != null && level != null && ReadingSight.canRead(player))
        {
            // The looked-at block's paper and the label (any other block or a mob) are separate; a held item's paper
            // shows when the crosshair isn't on a readable block (so the label and a held item can show together)
            boolean readableBlock = false;
            // hitResult: what the crosshair is on, worked out each frame (blocks, or mobs within reach)
            HitResult hitResult = minecraft.hitResult;
            if (hitResult instanceof EntityHitResult entityHit && hitResult.getType() == HitResult.Type.ENTITY)
            {
                if (MowLibClientConfig.LABEL_ENABLED.get())
                {
                    Entity entity = entityHit.getEntity();
                    newLabel = true;
                    newEntity = entity.getId();
                    // The mob's spawn egg as its icon, if it has one (26.1+: byId gives an Optional holder of the egg)
                    labelIcon = SpawnEggItem.byId(entity.getType()).map(ItemStack::new).orElse(ItemStack.EMPTY);
                    labelName = entity.getDisplayName();
                    if (newEntity != labelEntity || ticks % ASK_EVERY == 0) ClientPacketDistributor.sendToServer(new ReadingPayloads.EntityRequest(newEntity));
                }
            }
            else if (hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK)
            {
                BlockPos pos = hit.getBlockPos();
                BlockState state = level.getBlockState(pos);
                BlockEntity blockEntity = level.getBlockEntity(pos);
                ReadingPaper read = ReadingRegistry.read(new ReadingContext(level, player, pos, state, blockEntity, hit));
                if (read != null)
                {
                    // A readable block: its paper (if papers are on), never the label
                    readableBlock = true;
                    if (MowLibClientConfig.PAPER_ENABLED.get()) paper = withServerLines(read, pos);
                }
                else if (!state.isAir() && MowLibClientConfig.LABEL_ENABLED.get())
                {
                    newLabel = true;
                    newPos = pos;
                    newSlot = ReadingLabels.bookshelfSlot(state, hit);
                    labelIcon = new ItemStack(state.getBlock());
                    labelName = state.getBlock().getName();
                    // Asked straight away when the block (or bookshelf slot) changes, then twice a second
                    boolean ask = MowLibClientConfig.LABEL_AMOUNTS.get() || newSlot >= 0;
                    if (ask && (!pos.equals(labelPos) || newSlot != labelSlot || ticks % ASK_EVERY == 0))
                    {
                        ClientPacketDistributor.sendToServer(new ReadingPayloads.BlockRequest(pos, newSlot, false));
                    }
                }
            }
            if (!readableBlock) paper = heldPaper(player, level);
        }

        if (paper != null && !paper.lines().isEmpty())
        {
            paperLines = wrap(minecraft.font, paper.lines(), MowLibClientConfig.PAPER_MAX_WIDTH.get());
            paperPages = paper.pages();
        }
        else
        {
            paperLines = List.of();
            paperPages = null;
        }
        paperWidth = width(minecraft.font, paperLines);
        labelShown = newLabel;
        labelPos = newPos;
        labelSlot = newSlot;
        labelEntity = newEntity;
    }

    // A paper that asks the server: its own lines, then the server's last answer about this block (asked twice a second)
    private static ReadingPaper withServerLines(ReadingPaper read, BlockPos pos)
    {
        if (!read.askServer()) return read;
        boolean answered = pos.equals(ReadingPayloads.paperPos);
        if (!answered || ticks % ASK_EVERY == 0) ClientPacketDistributor.sendToServer(new ReadingPayloads.BlockRequest(pos, -1, true));
        if (!answered) return read;
        List<Component> lines = new ArrayList<>(read.lines());
        lines.addAll(ReadingPayloads.paperLines);
        return new ReadingPaper(lines, read.pages(), true);
    }

    // The held item's paper (main hand first), or null
    private static @Nullable ReadingPaper heldPaper(Player player, Level level)
    {
        for (InteractionHand hand : InteractionHand.values())
        {
            ItemStack stack = player.getItemInHand(hand);
            if (showsHeld(player, stack)) return ReadingPapers.item(stack, level, player);
        }
        return null;
    }

    // ---- Text on paper ----

    // Each line in ink, wrapped to the width; FormattedCharSequence is text already laid out for drawing
    private static List<FormattedCharSequence> wrap(Font font, List<Component> lines, int maxWidth)
    {
        List<FormattedCharSequence> wrapped = new ArrayList<>();
        for (Component line : lines)
        {
            // An empty line stays as a gap (split would drop it)
            if (line.getString().isEmpty()) wrapped.add(FormattedCharSequence.EMPTY);
            else wrapped.addAll(font.split(ink(line), maxWidth));
        }
        return wrapped;
    }

    // The line rebuilt piece by piece: uncoloured text in ink, pale colours (white, yellow, light grey...) darkened so
    // they show on paper, dark colours kept. visit calls the lambda for each run of text with its style.
    public static Component ink(Component line)
    {
        MutableComponent inked = Component.empty();
        line.visit((style, text) -> {
            TextColor color = style.getColor();
            int rgb = color == null ? INK_RGB : darken(color.getValue());
            inked.append(Component.literal(text).setStyle(style.withColor(rgb)));
            return Optional.empty(); // empty = keep going
        }, Style.EMPTY);
        return inked;
    }

    private static int darken(int rgb)
    {
        int red = (rgb >> 16) & 0xFF, green = (rgb >> 8) & 0xFF, blue = rgb & 0xFF;
        double brightness = (0.299 * red + 0.587 * green + 0.114 * blue) / 255.0;
        if (brightness < 0.45) return rgb;
        double factor = 0.45 / brightness;
        return ((int) (red * factor) << 16) | ((int) (green * factor) << 8) | (int) (blue * factor);
    }

    private static int width(Font font, List<FormattedCharSequence> lines)
    {
        int width = 0;
        for (FormattedCharSequence line : lines) width = Math.max(width, font.width(line));
        return width;
    }

    // ---- Drawing (every frame) ----

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker)
    {
        Minecraft minecraft = Minecraft.getInstance();
        // F1 hides the HUD
        if (minecraft.gui.hud.isHidden()) return;
        if (!paperLines.isEmpty()) drawPaper(graphics, minecraft.font);
        if (labelShown) drawLabel(graphics, minecraft.font);
    }

    private static void drawPaper(GuiGraphicsExtractor graphics, Font font)
    {
        int screenWidth = graphics.guiWidth(), screenHeight = graphics.guiHeight();
        int room = screenHeight - 2 * PAPER_PADDING - 8;

        // The page grid: as many columns as fit in a third of the screen (at most 9), and the rows that fit under the
        // text, scrolled so the open page's row is in view
        PagedContents pages = paperPages;
        int columns = 0, rows = 0, firstRow = 0;
        if (pages != null && pages.pages() > 0)
        {
            columns = Math.clamp((int) (screenWidth * GRID_SCREEN_SHARE - 2 * PAPER_PADDING) / SLOT, 1, Math.min(MAX_COLUMNS, pages.pages()));
            int totalRows = SlotGrid.rows(pages.pages(), columns);
            int textRoom = Math.min(paperLines.size(), 4) * LINE_HEIGHT;
            rows = Math.clamp((room - textRoom - GRID_GAP) / SLOT, 1, totalRows);
            int openRow = pages.selectedPage() / columns;
            firstRow = Math.clamp(openRow - rows / 2, 0, totalRows - rows);
        }
        int gridHeight = rows > 0 ? GRID_GAP + rows * SLOT : 0;

        // As many lines as fit above the grid
        int fit = Math.max(1, (room - gridHeight) / LINE_HEIGHT);
        List<FormattedCharSequence> lines = paperLines.size() > fit ? paperLines.subList(0, fit) : paperLines;
        int width = Math.max(paperWidth, columns * SLOT) + 2 * PAPER_PADDING;
        int height = lines.size() * LINE_HEIGHT + gridHeight + 2 * PAPER_PADDING - 2;
        HudAnchor anchor = MowLibClientConfig.PAPER_ANCHOR.get();
        int x = HudAnchor.keepOnScreen(anchor.x(screenWidth, width) + MowLibClientConfig.PAPER_X.get(), width, screenWidth);
        int y = HudAnchor.keepOnScreen(anchor.y(screenHeight, height) + MowLibClientConfig.PAPER_Y.get(), height, screenHeight);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PAPER, x, y, width, height);
        for (int index = 0; index < lines.size(); index++)
        {
            // false: no drop shadow (ink doesn't have one)
            graphics.text(font, lines.get(index), x + PAPER_PADDING, y + PAPER_PADDING + index * LINE_HEIGHT, INK, false);
        }
        if (pages != null && rows > 0)
        {
            int gridTop = y + PAPER_PADDING + lines.size() * LINE_HEIGHT + GRID_GAP - 2;
            drawPages(graphics, font, pages, x + PAPER_PADDING, gridTop, columns, firstRow, rows);
        }
    }

    // One slot per page (empty pages too), each page's item with its amount, the open page ringed in ink
    private static void drawPages(GuiGraphicsExtractor graphics, Font font, PagedContents pages, int left, int top,
                                  int columns, int firstRow, int rows)
    {
        int first = firstRow * columns;
        int last = Math.min(pages.pages(), first + rows * columns);
        for (int page = first; page < last; page++)
        {
            int slotX = SlotGrid.slotX(left, page, columns);
            int slotY = SlotGrid.slotY(top, page, columns, firstRow);
            SlotGrid.fill(graphics, slotX, slotY, SLOT_FILL);
            SlotGrid.outline(graphics, slotX, slotY, page == pages.selectedPage() ? INK : SLOT_EDGE);
            ResourceStack<ItemResource> stack = pages.stack(page);
            // One item drawn, with the page's whole amount as its count (a page can hold more than a stack)
            if (!stack.isEmpty()) SlotGrid.item(graphics, font, stack.resource().toStack(1), slotX, slotY, stack.amount());
        }
    }

    private static void drawLabel(GuiGraphicsExtractor graphics, Font font)
    {
        // The server's answer, if it's about this block or mob
        List<Component> answer = List.of();
        if (labelEntity >= 0 && labelEntity == ReadingPayloads.entityId) answer = ReadingPayloads.entityLines;
        else if (labelPos != null && labelPos.equals(ReadingPayloads.labelPos) && labelSlot == ReadingPayloads.labelSlot) answer = ReadingPayloads.labelLines;
        List<FormattedCharSequence> extra = new ArrayList<>();
        for (Component line : answer) extra.add(ink(line).getVisualOrderText());

        FormattedCharSequence name = ink(labelName).getVisualOrderText();
        boolean icon = !labelIcon.isEmpty();
        int textLeft = icon ? 16 + 4 : 0;
        int width = 2 * LABEL_PADDING + Math.max(textLeft + font.width(name), width(font, extra));
        int height = 2 * LABEL_PADDING + 16 + extra.size() * LINE_HEIGHT;
        HudAnchor anchor = MowLibClientConfig.LABEL_ANCHOR.get();
        int x = HudAnchor.keepOnScreen(anchor.x(graphics.guiWidth(), width) + MowLibClientConfig.LABEL_X.get(), width, graphics.guiWidth());
        int y = HudAnchor.keepOnScreen(anchor.y(graphics.guiHeight(), height) + MowLibClientConfig.LABEL_Y.get(), height, graphics.guiHeight());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LABEL, x, y, width, height);
        if (icon) graphics.item(labelIcon, x + LABEL_PADDING, y + LABEL_PADDING);
        // The name level with the middle of the icon
        graphics.text(font, name, x + LABEL_PADDING + textLeft, y + LABEL_PADDING + 4, INK, false);
        for (int index = 0; index < extra.size(); index++)
        {
            graphics.text(font, extra.get(index), x + LABEL_PADDING, y + LABEL_PADDING + 18 + index * LINE_HEIGHT, INK, false);
        }
    }
}
