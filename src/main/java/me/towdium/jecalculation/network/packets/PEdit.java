package me.towdium.jecalculation.network.packets;

import me.towdium.jecalculation.data.structure.Recipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public class PEdit implements CustomPacketPayload {
    public static final Type<PEdit> TYPE = new Type<>(Identifier.fromNamespaceAndPath("jecalculation", "edit"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PEdit> CODEC = StreamCodec.of(
            PEdit::encode, PEdit::decode
    );

    static final String KEY_OLD = "old";
    static final String KEY_NEW = "new";
    static final String KEY_INDEX = "index";
    static final String KEY_RECIPE = "recipe";

    String old, neu;
    int index;
    Recipe recipe;

    public PEdit() {
    }

    public PEdit(String neu, @Nullable String old, int index, @Nullable Recipe recipe) {
        this.neu = neu;
        this.old = old;
        this.index = index;
        this.recipe = recipe;
    }

    public PEdit(RegistryFriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        tag = tag == null ? new CompoundTag() : tag;
        old = tag.contains(KEY_OLD) ? tag.getStringOr(KEY_OLD, "") : null;
        neu = tag.getStringOr(KEY_NEW, "");
        index = tag.getIntOr(KEY_INDEX, -1);
        recipe = tag.contains(KEY_RECIPE) ? new Recipe(tag.getCompoundOrEmpty(KEY_RECIPE)) : null;
    }

    private static PEdit decode(RegistryFriendlyByteBuf buf) {
        return new PEdit(buf);
    }

    private static void encode(RegistryFriendlyByteBuf buf, PEdit packet) {
        packet.write(buf);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        if (old != null) tag.putString(KEY_OLD, old);
        tag.putString(KEY_NEW, neu);
        tag.putInt(KEY_INDEX, index);
        if (recipe != null) tag.put(KEY_RECIPE, recipe.serialize());
        buf.writeNbt(tag);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
