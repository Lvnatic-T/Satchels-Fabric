<center>

![Requires 1.21.1](https://img.shields.io/badge/Requires-Minecraft_1.21.1-62B447)
![Requires Fabric](https://img.shields.io/badge/Requires-Fabric-lightyellow)
[![Requires Accessories](https://img.shields.io/badge/Requires-Accessories-D7809B)](https://modrinth.com/mod/accessories)
![Required on Client and Server](https://img.shields.io/badge/Required_on-Client_%26_Server-28A6A0)
[![GitHub Source](https://img.shields.io/badge/Source-GitHub-0969DA?logo=github)](https://github.com/Lvnatic-T/Satchels-Fabric)
[![MIT License](https://img.shields.io/badge/License-MIT-007EC6)](https://github.com/Lvnatic-T/Satchels-Fabric/blob/main/LICENSE)

[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support_Me-FF5E5B?logo=kofi)](https://ko-fi.com/allthemons)
[![Modrinth](https://img.shields.io/badge/Modrinth-Lvnatic-00AF5C?logo=modrinth)](https://modrinth.com/user/Lvnatic)
[![CurseForge](https://img.shields.io/badge/CurseForge-Lvnatic-F16436?logo=curseforge)](https://www.curseforge.com/members/lvnatic/projects)

---

![Lvnatics Satchels banner](https://cdn.modrinth.com/data/cached_images/8ff0beb4faec3603d17776104f0de13f2738d383.png)


Lvnatic's Satchels is a Fabric port of [Vercte's Satchels](https://modrinth.com/mod/vercte-satchels), adding six extra inventory slots and a swappable hotbar without the need for a separate backpack screen. Keep your favorite tools, building blocks, or exploration essentials close at hand, and switch between your regular hotbar and Satchel with one key.

</center>







# What do these Satchels offer?

### Additional Inventory Slots & Accessibility:
Carry six extra stacks of items. Your Satchel slots appear alongside your inventory in supported containers, making it easy to move items around without opening a separate storage interface.

![Container UI demonstration](https://cdn.modrinth.com/data/k5D5FLQ4/images/0c59c6e28acc1b22c83d753732b8b71a64139f51.png)

---

### Seamless hotbar Swapping:
Press `V` _(configurable)_ to switch between your normal hotbar and the Satchel hotbar. Use the items in your Satchel just like items in your regular hotbar, and use`Shift` + `hotbar key` in inventory screens to swap items directly with Satchel slots. The Satchel item tooltip shows your current toggle key.

<img src="https://cdn.modrinth.com/data/cached_images/1aaed0540fab82f17626d3bd0ffc36bcaebdd2a4.gif" alt="Hotbar toggle demonstration2" width="55%">


---

### Adjustable position and animations:
Choose between four different positions for your Satchel slots. You can also enable or disable Shift + hotbar swapping and the sliding hotbar animation, all from Minecraft's **Options → Controls** menu.

<img src="https://cdn.modrinth.com/data/cached_images/1767ad12c6a08ff08640737010753293c27c2ee9.gif" alt="Hotbar position demonstration" width="55%">

---

### Accessories Integration:
Equip the Satchel in its dedicated [Accessories](https://modrinth.com/mod/accessories) slot. Your character wears a custom Satchel model, and the item can also be equipped by right-clicking it. In Creative Mode, find it in `Tools & Utilities`, immediately after the Lead.

<!-- GIF OF THE ACCESSORIES GUI -->

---

### Customize your appearance:
Dye your Satchel to match your style! Its color is reflected in the item, the worn model & the hotbar interface.

<img src="https://cdn.modrinth.com/data/cached_images/44e19faccf3dad55b85103b4a8a15cf90fa49810.gif" alt="3D Model colors demonstration" width="55%">

<img src="https://cdn.modrinth.com/data/cached_images/efa7aae2ca1c61e90bee731a7e5976f129863cc2.gif" alt="Hotbar colors demonstration" width="55%">


---

### Datapack-configurable storage blacklist:

To prevent nesting storage inside storage, Satchels comes with a default blacklist covering chests, trapped chests, ender chests, barrels, all shulker box colors, bundles & Sophisticated Backpacks.

<details>
<summary>Click to expand</summary>

The blacklist uses the `#satchels:storage_blacklist` item tag and can be extended or replaced by datapacks. Blacklisted items cannot be inserted into the Satchel, including through automatic pickup and hotbar swapping. Items already stored when the blacklist changes remain removable.

To add more items, create this file in your datapack:

`data/satchels/tags/item/storage_blacklist.json`

```json
{
  "replace": false,
  "values": [
    "minecraft:hopper",
    {
      "id": "examplemod:storage_item",
      "required": false
    }
  ]
}
```
- The blacklist is enforced by the server and doesn't require changes to the client
- Use `"replace": false` to add restrictions to the built-in blacklist
- To replace the default entries entirely, use `"replace": true`
- Run **`/reload`** after changing the datapack

</details>











# Controls & Configuration

| Control / Setting           | Function                                                                    |
|-----------------------------|-----------------------------------------------------------------------------|
| `V` _(rebindable)_          | Toggle between the regular and Satchel hotbars                              |
| `Shift` + <br/>`Hotbar Key` | Swap an item with the corresponding Satchel slot while an inventory is open |
| Satchel Position            | Choose one of four positions for the Satchel hotbar slots                   |
| Shift + <br/>Hotbar Swap    | Enable or disable the Shift + hotbar-key shortcut                           |
| Satchel Animations          | Enable or disable the sliding hotbar animation                              |

All Satchel settings are available under `ESC` → `Options` → `Controls` & are also saved to its config found under `config/satchels-client.toml`. The hotbar toggle itself can be rebound in Minecraft's keybind settings.










# Languages

Available in English, German & Spanish _(european spanish, mexican & chilean!)_









# Credits
Lvnatic's Satchels is an independently maintained Fabric port of [Vercte's Satchels](https://modrinth.com/mod/vercte-satchels) originally developed by **Vercte**! All original credits and licensing are retained under the MIT License.