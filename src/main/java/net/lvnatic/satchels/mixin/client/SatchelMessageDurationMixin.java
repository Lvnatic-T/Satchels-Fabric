package net.lvnatic.satchels.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class SatchelMessageDurationMixin {
    @Shadow private int overlayMessageTime;

    @Inject(method = "setOverlayMessage", at = @At("TAIL"))
    private void satchels$shortenMessage(Component message, boolean animateColor, CallbackInfo ci) {
        if (!(message.getContents() instanceof TranslatableContents translation)) return;

        String key = translation.getKey();

        if ("message.satchels.activated".equals(key) ||
                "message.satchels.deactivated".equals(key)) {
            overlayMessageTime = 35;
        }
    }
}