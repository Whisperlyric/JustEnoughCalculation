package me.towdium.jecalculation.data.label.labels;

import me.towdium.jecalculation.data.label.ILabel;
import me.towdium.jecalculation.gui.JecaGui;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.List;

public class LEmpty implements ILabel {
    public static final String IDENTIFIER = "empty";

    @Override
    public boolean matches(Object l) {
        return l == this;
    }

    @Override
    public void drawLabel(JecaGui gui, int xPos, int yPos, boolean center, boolean hand) {
    }

    @Nullable
    @Override
    public Object getRepresentation() {
        return null;
    }

    @Override
    public ILabel increaseAmount() {
        return this;
    }

    @Override
    public ILabel decreaseAmount() {
        return this;
    }

    @Override
    public ILabel multiply(float i) {
        return this;
    }

    @Override
    public boolean acceptPercent() {
        return false;
    }

    @Override
    public ILabel setPercent(boolean p) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPercent() {
        return false;
    }

    @Override
    public long getAmount() {
        return 0;
    }

    @Override
    public String getAmountString(boolean round) {
        return "0";
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public void getToolTip(List<String> existing, boolean detailed) {
    }

    @Override
    public ILabel copy() {
        return this;
    }

    @Override
    public CompoundTag toNbt() {
        return new CompoundTag();
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public ILabel setAmount(long amount) {
        return this;
    }
}
