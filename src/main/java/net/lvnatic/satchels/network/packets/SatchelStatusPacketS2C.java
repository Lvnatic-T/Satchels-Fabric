package net.lvnatic.satchels.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.lvnatic.satchels.Satchels;

public record SatchelStatusPacketS2C(boolean enabled) implements CustomPacketPayload {
    public static final Type<SatchelStatusPacketS2C> TYPE = new Type<>(Satchels.at("satchel_status"));

    public static final StreamCodec<ByteBuf, SatchelStatusPacketS2C> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SatchelStatusPacketS2C::enabled,
            SatchelStatusPacketS2C::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}