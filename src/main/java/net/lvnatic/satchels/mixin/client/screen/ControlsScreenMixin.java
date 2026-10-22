
package net.lvnatic.satchels.mixin.client.screen;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.network.chat.Component;
import net.lvnatic.satchels.client.SatchelsClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ControlsScreen.class)
public abstract class ControlsScreenMixin extends OptionsSubScreen {
    protected ControlsScreenMixin(Screen previous, Options options, Component title) {
        super(previous, options, title);
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void satchels$addOptions(CallbackInfo ci) {
        if (this.list == null) return;

        OptionInstance<Integer> offset = new OptionInstance<>(
                "satchels.options.offset",
                OptionInstance.cachedConstantTooltip(
                        Component.translatable("satchels.options.offset.tooltip")
                ),
                (component, value) -> Component.translatable(
                        "satchels.options.offset.selection",
                        value + 1,
                        value + 6
                ),
                new OptionInstance.ClampingLazyMaxIntRange(0, () -> 3, 3),
                SatchelsClientConfig.getSatchelOffset(),
                SatchelsClientConfig::updateOffset
        );

        OptionInstance<Boolean> shiftSwap = OptionInstance.createBoolean(
                "satchels.options.shift_swap",
                OptionInstance.cachedConstantTooltip(
                        Component.translatable("satchels.options.shift_swap.tooltip")
                ),
                SatchelsClientConfig.shouldSwapWithShiftKey(),
                SatchelsClientConfig::updateShiftSwap
        );

        OptionInstance<Boolean> guiAnimation = OptionInstance.createBoolean(
                "satchels.options.gui_animation",
                OptionInstance.cachedConstantTooltip(
                        Component.translatable("satchels.options.gui_animation.tooltip")
                ),
                SatchelsClientConfig.shouldAnimateGUI(),
                SatchelsClientConfig::updateGuiAnimation
        );

        this.list.addSmall(offset, shiftSwap, guiAnimation);
    }
}
