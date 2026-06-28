package me.towdium.jecalculation.network.packets;

import me.towdium.jecalculation.data.structure.RecordPlayer;
import me.towdium.jecalculation.data.label.labels.LPlaceholder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public class PRecord implements CustomPacketPayload {
    public static final Type<PRecord> TYPE = new Type<>(Identifier.fromNamespaceAndPath("jecalculation", "record"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PRecord> CODEC = StreamCodec.of(
            PRecord::encode, PRecord::decode
    );

    public static final String KEY_RECIPES = "recipes";
    public static final String KEY_LAST = "last";
    RecordPlayer record;

    public PRecord(RecordPlayer record) {
        this.record = record;
    }

    public PRecord() {
    }

    public PRecord(RegistryFriendlyByteBuf buf) {
        CompoundTag tag = Objects.requireNonNull(buf.readNbt());
        LPlaceholder.state = false;
        record = new RecordPlayer(tag);
    }

    private static PRecord decode(RegistryFriendlyByteBuf buf) {
        return new PRecord(buf);
    }

    private static void encode(RegistryFriendlyByteBuf buf, PRecord packet) {
        packet.write(buf);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeNbt(record.serialize());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
