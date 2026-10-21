package net.lvnatic.satchels;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.lvnatic.satchels.content.satchel.SatchelItem;

public final class ModItems {
    public static final Item SATCHEL = Registry.register(
            BuiltInRegistries.ITEM,
            Satchels.at("satchel"),
            new SatchelItem(new Item.Properties().stacksTo(1))
    );

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.addAfter(Items.LEAD, SATCHEL));
    }
}
