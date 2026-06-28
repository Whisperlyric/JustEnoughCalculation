package me.towdium.jecalculation.network.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class PCalculator implements CustomPacketPayload {
    public static final Type<PCalculator> TYPE = new Type<>(Identifier.fromNamespaceAndPath("jecalculation", "calculator"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PCalculator> CODEC = StreamCodec.of(
            PCalculator::encode, PCalculator::decode
    );

    ItemStack stack;
    int slot;

    public PCalculator(RegistryFriendlyByteBuf buf) {
        stack = ItemStack.STREAM_CODEC.decode(buf);
        slot = buf.readInt();
    }

    public PCalculator(ItemStack stack, int slot) {
        this.stack = stack;
        this.slot = slot;
    }

    private static PCalculator decode(RegistryFriendlyByteBuf buf) {
        return new PCalculator(buf);
    }

    private static void encode(RegistryFriendlyByteBuf buf, PCalculator packet) {
        packet.write(buf);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        ItemStack.STREAM_CODEC.encode(buf, stack);
        buf.writeInt(slot);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
