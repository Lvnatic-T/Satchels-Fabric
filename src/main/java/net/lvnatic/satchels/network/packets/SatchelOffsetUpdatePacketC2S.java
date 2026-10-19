package net.lvnatic.satchels.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.lvnatic.satchels.Satchels;

public record SatchelOffsetUpdatePacketC2S(int offset) implements CustomPacketPayload {
    public static final Type<SatchelOffsetUpdatePacketC2S> TYPE =
            new Type<>(Satchels.at("satchel_offset_update"));

    public static final StreamCodec<ByteBuf, SatchelOffsetUpdatePacketC2S> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    SatchelOffsetUpdatePacketC2S::offset,
                    SatchelOffsetUpdatePacketC2S::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}