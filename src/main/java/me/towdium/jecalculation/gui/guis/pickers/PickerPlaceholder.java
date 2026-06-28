package me.towdium.jecalculation.gui.guis.pickers;

import me.towdium.jecalculation.data.label.labels.LPlaceholder;
import me.towdium.jecalculation.gui.guis.IGui;
import me.towdium.jecalculation.gui.widgets.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import me.towdium.jecalculation.annotation.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

import static me.towdium.jecalculation.gui.JecaGui.COLOR_TEXT_RED;
import static me.towdium.jecalculation.gui.JecaGui.COLOR_TEXT_WHITE;
import static me.towdium.jecalculation.gui.Resource.BTN_YES;
import static me.towdium.jecalculation.gui.Resource.ICN_TEXT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Environment(EnvType.CLIENT)
public class PickerPlaceholder extends IPicker.Impl implements IGui {
    public PickerPlaceholder() {
        WLabelScroll scroll = new WLabelScroll(7, 69, 8, 5, false)
                .setLabels(LPlaceholder.getRecent()).setLsnrClick((i, v) -> notifyLsnr(i.get(v).getLabel()));
        WTextField create = new WTextField(26, 7, 69)
                .setListener(i -> i.setColor(i.getText().isEmpty() ? COLOR_TEXT_RED : COLOR_TEXT_WHITE));
        add(new WIcon(7, 45, 20, 20, ICN_TEXT, "common.search"));
        add(new WIcon(7, 7, 20, 20, ICN_TEXT, "placeholder.create"));
        add(new WSearch(26, 45, 90, scroll));
        add(new WLine(36));
        add(new WButtonIcon(95, 7, 20, 20, BTN_YES, "common.confirm").setListener(i -> {
            if (!create.getText().isEmpty()) callback.accept(new LPlaceholder(create.getText(), 1));
        }));
        add(scroll, create);
    }
}
