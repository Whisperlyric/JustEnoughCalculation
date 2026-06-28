package me.towdium.jecalculation.events;

import me.towdium.jecalculation.gui.JecaGui;
import me.towdium.jecalculation.utils.wrappers.Trio;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import me.towdium.jecalculation.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GuiScreenEventHandler {

    protected GuiScreenOverlayHandler overlayHandler = null;
    protected JecaGui gui = null;
    protected InventorySummary cachedInventory;
    protected Trio<List<? extends ClientTooltipComponent>, Integer, Integer> cachedTooltipEvent;
    private boolean mousePressed = false;
    private int mouseButton = 0;
    private double mouseX = 0, mouseY = 0;

    public GuiScreenEventHandler() {
        registerEvents();
    }

    private void registerEvents() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen && client.player != null) {
                overlayHandler = new GuiScreenOverlayHandler(client.player.getInventory());
                gui = new JecaGui(null, false, overlayHandler, true);
                gui.init(screen.width, screen.height);
                overlayHandler.setGui(gui);
            }
        });
    }

    protected boolean isScreenValidForOverlay(Screen screen) {
        return screen instanceof AbstractContainerScreen
                && !(screen instanceof JecaGui);
    }

    private boolean didInventoryChange(Inventory inventory) {
        if (cachedInventory == null) {
            cachedInventory = new InventorySummary(inventory);
            return false;
        }

        InventorySummary newSummery = new InventorySummary(inventory);
        if (newSummery.equals(cachedInventory)) {
            return false;
        }

        cachedInventory = newSummery;
        return true;
    }

    public List<Rect2i> getGuiAreas() {
        if (overlayHandler != null && gui != null && isScreenValidForOverlay(Minecraft.getInstance().screen)) {
            return overlayHandler.getGuiExtraAreas(gui.getGuiLeft(), gui.getGuiTop());
        }
        return Collections.emptyList();
    }
}
