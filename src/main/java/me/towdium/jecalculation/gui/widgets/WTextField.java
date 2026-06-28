package me.towdium.jecalculation.gui.widgets;

import me.towdium.jecalculation.gui.JecaGui;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import me.towdium.jecalculation.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Environment(EnvType.CLIENT)
public class WTextField implements IWidget {
    public ListenerAction<? super WTextField> listener;
    protected int xPos, yPos, xSize;
    EditBox textField;
    public static final int HEIGHT = 20;

    public WTextField(int xPos, int yPos, int xSize) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.xSize = xSize;
        textField = new EditBox(Minecraft.getInstance().font, xPos + 1, yPos + 1, xSize - 2, 18, Component.literal("WIP"));
    }

    @Override
    public void onMouseFocused(JecaGui gui, int xMouse, int yMouse, int button) {
        // MC 26.1.2: mouseClicked signature changed; focus handled in onMouseClicked
    }

    @Override
    public void onTick(JecaGui gui) {
        // MC 26.1.2: EditBox.tick() removed; no-op
    }

    @Override
    public boolean onMouseClicked(JecaGui gui, int xMouse, int yMouse, int button) {
        boolean hovered = xMouse >= xPos && xMouse < xPos + xSize && yMouse >= yPos && yMouse < yPos + HEIGHT;
        textField.setFocused(hovered);
        if (hovered && button == 1) {
            textField.setValue("");
            notifyLsnr();
            return true;
        } else return false;
    }

    @Override
    public boolean onDraw(JecaGui gui, int xMouse, int yMouse) {
        textField.extractWidgetRenderState(gui.getGraphics(), xPos, yPos, 0);
        return false;
    }

    @Override
    public boolean onKeyPressed(JecaGui gui, int key, int modifier) {
        KeyEvent event = new KeyEvent(key, GLFW.glfwGetKeyScancode(key), modifier);
        boolean ret = textField.keyPressed(event);
        if (ret) notifyLsnr();
        return textField.isFocused() && textField.isVisible() && key != 256;
    }

    @Override
    public boolean onKeyReleased(JecaGui gui, int key, int modifier) {
        // MC 26.1.2: EditBox.keyReleased removed; no-op
        return false;
    }

    @Override
    public boolean onChar(JecaGui gui, char ch, int modifier) {
        CharacterEvent event = new CharacterEvent(ch);
        boolean ret = textField.charTyped(event);
        if (ret) notifyLsnr();
        return ret;
    }

    public String getText() {
        return textField.getValue();
    }

    public WTextField setText(String s) {
        textField.setValue(s);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public WTextField setListener(ListenerAction<? super WTextField> listener) {
        this.listener = listener;
        return this;
    }

    public WTextField setColor(int color) {
        textField.setTextColor(color);
        return this;
    }

    protected void notifyLsnr() {
        if (listener != null) listener.invoke(this);
    }
}
