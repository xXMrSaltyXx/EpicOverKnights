# Adding a New Blade Type — Step-by-Step Guide

This guide explains how to add a new weapon blade type (e.g. `longsword`, `saber`) to EpicOverKnights Forge.
Follow the steps in order. Steps marked **[REQUIRED]** must always be done. Optional steps depend on the weapon.

---

## Prerequisites

- Textures for the new blade (all 8 materials) are placed in:
  `src/main/resources/assets/epicoverknights/textures/item/`
  Naming convention: `{material}_{type}_blade.png`
  (e.g. `iron_longsword_blade.png`, `copper_longsword_blade.png`, …)

> **STOP if any texture is missing.** Datagen will fail partway through if a referenced texture does not exist, requiring a full re-run. Verify all 8 files (`copper_`, `tin_`, `bronze_`, `iron_`, `gold_`, `silver_`, `steel_`, `stone_`) are present before starting Step 1.

---

## Step 1 — Register the blade type in Java [REQUIRED]

File: `src/main/java/com/saltycodes/epicoverknights/items/BladeType.java`

Add an entry to the enum:
```java
NEW_TYPE("new_type"),
```

This automatically registers all 8 material variants as items and generates their models during datagen.

---

## Step 2 — Add translations [REQUIRED]

File: `src/main/resources/assets/epicoverknights/lang/en_us.json`

Add one entry per material plus the tooltype key:
```json
"item.epicoverknights.copper_new_type_blade": "Copper New Type Blade",
"item.epicoverknights.gold_new_type_blade": "Gold New Type Blade",
"item.epicoverknights.tin_new_type_blade": "Tin New Type Blade",
"item.epicoverknights.stone_new_type_blade": "Stone New Type Blade",
"item.epicoverknights.silver_new_type_blade": "Silver New Type Blade",
"item.epicoverknights.bronze_new_type_blade": "Bronze New Type Blade",
"item.epicoverknights.iron_new_type_blade": "Iron New Type Blade",
"item.epicoverknights.steel_new_type_blade": "Steel New Type Blade",
"tooltype.overgeared.new_type": "New Type Display Name"
```

---

## Step 3 — Comment out Magistu Armory for DataGen [REQUIRED]

File: `build.gradle`

Comment out the Epic Knights dependency so datagen can run without it:
```groovy
// Comment out when doing DataGen
// runtimeOnly fg.deobf("maven.modrinth:epic-knights-shields-armor-and-weapons:10.10")
```

---

## Step 4 — Run DataGen [REQUIRED]

```bash
./gradlew runData
```

This generates the item model JSON files in `src/generated/resources/`.

---

## Step 5 — Re-enable Magistu Armory [REQUIRED]

File: `build.gradle`

Uncomment the line again:
```groovy
runtimeOnly fg.deobf("maven.modrinth:epic-knights-shields-armor-and-weapons:10.10")
```

---

## Step 6 — Look up the original Epic Knights recipe [RECOMMENDED]

Check the Epic Knights source repo (branch `1.20.1`) to understand how the weapon is originally crafted:
https://github.com/Magistu/Epic-Knights/blob/1.20.1/

- What ingredients does it use?
- Is it a shaped or shapeless recipe?
- Does it use a hilt, a grip, or no handle at all?

This informs how you adapt the recipe for this mod's blade-first workflow.

---

## Step 7 — Decide on the forging recipe [REQUIRED]

The forging (and stone knapping) pattern **must mirror the shape of the original Epic Knights recipe**. Look at the original recipe grid and map it directly into the 3×3 forging pattern:
- Horizontal layout (`ingot | blade`) → place ingot and blade in the same row
- Diagonal layout → reproduce the diagonal
- Vertical layout → reproduce the vertical stack

### Standard forging (ingot only)

Use `#` for hammer positions in the 3×3 grid. The knapping recipe is auto-derived
by replacing `#` with `x` in the same positions.

| Parameter            | Guidance                                                               |
|----------------------|------------------------------------------------------------------------|
| `--hammering`        | Complexity. Simple: 3, Medium: 4, Complex: 5                          |
| `--pattern-row1/2/3` | Shape of the 3×3 grid. `#` = hammer strike position.                  |

