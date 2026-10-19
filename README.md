# Lvnatic's Satchels

**A Fabric port of [Vercte's Satchels](https://modrinth.com/mod/vercte-satchels) for Minecraft 1.21.1.**

Lvnatic's Satchels introduces a simple way to expand your inventory without adding another backpack interface. Equipping a Satchel gives you **six additional inventory slots** beneath your hotbar, which can be accessed through a configurable hotbar toggle.

Whether you're building, exploring, or carrying several different tools, the Satchel lets you organize commonly used items separately from your regular inventory and switch between them whenever you need them.

The Satchel also integrates into vanilla inventory screens, allowing you to manage its contents while using chests, furnaces, crafting tables, and other containers.

And yes, you can dye it!

## Features

- **Six Additional Inventory Slots:** Expand your available inventory space with an extra row of six slots
- **Hotbar Swapping:** Switch between your regular hotbar and Satchel items using a single configurable keybind
- **Customizable Position:** Move the six Satchel slots between positions 1–6 and 4–9 of your hotbar
- **Inventory Integration:** Access your Satchel through inventories, as well as supported container interfaces
- **Accessories Integration:** Equip the Satchel in its dedicated Accessories slot
- **Custom 3D Model:** Display your equipped Satchel on your character, including its color and Accessories visibility settings
- **Dyeable:** Customize your Satchel using dyes and remove its color using a water cauldron
- **Item Management:** Supports inventory transfers, item pickup prioritization, and vanilla inventory-clearing commands

## Controls

| Control | Action                                                                                       |
|---|----------------------------------------------------------------------------------------------|
| `V` | Toggle between your regular hotbar and Satchel hotbar                                        |
| `Shift + Hotbar Key` | Swap an inventory item with the corresponding Satchel slot while an inventory screen is open |
| Controls → Satchel Position | Change the position of the Satchel slots, can also be configured in the config instead       |

The hotbar toggle can be rebound in Minecraft's Controls menu. Shift-swapping can be disabled through the client configuration.

## Configuration

The client configuration is located at:

`config/satchels-client.toml`

```toml
[gameplay]
satchel_offset = 0
shift_swap = true

[rendering]
gui_animation = true
```

**Gameplay**
- `satchel_offset`: Controls the starting position of the Satchel on the hotbar (`0`–`3`). Can also be configured in game instead.
- `shift_swap`: Enables or disables swapping inventory items into Satchel slots using Shift + hotbar key.

**Rendering**
- `gui_animation`: Enables or disables the Satchel hotbar transition animation.

The Satchel's equipped 3D model can be hidden through Accessories' built-in visibility toggle.

## Requirements

- **Minecraft:** Java Edition 1.21.1
- **Mod Loader:** Fabric
- **Dependencies:** Fabric API and Accessories

The mod contains both client-side and server-side functionality. Multiplayer and dedicated-server behavior have not yet been fully verified for this port.

## Credits

**Original Mod:** [Vercte's Satchels](https://modrinth.com/mod/vercte-satchels) by [Vercte](https://github.com/vercte)    
**Source Code:** [Lvnatic-T/Satchels-Fabric](https://github.com/Lvnatic-T/Satchels-Fabric)

This is an independently maintained Fabric port of Vercte's original NeoForge mod, distributed under the MIT License. Original authorship and copyright notices are retained.

For additional attribution information, see [ATTRIBUTION.md](ATTRIBUTION.md).