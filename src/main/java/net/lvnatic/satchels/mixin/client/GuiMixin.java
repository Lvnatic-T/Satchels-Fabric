package net.lvnatic.satchels.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.lvnatic.satchels.Satchels;
import net.lvnatic.satchels.api.SatchelAccess;
import net.lvnatic.satchels.client.SatchelsClient;
import net.lvnatic.satchels.client.SatchelsClientConfig;
import net.lvnatic.satchels.content.satchel.SatchelData;
import net.lvnatic.satchels.content.satchel.SatchelInventory;
import net.lvnatic.satchels.content.satchel.SatchelItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Unique private float satchels$slide = 24.0f;
    @Unique private float satchels$startSlide = 24.0f;
    @Unique private float satchels$target = 24.0f;
    @Unique private long satchels$animationStart;

    @Inject(method = "renderItemHotbar", at = @At("HEAD"))
    private void satchels$updateAnimation(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();

        boolean active = minecraft.player != null &&
                SatchelData.get(minecraft.player).isActive() &&
                SatchelData.get(minecraft.player).canAccess();

        float target = active ? 0.0f : 24.0f;
        long now = Util.getMillis();

        if (!SatchelsClientConfig.shouldAnimateGUI()) {
            satchels$slide = target;
            satchels$startSlide = target;
            satchels$target = target;
            satchels$animationStart = now;
            return;
        }

        float progress = Math.clamp((now - satchels$animationStart) / 300.0f, 0.0f, 1.0f);
        float easing = progress >= 1.0f
                ? 1.0f
                : 1.0f - (float) Math.pow(2.0, -10.0 * progress);

        satchels$slide = satchels$startSlide +
                (satchels$target - satchels$startSlide) * easing;

        if (target != satchels$target) {
            satchels$startSlide = satchels$slide;
            satchels$target = target;
            satchels$animationStart = now;
        }
    }

    @WrapOperation(
            method = "renderItemHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V"
            )
    )
    private void satchels$renderVanillaSlot(
            Gui gui,
            GuiGraphics graphics,
            int x,
            int y,
            DeltaTracker deltaTracker,
            Player player,
            ItemStack stack,
            int seed,
            Operation<Void> original
    ) {
        SatchelData data = SatchelData.get(player);

        if (seed >= 1 && seed <= 9 &&
                data.isSlotInSatchel(seed - 1) &&
                (data.isActive() || satchels$slide < 23.0f)) {
            return;
        }

        original.call(gui, graphics, x, y, deltaTracker, player, stack, seed);
    }

    @Inject(method = "renderItemHotbar", at = @At("TAIL"))
    private void satchels$renderOverlay(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.gameMode == null) return;
        if (minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) return;
        if (satchels$slide >= 24.0f) return;

        Player player = minecraft.player;
        SatchelData data = SatchelData.get(player);
        SatchelInventory inventory = data.getSatchelInventory();

        int x = graphics.guiWidth() / 2 - 91 + data.getHotbarOffset() * 20;
        int y = graphics.guiHeight() - 22 + Math.round(satchels$slide);

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);

        SatchelsClient.applySatchelColor(graphics, player);
        graphics.blitSprite(Satchels.at("satchel_hotbar"), x + 1, y, 120, 22);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        int selected = player.getInventory().selected;

        if (data.isSlotInSatchel(selected)) {
            int color = SatchelAccess.getSatchelTint(player);
            if (color == -1) color = SatchelItem.DEFAULT_COLOR;

            int red = (color >> 16) & 255;
            int green = (color >> 8) & 255;
            int blue = color & 255;

            float[] hsb = Color.RGBtoHSB(red, green, blue, null);
            hsb[0] = Math.max(hsb[0] - 0.01f, 0.0f);
            hsb[1] = Math.max(hsb[1] - 0.1f, 0.0f);
            hsb[2] = Math.min(hsb[2] + 0.1f, 1.0f);

            int selectionColor = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);

            graphics.setColor(
                    ((selectionColor >> 16) & 255) / 255.0f,
                    ((selectionColor >> 8) & 255) / 255.0f,
                    (selectionColor & 255) / 255.0f,
                    1.0f
            );

            graphics.blitSprite(
                    Satchels.at("satchel_hotbar_selection"),
                    x - 1 + (selected - data.getHotbarOffset()) * 20,
                    y - 1,
                    24,
                    24
            );

            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        for (int i = 0; i < SatchelInventory.SIZE; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;

            int itemX = x + 3 + i * 20;
            int itemY = y + 3;

            float pop = stack.getPopTime() -
                    deltaTracker.getGameTimeDeltaPartialTick(false);

            if (pop > 0.0f) {
                float scale = 1.0f + pop / 5.0f;

                graphics.pose().pushPose();
                graphics.pose().translate(itemX + 8.0f, itemY + 12.0f, 0);
                graphics.pose().scale(
                        1.0f / scale,
                        (scale + 1.0f) / 2.0f,
                        1.0f
                );
                graphics.pose().translate(-itemX - 8.0f, -itemY - 12.0f, 0);
            }

            graphics.renderItem(player, stack, itemX, itemY, i + 1);

            if (pop > 0.0f) graphics.pose().popPose();

            graphics.renderItemDecorations(minecraft.font, stack, itemX, itemY);
        }

        graphics.pose().popPose();
    }
}