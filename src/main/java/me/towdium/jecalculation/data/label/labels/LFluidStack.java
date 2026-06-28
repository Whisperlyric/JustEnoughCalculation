package me.towdium.jecalculation.data.label.labels;

import me.towdium.jecalculation.data.label.ILabel;
import me.towdium.jecalculation.data.label.ILabel.Serializer.SerializationException;
import me.towdium.jecalculation.gui.JecaGui;
import me.towdium.jecalculation.gui.Resource;
import me.towdium.jecalculation.utils.Utilities;
import me.towdium.jecalculation.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LFluidStack extends LStack<Fluid> {
    public static final String IDENTIFIER = "fluidStack";
    public static final String KEY_FLUID = "fluid";
    public static final String KEY_NBT = "nbt";

    Fluid fluid;
    CompoundTag nbt;

    @Override
    public Fluid getRepresentation() {
        return fluid;
    }

    @Override
    public boolean acceptPercent() {
        return false;
    }

    public LFluidStack(long amount, Fluid fluid) {
        this(amount, fluid, null);
    }

    public LFluidStack(long amount, Fluid fluid, @Nullable CompoundTag nbt) {
        super(amount, false);
        this.fluid = fluid;
        this.nbt = nbt;
    }

    public LFluidStack(CompoundTag nbt) {
        super(nbt);
        String id = nbt.getStringOr(KEY_FLUID, "");
        Optional<Fluid> f = BuiltInRegistries.FLUID.getOptional(Identifier.tryParse(id));
        if (f.isEmpty()) throw new SerializationException("Fluid " + id + " cannot be resolved, ignoring");
        this.fluid = f.get();
        this.nbt = nbt.contains(KEY_NBT) ? nbt.getCompoundOrEmpty(KEY_NBT) : null;
    }

    @Override
    public Fluid get() {
        return fluid;
    }

    @Override
    public Context<Fluid> getContext() {
        return Context.FLUID;
    }

    @Override
    protected void drawLabel(int xPos, int yPos, JecaGui gui, boolean hand) {
        gui.drawResource(Resource.LBL_FLUID, xPos, yPos);
        gui.drawFluid(fluid, xPos + 2, yPos + 2, 12, 12);
        gui.drawResource(Resource.LBL_FRAME, xPos, yPos);
    }

    @Override
    protected int getMultiplier() {
        return 100;
    }

    @Override
    public String getAmountString(boolean round) {
        return format(amount);
    }

    @Override
    public void getToolTip(List<String> existing, boolean detailed) {
        super.getToolTip(existing, detailed);
        existing.add(FORMAT_BLUE + FORMAT_ITALIC + Utilities.getModName(fluid));
    }

    @Override
    public LFluidStack copy() {
        return new LFluidStack(amount, fluid, nbt);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String getDisplayName() {
        return fluid.defaultFluidState().createLegacyBlock().getBlock().getName().getString();
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag ret = super.toNbt();
        ret.putString(KEY_FLUID, BuiltInRegistries.FLUID.getKey(fluid).toString());
        if (nbt != null) ret.put(KEY_NBT, nbt);
        return ret;
    }

    public static List<ILabel> suggest(List<ILabel> is, @Nullable Class<?> context) {
        return Collections.emptyList();
    }

    public static boolean merge(ILabel a, ILabel b) {
        if (a instanceof LFluidStack fa && b instanceof LFluidStack fb) {
            return fa.fluid == fb.fluid;
        }
        return false;
    }

    public static String format(long amount) {
        if (amount >= 1000) return amount / 1000 + "B";
        return amount + "mB";
    }

    @Override
    public boolean matches(Object l) {
        if (l instanceof LFluidStack lfs) {
            return fluid == lfs.fluid;
        }
        return false;
    }
}
