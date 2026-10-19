package net.lvnatic.satchels.network;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventorySlot;
import net.lvnatic.satchels.network.packets.SatchelInventorySyncPacketS2C;
import net.lvnatic.satchels.network.packets.SatchelOffsetUpdatePacketC2S;
import net.lvnatic.satchels.network.packets.SatchelStatusPacketS2C;
import net.lvnatic.satchels.network.packets.SwapSatchelHotbarPacketC2S;
import net.lvnatic.satchels.network.packets.ToggleSatchelPacketC2S;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ModPackets {
    private static final Map<UUID, List<ItemStack>> LAST_SYNC = new HashMap<>();

    public static void initialize() {
        PayloadTypeRegistry.playC2S().register(
                ToggleSatchelPacketC2S.TYPE,
                ToggleSatchelPacketC2S.STREAM_CODEC
        );

        PayloadTypeRegistry.playC2S().register(
                SatchelOffsetUpdatePacketC2S.TYPE,
                SatchelOffsetUpdatePacketC2S.STREAM_CODEC
        );

        PayloadTypeRegistry.playC2S().register(
                SwapSatchelHotbarPacketC2S.TYPE,
                SwapSatchelHotbarPacketC2S.STREAM_CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                SatchelStatusPacketS2C.TYPE,
                SatchelStatusPacketS2C.STREAM_CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                SatchelInventorySyncPacketS2C.TYPE,
                SatchelInventorySyncPacketS2C.STREAM_CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(ToggleSatchelPacketC2S.TYPE, (packet, context) -> {
            SatchelData data = SatchelData.get(context.player());
            boolean enabled = packet.enabled() && data.canAccess();

            if (data.isActive() != enabled) data.setActive(enabled, true);

            ServerPlayNetworking.send(context.player(), new SatchelStatusPacketS2C(enabled));
        });

        ServerPlayNetworking.registerGlobalReceiver(SatchelOffsetUpdatePacketC2S.TYPE, (packet, context) ->
                SatchelData.get(context.player()).setHotbarOffset(packet.offset())
        );

        ServerPlayNetworking.registerGlobalReceiver(SwapSatchelHotbarPacketC2S.TYPE, (packet, context) -> {
            AbstractContainerMenu menu = context.player().containerMenu;
            SatchelData data = SatchelData.get(context.player());

            if (!data.canAccess() || menu.containerId != packet.menuId()) return;
            if (!menu.getCarried().isEmpty()) return;
            if (packet.sourceSlot() < 0 || packet.sourceSlot() >= menu.slots.size()) return;
            if (packet.satchelSlot() < 0 || packet.satchelSlot() >= data.getSatchelInventory().getContainerSize()) return;

            Slot source = menu.slots.get(packet.sourceSlot());
            SatchelInventorySlot target = null;

            for (Slot slot : menu.slots) {
                if (slot instanceof SatchelInventorySlot satchelSlot &&
                        slot.getContainerSlot() == packet.satchelSlot()) {
                    target = satchelSlot;
                    break;
                }
            }

            if (target == null || source == target) return;

            ItemStack sourceStack = source.getItem();
            ItemStack targetStack = target.getItem();

            if (!source.mayPickup(context.player()) || !target.mayPickup(context.player())) return;
            if (!source.mayPlace(targetStack) || !target.mayPlace(sourceStack)) return;
            if (targetStack.getCount() > source.getMaxStackSize(targetStack)) return;
            if (sourceStack.getCount() > target.getMaxStackSize(sourceStack)) return;

            source.set(targetStack.copy());
            target.set(sourceStack.copy());

            menu.broadcastChanges();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            LAST_SYNC.remove(handler.player.getUUID());

            SatchelData data = SatchelData.get(handler.player);
            ServerPlayNetworking.send(handler.player, new SatchelStatusPacketS2C(data.isActive()));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                LAST_SYNC.remove(handler.player.getUUID())
        );

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                LAST_SYNC.remove(newPlayer.getUUID())
        );

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerList().getPlayers()) {
                List<ItemStack> current = SatchelData.get(player).getSatchelInventory().getItems();
                List<ItemStack> previous = LAST_SYNC.get(player.getUUID());

                if (previous != null && ItemStack.listMatches(previous, current)) continue;

                List<ItemStack> snapshot = current.stream().map(ItemStack::copy).toList();
                LAST_SYNC.put(player.getUUID(), snapshot);

                ServerPlayNetworking.send(player, new SatchelInventorySyncPacketS2C(snapshot));
            }
        });
    }
}