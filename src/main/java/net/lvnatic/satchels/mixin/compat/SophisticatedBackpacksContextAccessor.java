package net.lvnatic.satchels.mixin.compat;

import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BackpackContext.Item.class, remap = false)
public interface SophisticatedBackpacksContextAccessor {
    @Accessor(value = "handlerName", remap = false)
    String satchels$getHandlerName();
}