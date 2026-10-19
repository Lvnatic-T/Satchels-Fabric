package net.lvnatic.satchels.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.lvnatic.satchels.Satchels;

public record ToggleSatchelPacketC2S(boolean enabled) implements CustomPacketPayload {
    public static final Type<ToggleSatchelPacketC2S> TYPE = new Type<>(Satchels.at("toggle_satchel"));

    public static final StreamCodec<ByteBuf, ToggleSatchelPacketC2S> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ToggleSatchelPacketC2S::enabled,
            ToggleSatchelPacketC2S::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}