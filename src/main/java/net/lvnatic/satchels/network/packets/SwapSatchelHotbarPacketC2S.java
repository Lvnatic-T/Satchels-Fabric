package net.lvnatic.satchels.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.lvnatic.satchels.Satchels;

public record SwapSatchelHotbarPacketC2S(int menuId, int sourceSlot, int satchelSlot) implements CustomPacketPayload {
    public static final Type<SwapSatchelHotbarPacketC2S> TYPE =
            new Type<>(Satchels.at("swap_satchel_hotbar"));

    public static final StreamCodec<ByteBuf, SwapSatchelHotbarPacketC2S> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    SwapSatchelHotbarPacketC2S::menuId,
                    ByteBufCodecs.VAR_INT,
                    SwapSatchelHotbarPacketC2S::sourceSlot,
                    ByteBufCodecs.VAR_INT,
                    SwapSatchelHotbarPacketC2S::satchelSlot,
                    SwapSatchelHotbarPacketC2S::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}