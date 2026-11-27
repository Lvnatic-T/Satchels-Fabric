package net.lvnatic.satchels.client.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.ItemContainerContents;

public record SatchelTooltipData(ItemContainerContents contents, int color) implements TooltipComponent {
}