# Blade Recipe Generator

Automates creation of all required recipe JSON files for a new blade/tool type
under `src/main/resources/data/epicoverknights/recipes/**`.

## Quick start

```sh
# Preview what would be created (no files written)
./tools/blade_recipe_generator/generate_blade_type.sh --type saber --dry-run

# Generate all files
./tools/blade_recipe_generator/generate_blade_type.sh --type saber

# Overwrite already existing files
./tools/blade_recipe_generator/generate_blade_type.sh --type saber --force
```

Generated blade ids follow this naming scheme everywhere:

- `epicoverknights:<material>_<type>_blade`
- Example: `epicoverknights:iron_saber_blade`

## What it generates

| Folder                          | Files                                                                    |
|---------------------------------|--------------------------------------------------------------------------|
| `recipes/forging/<type>_blade/` | one per enabled material                                                 |
| `recipes/casting/<type>_blade/` | three per enabled material (blasting / furnace / smelting)               |
| `recipes/knapping/`             | `stone_<type>_blade.json` (can be disabled)                              |
| `recipes/crafting/<type>/`      | one per enabled material (blade + hilt → weapon)                         |
| `recipes/smithing/`             | one smithing upgrade recipe (default: `steel_<type>` → `diamond_<type>`) |
| `recipes/tooltypes/`            | one `<type>_to_tooltype.json` file listing generated blade items         |

## Flags

| Flag                       | Default                                        | Description                                        |
|----------------------------|------------------------------------------------|----------------------------------------------------|
| `--type <name>`            | —                                              | **Required.** New blade type name (e.g. `saber`)   |
| `--dry-run`                | off                                            | Print planned operations without writing files     |
| `--force`                  | off                                            | Overwrite existing files                           |
| `--with-knapping`          | **on**                                         | Generate stone knapping recipe                     |
| `--without-knapping`       | off                                            | Skip stone knapping recipe                         |
| `--with-stone-forging`     | off                                            | Generate stone forging recipe                      |
| `--without-stone-forging`  | **on**                                         | Skip stone forging recipe                          |
| `--with-smithing`          | **on**                                         | Generate smithing upgrade recipe                   |
| `--without-smithing`       | off                                            | Skip smithing upgrade recipe                       |
| `--with-tooltypes`         | **on**                                         | Generate item-to-tooltype mapping recipe           |
| `--without-tooltypes`      | off                                            | Skip item-to-tooltype mapping recipe               |
| `--smithing-base <mat>`    | `steel`                                        | Base weapon material used in smithing              |
| `--smithing-result <mat>`  | `diamond`                                      | Result weapon material used in smithing            |
| `--smithing-addition <id>` | `minecraft:diamond`                            | Addition item id for smithing transform            |
| `--smithing-template <id>` | `overgeared:diamond_upgrade_smithing_template` | Template item id                                   |
| `--crafting-handle <id>`   | `magistuarmory:hilt`                           | Handle item in the crafting recipe (use `magistuarmory:pole` for polearms) |
| `--hammering <n>`          | `3`                                            | Hammer strikes required in the forging minigame |
| `--casting-amount <n>`     | `9`                                            | Nugget units needed for casting (9 = 1 ingot; scale by ingot count of blade) |
| `--include-stone-tooltype` | off                                            | Also include the stone blade in `tooltypes` output |

## Customisation

### Recipe structure / patterns

Edit the template files in `templates/`:

| File                        | Controls                                   |
|-----------------------------|--------------------------------------------|
| `forging.json.tpl`          | Forging recipe structure & pattern         |
| `casting_blasting.json.tpl` | Blast-casting recipe                       |
| `casting_furnace.json.tpl`  | Furnace-casting recipe                     |
| `casting_smelting.json.tpl` | Smelting-casting recipe                    |
| `knapping_stone.json.tpl`   | Stone knapping recipe                      |
| `crafting.json.tpl`         | Shapeless crafting (blade + hilt → weapon) |
| `smithing.json.tpl`         | Smithing upgrade recipe                    |
| `tooltypes.json.tpl`        | `item_to_tooltype` mapping recipe          |

Available placeholders in templates:

| Placeholder                  | Replaced with                                     |
|------------------------------|---------------------------------------------------|
| `__TYPE__`                   | blade type (e.g. `saber`)                         |
| `__MATERIAL__`               | material name (e.g. `iron`)                       |
| `__TIER__`                   | forging tier (e.g. `iron`, `stone`)               |
| `__EXPERIENCE__`             | casting experience value (e.g. `0.2`)             |
| `__FORGING_KEY_KIND__`       | `"item"` or `"tag"`                               |
| `__FORGING_KEY_VALUE__`      | item/tag id (e.g. `forge:ingots/copper`)          |
| `__BLADE_ITEM__`             | `epicoverknights:<material>_<type>_blade`         |
| `__WEAPON_ITEM__`            | `magistuarmory:<material>_<type>`                 |
| `__SMITHING_BASE_ITEM__`     | smithing base weapon item id                      |
| `__SMITHING_RESULT_ITEM__`   | smithing result weapon item id                    |
| `__SMITHING_ADDITION_ITEM__` | smithing addition item id                         |
| `__SMITHING_TEMPLATE_ITEM__` | smithing template item id                         |
| `__TOOLTYPE_ITEMS__`         | formatted JSON item list for the tooltype mapping |
| `__CRAFTING_HANDLE__`        | handle item id in the crafting recipe             |
| `__HAMMERING__`              | hammer strikes for the forging minigame           |
| `__CASTING_AMOUNT__`         | nugget units required for casting                 |

### Materials & balancing

Edit `config/materials.tsv` (tab-separated):

```
# material  tier    experience  key_kind  key_value                      forge  cast  craft
iron        iron    0.2         item      overgeared:heated_iron_ingot   1      1     1
gold        iron    0.25        tag       forge:ingots/gold              1      1     1
```

- Set `forge_enabled`, `casting_enabled`, `crafting_enabled` to `0` to skip a category for a specific material.
- Lines starting with `#` are ignored.
- `tooltypes` uses all listed materials by default except `stone`.
- `smithing` is generated once per blade type and can be changed via flags or by editing `templates/smithing.json.tpl`.
