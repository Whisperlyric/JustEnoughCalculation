package me.towdium.jecalculation.data.label.labels;

import me.towdium.jecalculation.data.label.ILabel;
import me.towdium.jecalculation.data.label.ILabel.Serializer.SerializationException;
import me.towdium.jecalculation.gui.JecaGui;
import me.towdium.jecalculation.gui.Resource;
import me.towdium.jecalculation.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LItemStack extends LStack<Item> {
    public static final String IDENTIFIER = "itemStack";

    public static final String KEY_ITEM = "item";
    public static final String KEY_NBT = "nbt";
    public static final String KEY_F_META = "fMeta";
    public static final String KEY_F_NBT = "fNbt";

    Item item;
    CompoundTag nbt;
    boolean fMeta;
    boolean fNbt;
    transient ItemStack rep;

    public LItemStack(ItemStack is) {
        super(is.getCount(), false);
        net.minecraft.world.item.component.CustomData customData = is.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData == null ? null : customData.copyTag();
        init(is.getItem(), tag, false, false);
    }

    public LItemStack(CompoundTag tag) {
        super(tag);
        String id = tag.getStringOr(KEY_ITEM, "");
        Optional<Item> i = BuiltInRegistries.ITEM.getOptional(Identifier.tryParse(id));
        if (i.isEmpty()) throw new SerializationException("Item " + id + " cannot be resolved, ignoring");
        init(i.get(),
                tag.contains(KEY_NBT) ? tag.getCompoundOrEmpty(KEY_NBT) : null,
                tag.getBooleanOr(KEY_F_META, false),
                tag.getBooleanOr(KEY_F_NBT, false)
        );
    }

    @Override
    public Item get() {
        return item;
    }

    @Override
    public Context<Item> getContext() {
        return Context.ITEM;
    }

    private LItemStack(LItemStack lis) {
        super(lis);
        item = lis.item;
        nbt = lis.nbt == null ? null : lis.nbt.copy();
        fMeta = lis.fMeta;
        fNbt = lis.fNbt;
        rep = lis.rep == null ? null : lis.rep.copy();
    }

    private void init(Item item, @Nullable CompoundTag nbt, boolean fMeta, boolean fNbt) {
        this.item = item;
        this.nbt = nbt;
        this.fMeta = fMeta;
        this.fNbt = fNbt;
        rep = new ItemStack(item, (int) Math.min(amount, Integer.MAX_VALUE));
        if (nbt != null) {
            net.minecraft.world.item.component.CustomData.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, rep, nbt);
        }
    }

    @Override
    public Object getRepresentation() {
        return rep;
    }

    @Override
    public boolean acceptPercent() {
        return false;
    }

    public LItemStack setFMeta(boolean fMeta) {
        this.fMeta = fMeta;
        return this;
    }

    public LItemStack setFNbt(boolean fNbt) {
        this.fNbt = fNbt;
        return this;
    }

    public LItemStack setFCap(boolean fCap) {
        // MC 26.1.2: capabilities removed; kept for API compatibility
        return this;
    }

    @Override
    protected void drawLabel(int xPos, int yPos, JecaGui gui, boolean hand) {
        gui.drawItemStack(xPos, yPos, rep, false, hand);
        gui.drawResource(Resource.LBL_FRAME, xPos, yPos);
    }

    @Override
    public String getAmountString(boolean round) {
        return String.valueOf(amount);
    }

    @Override
    public LItemStack copy() {
        return new LItemStack(this);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String getDisplayName() {
        return rep.getHoverName().getString();
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag ret = super.toNbt();
        ret.putString(KEY_ITEM, BuiltInRegistries.ITEM.getKey(item).toString());
        if (nbt != null) ret.put(KEY_NBT, nbt);
        ret.putBoolean(KEY_F_META, fMeta);
        ret.putBoolean(KEY_F_NBT, fNbt);
        return ret;
    }

    @Override
    public boolean matches(Object l) {
        if (l instanceof LItemStack lis) {
            if (item != lis.item) return false;
            if (!fNbt && !lis.fNbt) {
                if (!Objects.equals(nbt, lis.nbt)) return false;
            }
            if (!fMeta) {
                // check meta via item damage
            }
            return true;
        }
        return false;
    }

    public static boolean merge(ILabel a, ILabel b) {
        if (a instanceof LItemStack ia && b instanceof LItemStack ib) {
            return ia.item == ib.item;
        }
        return false;
    }

    public static List<ILabel> suggest(List<ILabel> is, @Nullable Class<?> context) {
        return Collections.emptyList();
    }

    public static List<ILabel> fallback(List<ILabel> is, @Nullable Class<?> context) {
        List<ILabel> ret = new ArrayList<>();
        if (is.size() == 1) {
            ILabel label = is.getFirst();
            if (label instanceof LItemStack lis) {
                if (!lis.fNbt && !lis.fMeta) {
                    ret.add(lis.copy().setFMeta(true));
                    ret.add(lis.copy().setFNbt(true));
                    ret.add(lis.copy().setFMeta(true).setFNbt(true));
                }
            }
        }
        return ret;
    }
}
