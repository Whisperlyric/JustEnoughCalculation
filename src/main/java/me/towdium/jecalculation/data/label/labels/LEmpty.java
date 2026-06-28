package me.towdium.jecalculation.data.label.labels;

import me.towdium.jecalculation.data.label.ILabel;
import me.towdium.jecalculation.gui.JecaGui;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class LEmpty implements ILabel {
    public static final String IDENTIFIER = "empty";

    @Override
    public boolean matches(@NotNull Object l) {
        return l == this;
    }

    @Override
    public void drawLabel(@NotNull JecaGui gui, int xPos, int yPos, boolean center, boolean hand) {
    }

    @Nullable
    @Override
    public Object getRepresentation() {
        return null;
    }

    @Override
    public @NotNull ILabel increaseAmount() {
        return this;
    }

    @Override
    public @NotNull ILabel decreaseAmount() {
        return this;
    }

    @Override
    public @NotNull ILabel multiply(float i) {
        return this;
    }

    @Override
    public boolean acceptPercent() {
        return false;
    }

    @Override
    public @NotNull ILabel setPercent(boolean p) {
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
    public @NotNull String getAmountString(boolean round) {
        return "0";
    }

    @Override
    public @NotNull String getDisplayName() {
        return "";
    }

    @Override
    public void getToolTip(List<@NotNull String> existing, boolean detailed) {
    }

    @Override
    public @NotNull ILabel copy() {
        return this;
    }

    @Override
    public @NotNull CompoundTag toNbt() {
        return new CompoundTag();
    }

    @Override
    public @NotNull String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public @NotNull ILabel setAmount(long amount) {
        return this;
    }
}
