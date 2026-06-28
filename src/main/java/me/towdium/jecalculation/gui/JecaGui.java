package me.towdium.jecalculation.gui;

import me.towdium.jecalculation.JustEnoughCalculation;
import me.towdium.jecalculation.data.Controller;
import me.towdium.jecalculation.data.label.ILabel;
import me.towdium.jecalculation.events.GuiScreenOverlayHandler;
import me.towdium.jecalculation.gui.guis.GuiCraft;
import me.towdium.jecalculation.gui.guis.GuiMath;
import me.towdium.jecalculation.gui.guis.IGui;
import me.towdium.jecalculation.utils.GuiUtils;
import me.towdium.jecalculation.utils.Utilities;
import me.towdium.jecalculation.utils.wrappers.Wrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import me.towdium.jecalculation.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.util.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.towdium.jecalculation.utils.Utilities.getPlayer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Environment(EnvType.CLIENT)
public class JecaGui extends AbstractContainerScreen<JecaGui.@NotNull JecaContainer> {
    public static final KeyMapping keyOpenGuiCraft = new KeyMapping(
            "jecalculation.key.gui_craft", GLFW.GLFW_KEY_UNKNOWN,
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(JustEnoughCalculation.MODID, "key.category")));
    public static final KeyMapping keyOpenGuiMath = new KeyMapping(
            "jecalculation.key.gui_math", GLFW.GLFW_KEY_UNKNOWN,
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(JustEnoughCalculation.MODID, "key.category")));
    public static final int COLOR_GUI_GREY = 0xFFA1A1A1;
    public static final int COLOR_TEXT_RED = 0xFF0000;
    public static final int COLOR_TEXT_GREY = 0x404040;
    public static final int COLOR_TEXT_WHITE = 0xFFFFFF;
    public static final boolean ALWAYS_TOOLTIP = false;
    @SuppressWarnings("StringOperationCanBeSimplified")
    public static final String SEPARATOR = new String();
    public static final boolean IS_OSX = Util.OS.OSX.equals(Util.getPlatform());
    public ILabel hand = ILabel.EMPTY;
    protected static JecaGui last;
    public static JecaGui override;
    protected JecaGui parent;
    protected GuiGraphicsExtractor graphics;
    protected final Utilities.OffsetStack itemOffset = new Utilities.OffsetStack();
    protected final boolean isWidget;
    protected boolean preventRecipeScreen = false;

    public IGui root;

    public JecaGui(@Nullable JecaGui parent, IGui root, boolean isWidget) {
        this(parent, false, root, isWidget);
    }

    public JecaGui(@Nullable JecaGui parent, boolean acceptsTransfer, IGui root, boolean isWidget) {
        super(acceptsTransfer ? new JecaGui.ContainerTransfer() : new JecaGui.ContainerNonTransfer(),
                getPlayer().getInventory(), Component.literal(""));
        this.parent = parent;
        this.root = root;
        this.isWidget = isWidget;
        if (menu != null) menu.setGui(this);
    }

    public static void registerEvents() {
        // Events are now registered in JustEnoughCalculation main class via Fabric API
        // This method is kept for compatibility but does nothing
    }

    @Override
    public void removed() {
        super.removed();
        Objects.requireNonNull(this.minecraft);
    }

    public static int getMouseX() {
        JecaGui gui = getCurrent();
        Minecraft mc = Objects.requireNonNull(gui.minecraft, "Internal error");
        int windowWidth = mc.getWindow().getScreenWidth();
        if (windowWidth == 0) return 0;
        return (int) mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / windowWidth - gui.leftPos;
    }

    public static int getMouseY() {
        JecaGui gui = getCurrent();
        Minecraft mc = Objects.requireNonNull(gui.minecraft, "Internal error");
        int windowHeight = mc.getWindow().getScreenHeight();
        if (windowHeight == 0) return 0;
        return (int) mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / windowHeight - gui.topPos;
    }

    public int getGlobalMouseX() {
        Minecraft mc = Objects.requireNonNull(Minecraft.getInstance(), "Internal error");
        int width = mc.getWindow().getWidth();
        if (width == 0) return 0;
        if (IS_OSX) width /= 2;
        return (int) mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / width - this.leftPos;
    }

    public int getGlobalMouseY() {
        Minecraft mc = Objects.requireNonNull(Minecraft.getInstance(), "Internal error");
        int height = mc.getWindow().getHeight();
        if (height == 0) return 0;
        if (IS_OSX) height /= 2;
        return (int) mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / height - this.topPos;
    }

    public int getGuiLeft() {
        return leftPos;
    }

    public int getGuiTop() {
        return topPos;
    }

    public int getYSize() {
        return imageHeight;
    }

    @Override
    public void containerTick() {
        root.onTick(this);
    }

    @Nullable
    public Slot getSlotUnderMouse() {
        Container i = new SimpleContainer(ItemStack.EMPTY);
        Slot s = new Slot(i, 0, 0, 0);
        ILabel l = getLabelUnderMouse();
        Object rep = l == null ? null : l.getRepresentation();
        if (rep instanceof ItemStack) s.set((ItemStack) rep);
        return s;
    }

    public static boolean mouseIn(int xPos, int yPos, int xSize, int ySize, int xMouse, int yMouse) {
        return xMouse > xPos && yMouse > yPos && xMouse <= xPos + xSize && yMouse <= yPos + ySize;
    }

    public static void displayGui(IGui root) {
        Screen s = Minecraft.getInstance().screen;
        if (s instanceof JecaGui) {
            displayGui(root, true);
        } else {
            displayGui(root, null);
        }
    }

    public static void displayGui(IGui root, @Nullable JecaGui parent) {
        Minecraft mc = Minecraft.getInstance();
        Screen s = mc.screen;
        JecaGui gui = new JecaGui(parent, root.acceptsTransfer(), root, false);
        if (s != null && (Utilities.isRecipeScreen(s) || s instanceof ChatScreen)) {
            JecaGui.override = gui;
        }
    }

    public static void displayGui(IGui root, boolean updateParent) {
        JecaGui current = JecaGui.getCurrent();
        JecaGui parent = updateParent ? current : current.parent;
        JecaGui.displayGui(root, parent);
    }

    public GuiGraphicsExtractor getGraphics() {
        return graphics;
    }

    public void setGraphics(GuiGraphicsExtractor graphics) {
        this.graphics = graphics;
    }

    public Utilities.OffsetStack getItemOffsetStack() {
        return itemOffset;
    }

    public static JecaGui getCurrent() {
        Screen gui = Minecraft.getInstance().screen;
        JecaGui ret = gui instanceof JecaGui ? (JecaGui) gui : null;
        Objects.requireNonNull(ret);
        return ret;
    }

    public static JecaGui getLast() {
        return last;
    }

    public static void displayParent() {
        JecaGui gui = getCurrent().parent;
        gui.root.onVisible(gui);
        last = gui;
        Minecraft.getInstance().setScreen(gui);
    }

    @Nullable
    public ILabel getLabelUnderMouse() {
        Wrapper<ILabel> l = new Wrapper<>(null);
        root.getLabelUnderMouse(getGlobalMouseX(), getGlobalMouseY(), l);
        return l.value;
    }

    public static boolean handleScreenOpen(@Nullable Screen screen) {
        if (override != null) {
            override.root.onVisible(override);
            last = override;
            Screen s = override;
            override = null;
            Minecraft.getInstance().setScreen(s);
            return true;
        }
        if (Minecraft.getInstance().screen instanceof JecaGui gui && gui.preventRecipeScreen && Utilities.isRecipeScreen(screen)) {
            gui.preventRecipeScreen = false;
            return true;
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    public static int openGuiMath(@Nullable ItemStack is, int slot) {
        boolean ret = is == null && Controller.isServerActive();
        String s = "jecalculation.chat.server_mode";
        if (ret) getPlayer().sendSystemMessage(Component.translatable(s));
        else JecaGui.displayGui(new GuiMath(is, slot));
        return ret ? 1 : 0;
    }

    @Environment(EnvType.CLIENT)
    public static int openGuiCraft(@Nullable ItemStack is, int slot) {
        boolean ret = is == null && Controller.isServerActive();
        String s = "jecalculation.chat.server_mode";
        if (ret) getPlayer().sendSystemMessage(Component.translatable(s));
        else JecaGui.displayGui(new GuiCraft(is, slot));
        return ret ? 1 : 0;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor matrixGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(matrixGraphics, mouseX, mouseY, partialTicks);
        graphics = matrixGraphics;
        mouseX -= leftPos;
        mouseY -= topPos;
        Matrix3x2fStack pose = matrixGraphics.pose();
        pose.pushMatrix();
        pose.translate(leftPos, topPos);
        root.onDraw(this, mouseX, mouseY);
        pose.popMatrix();
        pose.pushMatrix();
        hand.drawLabel(this, mouseX + leftPos, mouseY + topPos, true, true);
        pose.popMatrix();
        List<String> tooltip = new ArrayList<>();
        root.onTooltip(this, mouseX, mouseY, tooltip);
        drawHoveringText(matrixGraphics, tooltip, mouseX + leftPos, mouseY + topPos, font);
    }

    public void drawHoveringText(GuiGraphicsExtractor matrixGraphics, List<String> textLines, int x, int y, net.minecraft.client.gui.Font font) {
        if (!textLines.isEmpty()) {
            var pose = matrixGraphics.pose();
            pose.pushMatrix();
            int i = 0;
            int separators = 0;
            for (String s : textLines) {
                int j = this.font.width(s);
                if (j > i) i = j;
                //noinspection StringEquality
                if (s == JecaGui.SEPARATOR) separators++;
            }
            //noinspection StringEquality
            if (textLines.getLast() == SEPARATOR) separators--;
            int l1 = x + 12;
            int i2 = y - 12;
            int k = 8 + (textLines.size() - separators - 1) * 10 + 2 * separators;
            if (l1 + i > this.width) l1 -= 28 + i;
            if (i2 + k + 6 > this.height) i2 = this.height - k - 6;
            matrixGraphics.fill(RenderPipelines.GUI, l1 - 3, i2 - 4, l1 + i + 3, i2 - 3, -267386864);
            matrixGraphics.fill(RenderPipelines.GUI, l1 - 3, i2 + k + 3, l1 + i + 3, i2 + k + 4, -267386864);
            matrixGraphics.fill(RenderPipelines.GUI, l1 - 3, i2 - 3, l1 + i + 3, i2 + k + 3, -267386864);
            matrixGraphics.fill(RenderPipelines.GUI, l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, -267386864);
            matrixGraphics.fill(RenderPipelines.GUI, l1 + i + 3, i2 - 3, l1 + i + 4, i2 + k + 3, -267386864);
            matrixGraphics.fill(RenderPipelines.GUI, l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, 1347420415);
            matrixGraphics.fill(RenderPipelines.GUI, l1 + i + 2, i2 - 3 + 1, l1 + i + 3, i2 + k + 3 - 1, 1347420415);
            matrixGraphics.fill(RenderPipelines.GUI, l1 - 3, i2 - 3, l1 + i + 3, i2 - 3 + 1, 1347420415);
            matrixGraphics.fill(RenderPipelines.GUI, l1 - 3, i2 + k + 2, l1 + i + 3, i2 + k + 3, 1344798847);
            for (String s1 : textLines) {
                //noinspection StringEquality
                if (s1 == SEPARATOR) i2 += 2;
                else {
                    matrixGraphics.text(font, s1, l1, i2, -1, true);
                    i2 += 10;
                }
            }
            pose.popMatrix();
        }
    }

    public void drawResource(Resource r, int xPos, int yPos) {
        drawResource(r, xPos, yPos, 0xFFFFFF);
    }

    public void drawResource(Resource r, int xPos, int yPos, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, r.getResourceLocation(), xPos, yPos,
                (float) r.getXPos(), (float) r.getYPos(), r.getXSize(), r.getYSize(), 256, 256);
    }

    public void drawResourceContinuous(Resource r, int xPos, int yPos, int xSize, int ySize, int border) {
        drawResourceContinuous(r, xPos, yPos, xSize, ySize, border, border, border, border);
    }

    public void drawResourceContinuous(
            Resource r, int xPos, int yPos, int xSize, int ySize,
            int borderTop, int borderBottom, int borderLeft, int borderRight) {
        GuiUtils.drawContinuousTexturedBox(graphics, r.getResourceLocation(), xPos, yPos, r.getXPos(), r.getYPos(),
                xSize, ySize, r.getXSize(), r.getYSize(), borderTop, borderBottom, borderLeft, borderRight, 0);
    }

    private void setColor(int color) {
    }

    public void drawFluid(Fluid f, int xPos, int yPos, int xSize, int ySize) {
        // MC 26.1.2: simplified fluid rendering - draw a colored placeholder
        Identifier fluidId = BuiltInRegistries.FLUID.getKey(f);
        Identifier spriteLoc = Identifier.fromNamespaceAndPath(fluidId.getNamespace(), "block/" + fluidId.getPath() + "_still");
        try {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteLoc, xPos, yPos, xSize, ySize);
        } catch (Exception ignored) {
            // Fallback: draw a blue rectangle if sprite unavailable
            graphics.fill(RenderPipelines.GUI, xPos, yPos, xPos + xSize, yPos + ySize, 0xFF4080FF);
        }
    }

    public void drawRectangle(int xPos, int yPos, int xSize, int ySize, int color) {
        graphics.fill(RenderPipelines.GUI, xPos, yPos, xPos + xSize, yPos + ySize, color);
    }

    public int getStringWidth(String s) {
        return font.width(s);
    }

    public void drawSplitText(float xPos, float yPos, int width, FontType f, String s) {
        drawSplitText(xPos, yPos, f, Utilities.I18n.wrap(s, width));
    }

    public void drawSplitText(float xPos, float yPos, FontType f, List<String> ss) {
        drawText(xPos, yPos, f, () -> {
            int y = 0;
            for (String i : ss) {
                if (f.shadow) graphics.text(font, i, 0, y, f.color);
                else graphics.text(font, i, 0, y, f.color, false);
                y += font.lineHeight + 1;
            }
        });
    }

    public void drawText(float xPos, float yPos, FontType f, String s) {
        drawText(xPos, yPos, Integer.MAX_VALUE, f, s);
    }

    public void drawText(float xPos, float yPos, int width, FontType f, String s) {
        drawText(xPos, yPos, f, () -> {
            String str = s;
            int strWidth = f.getTextWidth(str);
            int ellipsisWidth = f.getTextWidth("...");
            if (strWidth > width && strWidth > ellipsisWidth)
                str = f.trimToWidth(str, width - ellipsisWidth).trim() + "...";
            if (f.shadow) graphics.text(font, str, 0, 0, f.color);
            else graphics.text(font, str, 0, 0, f.color, false);
        });
    }

    private void drawText(float xPos, float yPos, FontType f, Runnable r) {
        getGraphics().pose().pushMatrix();
        getGraphics().pose().translate(xPos, yPos);
        if (f.half) getGraphics().pose().scale(0.5f, 0.5f);
        r.run();
        getGraphics().pose().popMatrix();
    }

    public void drawItemStack(int xPos, int yPos, ItemStack is, boolean centred, boolean hand) {
        if (centred) {
            xPos -= 8;
            yPos -= 8;
        }

        int x = hand ? xPos : leftPos + xPos;
        int y = hand ? yPos : topPos + yPos;

        if (root instanceof GuiScreenOverlayHandler) {
            graphics.item(is, xPos, yPos);
        } else {
            graphics.item(is, xPos + itemOffset.x(), yPos + itemOffset.y());
        }
        graphics.itemDecorations(font, is, leftPos + xPos, topPos + yPos);
    }

    @Override
    protected void init() {
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        char ch = (char) event.codepoint();
        return root.onChar(this, ch, 0) || super.charTyped(event);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        int key = event.key();
        if (key == GLFW.GLFW_KEY_ESCAPE && hand != ILabel.EMPTY) hand = ILabel.EMPTY;
        else if (!root.onKeyPressed(this, key, event.modifiers())) {
            if (key == GLFW.GLFW_KEY_ESCAPE && parent != null) displayParent();
            else return super.keyPressed(event);
        }
        return true;
    }

    @Override
    public boolean keyReleased(net.minecraft.client.input.KeyEvent event) {
        return root.onKeyReleased(this, event.key(), event.modifiers())
                || super.keyReleased(event);
    }

    @Environment(EnvType.CLIENT)
    public static class FontType {
        public static final FontType SHADOW = new FontType(JecaGui.COLOR_TEXT_WHITE, true, false, false);
        public static final FontType PLAIN = new FontType(JecaGui.COLOR_TEXT_GREY, false, false, false);
        public static final FontType RAW = new FontType(JecaGui.COLOR_TEXT_GREY, false, false, true);
        public static final FontType HALF = new FontType(JecaGui.COLOR_TEXT_WHITE, true, true, true);

        public int color;
        public boolean shadow, half, raw;
        private final Font font = Minecraft.getInstance().font;

        public FontType(int color, boolean shadow, boolean half, boolean raw) {
            this.color = color;
            this.shadow = shadow;
            this.half = half;
            this.raw = raw;
        }

        public int getTextWidth(String s) {
            return (int) Math.ceil(font.width(s) * (half ? 0.5f : 1));
        }

        public int getTextHeight() {
            return (int) Math.ceil(font.lineHeight * (half ? 0.5f : 1));
        }

        public String trimToWidth(String s, int i) {
            return font.plainSubstrByWidth(s, i * (half ? 2 : 1));
        }
    }

    @Environment(EnvType.CLIENT)
    public static class JecaContainer extends AbstractContainerMenu {
        JecaGui gui;

        protected JecaContainer() {
            super(null, 0);
        }

        public JecaGui getGui() {
            return gui;
        }

        public void setGui(JecaGui gui) {
            this.gui = gui;
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return null;
        }

        @Override
        public boolean stillValid(Player playerIn) {
            return true;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ContainerTransfer extends JecaContainer {
    }

    @Environment(EnvType.CLIENT)
    public static class ContainerNonTransfer extends JecaContainer {
    }
}
