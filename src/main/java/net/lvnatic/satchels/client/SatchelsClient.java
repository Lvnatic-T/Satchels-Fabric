package net.lvnatic.satchels.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.lvnatic.satchels.ModItems;
import net.lvnatic.satchels.Satchels;
import net.lvnatic.satchels.api.SatchelAccess;
import net.lvnatic.satchels.client.model.SatchelLayer;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventory;
import net.lvnatic.satchels.content.satchel.SatchelItem;
import net.lvnatic.satchels.network.packets.SatchelInventorySyncPacketS2C;
import net.lvnatic.satchels.network.packets.SatchelOffsetUpdatePacketC2S;
import net.lvnatic.satchels.network.packets.SatchelStatusPacketS2C;
import net.lvnatic.satchels.network.packets.ToggleSatchelPacketC2S;
import org.lwjgl.glfw.GLFW;

public final class SatchelsClient implements ClientModInitializer {
    private static KeyMapping toggleSatchel;
    private Player lastPlayer;

    @Override
    public void onInitializeClient() {
        SatchelsClientConfig.load();

        ModelLoadingPlugin.register(context ->
                context.addModels(Satchels.at("item/satchel_worn"))
        );

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, registrationHelper, context) -> {
            if (renderer instanceof PlayerRenderer playerRenderer) {
                registrationHelper.register(new SatchelLayer(playerRenderer, context.getItemRenderer()));
            }
        });

        ColorProviderRegistry.ITEM.register(
                (stack, layer) -> layer == 0
                        ? DyedItemColor.getOrDefault(stack, SatchelItem.DEFAULT_COLOR)
                        : -1,
                ModItems.SATCHEL
        );

        SatchelAccess.SATCHEL_TINT_GETTERS.add(player -> {
            ItemStack stack = SatchelAccess.getSatchelStack(player);
            return stack.isEmpty() ? -1 : DyedItemColor.getOrDefault(stack, SatchelItem.DEFAULT_COLOR);
        });

        toggleSatchel = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.satchels.toggle_satchel",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KeyMapping.CATEGORY_INVENTORY
        ));

        ItemTooltipCallback.EVENT.register((stack, context, type, tooltip) -> {
            if (!stack.is(ModItems.SATCHEL)) return;

            tooltip.add(
                    Component.translatable("tooltip.satchels.storage")
                            .withStyle(ChatFormatting.GRAY)
            );

            tooltip.add(
                    Component.translatable(
                            "tooltip.satchels.toggle",
                            toggleSatchel.getTranslatedKeyMessage()
                                    .copy()
                                    .withStyle(ChatFormatting.YELLOW)
                    ).withStyle(ChatFormatting.GRAY)
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != lastPlayer) {
                lastPlayer = client.player;

                if (client.player != null && client.getConnection() != null) {
                    int offset = SatchelsClientConfig.getSatchelOffset();
                    SatchelData.get(client.player).setHotbarOffset(offset);
                    ClientPlayNetworking.send(new SatchelOffsetUpdatePacketC2S(offset));
                }
            }

            while (toggleSatchel.consumeClick()) {
                if (client.player == null || client.getConnection() == null) continue;

                SatchelData data = SatchelData.get(client.player);
                if (!data.canAccess()) continue;

                ClientPlayNetworking.send(new ToggleSatchelPacketC2S(!data.isActive()));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SatchelStatusPacketS2C.TYPE, (packet, context) ->
                context.client().execute(() -> {
                    if (context.client().player == null) return;

                    SatchelData data = SatchelData.get(context.client().player);
                    boolean changed = data.isActive() != packet.enabled();

                    data.setActive(packet.enabled(), false);

                    if (changed) {
                        context.client().player.displayClientMessage(
                                Component.translatable(packet.enabled()
                                        ? "message.satchels.activated"
                                        : "message.satchels.deactivated"),
                                true
                        );
                    }
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(SatchelInventorySyncPacketS2C.TYPE, (packet, context) ->
                context.client().execute(() -> {
                    if (context.client().player == null) return;
                    if (packet.items().size() != SatchelInventory.SIZE) return;

                    SatchelInventory inventory = SatchelData.get(context.client().player).getSatchelInventory();

                    for (int i = 0; i < SatchelInventory.SIZE; i++) {
                        inventory.setItem(i, packet.items().get(i).copy());
                    }
                })
        );
    }

    public static void applySatchelColor(GuiGraphics graphics, Player player) {
        int color = SatchelAccess.getSatchelTint(player);
        if (color == -1) color = SatchelItem.DEFAULT_COLOR;

        graphics.setColor(
                ((color >> 16) & 255) / 255.0f,
                ((color >> 8) & 255) / 255.0f,
                (color & 255) / 255.0f,
                1.0f
        );
    }
}