package net.lvnatic.satchels.mixin.client.screen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.lvnatic.satchels.content.satchel.SatchelInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {
    @WrapOperation(
            method = "selectTab",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/NonNullList;add(Ljava/lang/Object;)Z",
                    ordinal = 2
            )
    )
    private boolean satchels$excludeSatchelSlots(NonNullList<?> list, Object object, Operation<Boolean> original) {
        if (object instanceof Slot slot && slot.container instanceof SatchelInventory) {
            return false;
        }

        return original.call(list, object);
    }
}