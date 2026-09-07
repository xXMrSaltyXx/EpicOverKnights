# Overgeared x Epic Knights

*A compatibility mod bridging Epic Knights and Overgeared*

</div>

## What does it do?

Epic Knights adds a large variety of medieval weapons and armor to Minecraft but out of the box, everything is crafted
through vanilla crafting tables, which clashes with Overgeared's material-based forging system.

This mod fixes that. All vanilla Epic Knights crafting recipes are **removed** and replaced with proper Overgeared
recipes using **forging**, **casting**, and **assembly**.

<table align="center"><tr>
<td align="center">

![Shortsword-Blade forging recipe](https://cdn.modrinth.com/data/cached_images/d7156a85793c7bf78ceb969e9ce4ebc410bc687b.png)<div align="center">

</td>
<td align="center" width="60">
<b>→</b>
</td>
<td align="center">

![Shortsword assembly using the Blade and a Hilt](https://cdn.modrinth.com/data/cached_images/df051f49809ae69e814561f4dd67d45c306e7ee2.png)

</td>
</tr></table>

**Blueprints** are also included for Armor, Shields and Weapons.

### New Items

To achieve this, the mod adds new intermediate **blade and component items**.
These work exactly like Overgeared's own components: you cast or forge the blade first, then assemble the final weapon.
This keeps the crafting process consistent with how Overgeared handles its own gear.

<details>
<summary>Steel Forging Items</summary>
  
![All steel forging items added by this mod, from Shortsword-Blade to Morgenster-Head](https://cdn.modrinth.com/data/cached_images/ec62d1cfc830efcb2e3c7b2d839bdf51811def58.png)

</details>

## Compatibility

| Mod | Status |
|-----|--------|
| [Epic Knights: Shields, Armor and Weapons](https://modrinth.com/mod/epic-knights-shields-armor-and-weapons) | 🟢 Supported |
| [Epic Knights: Addon](https://modrinth.com/mod/epic-knights-addon) | 🟢 Supported (optional) |

**Epic Knights: Addon** is optional. When it is installed, all of its steel weapons get the same
treatment as the base mod: forge the blade or head, then assemble it with a hilt, pole or rod
(the glaive, like the ranseur, is assembled straight from a shortsword blade). Its armour and
decorations follow the same rule as the base mod's: whatever has stock metal to hammer (ingots,
nuggets, plates) is forged on the anvil in the addon's own grid shape, straps, leather or cloth
included where the original has them. Whatever is only joined by hand onto a forged piece (mail
onto a helmet, blaze powder onto knight armour) is assembled in the crafting grid instead, so the
forging quality carries over. Pieces with nothing to hammer and no forged base (mail with cloth,
plates tied onto a gambeson), and dyed pieces, keep their crafting recipes. Without the addon
nothing of that is loaded.

| Minecraft Version | Status |
|-------------------|--------|
| 1.20.1 | 🟢 Supported |
| 1.21.1 | 🟢 Supported |

## Requirements

<table><tr>
<td align="center">
<a href="https://modrinth.com/mod/epic-knights-shields-armor-and-weapons">
<img src="https://cdn.modrinth.com/data/L6jvzao4/2eac93d65e0df5ce99db25a46209155281a37035.png" width="80" alt="Epic Knights"><br>
<img src="https://img.shields.io/badge/Epic%20Knights-required-e05252?style=flat-square&logo=modrinth&logoColor=white" alt="Epic Knights">
</a>
</td>
<td align="center">
<a href="https://modrinth.com/mod/overgeared">
<img src="https://cdn.modrinth.com/data/SQL3X2Ky/20c446ea6a90cbb8fcfd552bea7dbdc16388e31b_96.webp" width="80" alt="Overgeared"><br>
<img src="https://img.shields.io/badge/Overgeared-required-e05252?style=flat-square&logo=modrinth&logoColor=white" alt="Overgeared">
</a>
</td>
</tr></table>

## Credits & Disclaimer

### Original Mods
- **[Epic Knights: Shields, Armor and Weapons](https://modrinth.com/mod/epic-knights-shields-armor-and-weapons)** - Medieval weapons and armor system
- **[Epic Knights: Addon](https://modrinth.com/mod/epic-knights-addon)** - Additional weapons and armour (optional)
- **[Overgeared](https://modrinth.com/mod/overgeared)** - Material-based forging and crafting system

### Disclaimer
This is an **unofficial** compatibility mod created by the community.

All rights to the original mods remain with their respective creators. This mod only provides integration between Epic Knights and Overgeared by adding compatible recipes, intermediate crafting components, and blueprints to bridge the two systems.

### Development Tools
Parts of this mod's JSON generation were assisted by Claude Code to handle the repetitive transcription work involved in creating recipes for multiple items and materials. All design decisions, textures, recipe balancing, and item registration were done manually.

## Development

Blade textures and recipes are derived, not hand-made per material:

- `tools/textures/extract_blade.py` cuts a first draft of the blade/head master
  (`steel_<weapon>_blade.png`) out of an Epic Knights weapon texture (handle detection by colour,
  per-weapon overrides in `blade_masks.json`). The shipped masters, base and addon, are hand-edited;
  the script never overwrites an existing file unless asked to with `--force`.
- `tools/textures/gen_materials.py` derives every other material from the steel master using the
  palette of the Epic Knights originals.
- `tools/derive_addon.py` turns the addon's crafting recipes into `BladeType` entries and, with
  `--table`, into the `ForgingTable` rows for its armour (the classification rule is in the script's
  docstring); deliberate deviations from the original shapes live in its `PATTERN_OVERRIDES`.
- `tools/check_conflicts.py <generated resources dir>` finds forging grids, assembly ingredient sets,
  casting recipes or tool types that would collide. Overgeared matches the 3x3 forging grid exactly
  (no shifting, no mirroring), so two blades may only differ by position — run it after every change.
- Everything else (items, models, recipes, tool types, blueprints, names, recipe removal) follows
  from `BladeType` and `ForgingTable` at DataGen time: `./gradlew chiseledRunData`.

