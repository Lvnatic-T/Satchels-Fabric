package net.lvnatic.satchels.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.lvnatic.satchels.Satchels;
import net.lvnatic.satchels.content.satchel.SatchelInventory;

public final class ClientSatchelTooltip implements ClientTooltipComponent {
    private static final int WIDTH = 120;
    private static final int HEIGHT = 22;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SatchelInventory.SIZE, ItemStack.EMPTY);
    private final int color;

    public ClientSatchelTooltip(ItemContainerContents contents, int color) {
        contents.copyInto(items);
        this.color = color;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        return WIDTH;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        graphics.setColor(
                ((color >> 16) & 255) / 255.0f,
                ((color >> 8) & 255) / 255.0f,
                (color & 255) / 255.0f,
                1.0f
        );
        graphics.blitSprite(Satchels.at("satchel_hotbar"), x, y, WIDTH, HEIGHT);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        for (int i = 0; i < SatchelInventory.SIZE; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;

            int itemX = x + 2 + i * 20;
            int itemY = y + 3;
            graphics.renderFakeItem(stack, itemX, itemY);
            graphics.renderItemDecorations(font, stack, itemX, itemY);
        }
    }
}