**Existing patterns for reference:**
```
Pike:        "   " / " # " / "   "   hammering: 3  (small, simple)
Shortsword:  "  #" / " # " / "   "   hammering: 4  (diagonal, medium)
Katzbalger:  " # " / " # " / "   "   hammering: 4  (straight, medium)
Ahlspiess:   "  #" / "## " / " # "   hammering: 5  (L-shape, complex)
```

### Blade-from-blade forging (source blade + ingot in forge)

Use `--with-blade-forging` when the new blade is forged from an existing blade + ingot.
In the pattern, use `I` for the ingot position and `#` for the source blade position.

- Metallic materials: `forging_blade_from_blade.json.tpl` (overgeared:forging with 2 key types)
- Stone material: `crafting_blade_from_blade.json.tpl` (cobblestone + stone source blade, replaces knapping)

**Example — Bastardsword (ingot on top, blade below):**
```
Pattern: "I  " / "#  " / "   "   hammering: 5
```

---

## Step 8 — Decide on the weapon crafting handle [REQUIRED]

**Standard handle** (blade + hilt → weapon):
Most weapons use `magistuarmory:hilt`. This is the default.

**Different handle:**
If the weapon uses a different handle, pass `--crafting-handle magistuarmory:longsword_grip`.

The casting amount should reflect the total ingot cost of the blade:
- 1 ingot = 9 units (default)
- If the blade is forged from another blade + 1 ingot: source blade ingots + 9 (e.g. shortsword=9 + 9 = 18)

---

## Step 9 — Run the recipe generator [REQUIRED]

**Standard blade (ingots only):**
```bash
./tools/blade_recipe_generator/generate_blade_type.sh \
  --type new_type \
  --hammering <n> \
  --pattern-row1 "<row1>" \
  --pattern-row2 "<row2>" \
  --pattern-row3 "<row3>" \
  [--crafting-handle <handle_item_id>] \
  [--without-smithing] \
  [--without-knapping]
```

**Blade-from-blade (forged from source blade + ingot):**
```bash
./tools/blade_recipe_generator/generate_blade_type.sh \
  --type new_type \
  --hammering <n> \
  --pattern-row1 "I  " \
  --pattern-row2 "#  " \
  --pattern-row3 "   " \
  --casting-amount <source_blade_ingots + 9> \
  --with-blade-forging \
  --source-blade-type <source_type>
```

**Example — Bastardsword:**
```bash
./tools/blade_recipe_generator/generate_blade_type.sh \
  --type bastardsword \
  --hammering 5 \
  --pattern-row1 "I  " \
  --pattern-row2 "#  " \
  --pattern-row3 "   " \
  --casting-amount 18 \
  --with-blade-forging \
  --source-blade-type shortsword
```

This generates recipes in `src/main/resources/data/epicoverknights/recipes/`:
- `forging/{type}_blade/` — one per metallic material (standard ingot or blade+ingot)
- `casting/{type}_blade/` — blasting/furnace/smelting per castable material
- `crafting/{type}/` — weapon assembly (blade + hilt) + stone blade recipe (if blade-forging)
- `knapping/stone_{type}_blade.json` — stone knapping (only for standard forging)
- `smithing/steel_{type}_to_diamond_{type}.json` — smithing upgrade
- `tooltypes/{type}_to_tooltype.json` — tooltype mapping

---

## Step 10 — Remove original Magistu Armory recipes [REQUIRED]

File: `src/main/java/com/saltycodes/epicoverknights/RecipeRemover.java`

Add the weapon type name to `MAGISTU_WEAPON_TYPES`:
```java
private static final String[] MAGISTU_WEAPON_TYPES = {
    "stylet", "shortsword", "katzbalger", "pike", "ranseur", "ahlspiess", "bastardsword",
    "new_type"  // ← add here
};
```

This removes all `magistuarmory:{material}_{type}` recipes (including diamond) on server start,
so only the recipes from this mod are available.

---

## Notes

- **New material** (not blade type): Add to `BladeMaterial.java` AND `tools/blade_recipe_generator/config/materials.tsv`. No need for `BladeType.java` changes.
- **Forging template** is at `tools/blade_recipe_generator/templates/forging.json.tpl` — edit if all future blade types need a structural change.
- **Crafting handle IDs** can be verified in-game via JEI or by checking the Magistu Armory item registry.
- Run `./gradlew runData` again after any changes to Java item registration.
