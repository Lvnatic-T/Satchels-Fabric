package net.lvnatic.satchels.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.lvnatic.satchels.Satchels;

public record SatchelPickupModePacketC2S(int mode) implements CustomPacketPayload {
    public static final Type<SatchelPickupModePacketC2S> TYPE = new Type<>(Satchels.at("pickup_mode"));

    public static final StreamCodec<ByteBuf, SatchelPickupModePacketC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SatchelPickupModePacketC2S::mode,
            SatchelPickupModePacketC2S::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
