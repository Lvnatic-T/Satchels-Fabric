# Changelog v1.1.0:
## Quality of Life Improvements
### More configuration!
All these new settings can be found in Minecraft's Controls menu, where all other Satchel options are already
- Added an option to customize the Satchel hotbar animation speed
- Added an option to disable Satchel activation/deactivation messages
- Added a tooltip preview showing the contents of filled Satchels _(holding shift)_
- You can now configure the automatic Satchel item pickup:
  - **Off:** Disables automatic pickup into the Satchel
  - **Overflow Only:** Prioritizes the regular inventory and only uses the Satchel when necessary. *(Default)*
  - **Prioritize Satchel:** Preserves the 1.0 pickup behavior

### Persistent Satchel Inventory
- Satchels can now be unequipped even if items are inside
- They now retain their inventory contents when unequipped/dropped
- Satchels still honor the keepInventory & keepAccessoryInventory gamerules
- Dying now drops it with its contents intact _(also fixes compatibility with some Gravestone mods)_

## Fixes & Miscellaneous
- Shift clicking items from containers no longer puts them into Satchels
- Fixed a bug causing the hotbar to not work properly after switching dimensions with a Satchel activated