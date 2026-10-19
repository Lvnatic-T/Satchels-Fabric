package net.lvnatic.satchels.network.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.lvnatic.satchels.Satchels;

import java.util.List;

public record SatchelInventorySyncPacketS2C(List<ItemStack> items) implements CustomPacketPayload {
    public static final Type<SatchelInventorySyncPacketS2C> TYPE =
            new Type<>(Satchels.at("satchel_inventory_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SatchelInventorySyncPacketS2C> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.OPTIONAL_LIST_STREAM_CODEC,
                    SatchelInventorySyncPacketS2C::items,
                    SatchelInventorySyncPacketS2C::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}