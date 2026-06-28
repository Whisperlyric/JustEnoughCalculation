package me.towdium.jecalculation.gui.widgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import me.towdium.jecalculation.annotation.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

import static me.towdium.jecalculation.gui.JecaGui.COLOR_TEXT_RED;
import static me.towdium.jecalculation.gui.JecaGui.COLOR_TEXT_WHITE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Environment(EnvType.CLIENT)
public class WSearch extends WTextField {
    ISearchable[] lss;

    public WSearch(int xPos, int yPos, int xSize, ISearchable... lss) {
        super(xPos, yPos, xSize);
        this.lss = lss;
    }

    @Override
    protected void notifyLsnr() {
        super.notifyLsnr();
        refresh();
    }

    public void refresh() {
        boolean b = false;
        for (ISearchable i : lss) if (i.setFilter(textField.getValue())) b = true;
        setColor(b ? COLOR_TEXT_WHITE : COLOR_TEXT_RED);
    }
}
