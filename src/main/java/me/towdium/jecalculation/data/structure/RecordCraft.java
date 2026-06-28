package me.towdium.jecalculation.data.structure;

import me.towdium.jecalculation.data.label.ILabel;
import me.towdium.jecalculation.utils.Utilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecordCraft implements IRecord {
    public static final String KEY_RECENTS = "recents";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_INVENTORY = "inventory";
    public static final String KEY_MODE = "mode";
    public static final String KEY_OVERLAY_OPEN = "overlayOpen";
    public static final String KEY_OVERLAY_X = "overlayPositionX";
    public static final String KEY_OVERLAY_Y = "overlayPositionY";
    public static final String KEY_OVERLAY_DEPTH = "overlayDepth";

    Utilities.Recent<ILabel> record = new Utilities.Recent<>((a, b) ->
            a == ILabel.EMPTY || a.equals(b), 9);
    public String amount;
    public boolean inventory;
    public Mode mode;
    public boolean overlayOpen;
    public int overlayPositionX;
    public int overlayPositionY;
    public int overlayDepth;

    public RecordCraft(CompoundTag nbt) {
        List<ILabel> ls = nbt.getListOrEmpty(KEY_RECENTS).stream()
                .filter(n -> n instanceof CompoundTag)
                .map(n -> ILabel.SERIALIZER.deserialize((CompoundTag) n))
                .collect(Collectors.toList());
        new Utilities.ReversedIterator<>(ls).forEachRemaining(l -> record.push(l, false));
        amount = nbt.getStringOr(KEY_AMOUNT, "");
        inventory = nbt.getBooleanOr(KEY_INVENTORY, false);
        String s = nbt.getStringOr(KEY_MODE, "");
        mode = Mode.INPUT;
        for (Mode m : Mode.values()) {
            if (s.equals(m.toString().toLowerCase())) mode = m;
        }

        overlayOpen = nbt.getBooleanOr(KEY_OVERLAY_OPEN, false);
        overlayPositionX = nbt.getIntOr(KEY_OVERLAY_X, 0);
        overlayPositionY = nbt.getIntOr(KEY_OVERLAY_Y, 0);
        overlayDepth = nbt.getIntOr(KEY_OVERLAY_DEPTH, 0);
    }

    // return true if any existing matches
    public boolean push(ILabel label, boolean replace) {
        return record.push(label, replace);
    }

    public ILabel getLatest() {
        return record.size() == 0 ? ILabel.EMPTY : record.toList().getFirst();
    }

    public List<ILabel> getHistory() {
        return record.size() > 1 ? record.toList().subList(1, record.size()) : new ArrayList<>();
    }

    public CompoundTag serialize() {
        CompoundTag ret = new CompoundTag();
        ret.putBoolean(KEY_INVENTORY, inventory);
        ret.putString(KEY_AMOUNT, amount);
        ListTag recent = new ListTag();
        record.toList().forEach(l -> recent.add(ILabel.SERIALIZER.serialize(l)));
        ret.put(KEY_RECENTS, recent);
        ret.putString(KEY_MODE, mode.toString().toLowerCase());
        ret.putBoolean(KEY_OVERLAY_OPEN, overlayOpen);
        ret.putInt(KEY_OVERLAY_X, overlayPositionX);
        ret.putInt(KEY_OVERLAY_Y, overlayPositionY);
        ret.putInt(KEY_OVERLAY_DEPTH, overlayDepth);
        return ret;
    }

    public enum Mode {
        INPUT, OUTPUT, CATALYST, STEPS
    }
}
