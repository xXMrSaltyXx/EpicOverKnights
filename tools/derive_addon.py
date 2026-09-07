#!/usr/bin/env python3
"""Derive Overgeared blade definitions from the Epic Knights: Addon crafting recipes.

The addon crafts every weapon in a vanilla 3x3 grid from metal parts (ingots,
nuggets, plates, a shortsword) plus a handle (hilt, pole, wooden rod). The
metal cells become the Overgeared forging pattern, the handle parts become the
assembly ingredients — the same split the base mod uses by hand.

Usage:
  derive_addon.py <epic-knights-addon jar> [--java out.java] [--masks blade_masks.json]

Prints the BladeType enum entries (paste into BladeType.java, ADDON section)
and writes the blade-mask config used by tools/textures/extract_blade.py.
Values (hammering, casting amount, part names) are heuristics meant to be
reviewed, not gospel.
"""
import argparse
import json
import re
import sys
import zipfile

ADDON = "magistuarmoryaddon"

# Metal ingredients keep their grid cell. key letter, forging key spec, metal units (ingot = 9), hammering weight
METAL = {
    "magistuarmory:steel_nugget":      ("N", "item:overgeared:steel_nugget",        1,  0.15),
    "#c:nuggets/steel":                ("N", "item:overgeared:steel_nugget",        1,  0.15),
    "#c:ingots/steel":                 ("I", "ingot",                               9,  1.0),
    "magistuarmory:steel_ingot":       ("I", "ingot",                               9,  1.0),
    "#c:plates/steel":                 ("P", "item:overgeared:steel_plate",         9,  1.0),
    "#magistuarmory:small_plates/steel": ("S", "item:magistuarmory:small_steel_plate", 6, 0.6),
    "magistuarmory:steel_shortsword":  ("#", "blade:shortsword",                    18, 2.0),
    "magistuarmory:steel_stylet":      ("*", "blade:stylet",                        9,  1.0),
    "#c:nuggets/gold":                 ("G", "tag:{common}:nuggets/gold",           0,  0.15),
    "magistuarmory:halfarmor_chestplate": ("H", "item:magistuarmory:halfarmor_chestplate", 0, 3.0),
}
# Handle ingredients leave the grid and become assembly ingredients.
HANDLE = {
    "#magistuarmory:hilts": "tag:magistuarmory:hilts",
    "#magistuarmory:poles": "tag:magistuarmory:poles",
    "#c:rods/wooden":       "tag:{common}:rods/wooden",
}
# Not blades: forged directly into the addon item (see AddonForging table).
DIRECT = {"mustache_decoration", "skirt_decoration", "puff_and_slash_boots", "puff_and_slash_chestplate"}
HEAD_WORDS = ("hammer", "mace", "axe", "star", "beak", "goedendag", "fork", "lance", "spear")
# Overgeared's built-in tool types we must not collide with.
RESERVED_TOOLTYPES = {"sword", "axe", "pickaxe", "shovel", "hoe", "knife", "fillet_knife", "machete", "cleaver",
                      "mattock", "bow", "helmet", "chestplate", "leggings", "boots", "horse_armor", "thrown",
                      "staff", "polearm", "blunt_weapon", "dagger", "shield", "dragon_armor"}


def ingredient_id(ing):
    if "item" in ing:
        return ing["item"]
    return "#" + ing["tag"]


def derive(name, recipe, lang):
    pattern = [row.ljust(3) for row in recipe["pattern"]] + ["   "] * (3 - len(recipe["pattern"]))
    keys = {k: ingredient_id(v) for k, v in recipe["key"].items()}
    forge_rows, forge_keys, assembly = [], {}, []
    units = 0.0
    weight = 0.0
    ingot_only = True
    metal_cells = 0
    for row in pattern:
        out = ""
        for ch in row:
            ing = keys.get(ch)
            if ing is None:
                out += " "
            elif ing in METAL:
                letter, spec, u, wgt = METAL[ing]
                out += letter
                forge_keys[letter] = spec
                units += u
                weight += wgt
                metal_cells += 1
                if spec != "ingot":
                    ingot_only = False
            elif ing in HANDLE:
                out += " "
                if HANDLE[ing] not in assembly:
                    assembly.append(HANDLE[ing])
            else:
                raise SystemExit(f"{name}: unknown ingredient {ing}")
        forge_rows.append(out)
    # Recipes that consume a finished shortsword/stylet reuse its hilt; our blade needs one at assembly.
    if not assembly and any(spec.startswith("blade:") for spec in forge_keys.values()):
        assembly.append("tag:magistuarmory:hilts")
    hammering = max(3, min(7, 2 + round(weight)))
    castable_type = ingot_only and metal_cells <= 2
    display = lang.get(f"item.{ADDON}.steel_{name}") or name.replace("_", " ").title()
    display = re.sub(r"^Steel ", "", display)
    part = "Head" if any(w in name for w in HEAD_WORDS) else "Blade"
    tooltype = f"ek_{name}" if name in RESERVED_TOOLTYPES else None
    return {
        "name": name, "display": display, "part": part, "tooltype": tooltype,
        "pattern": forge_rows, "keys": forge_keys, "assembly": assembly,
        # casting needs a single metal and at least one ingot's worth of it
        "cast": int(units) if units >= 9 and "G" not in forge_keys and "H" not in forge_keys else 0,
        "hammering": hammering, "castable_type": castable_type,
        "mode": "hilt" if "tag:magistuarmory:hilts" in assembly else "pole",
    }


def java_entry(d):
    const = d["name"].upper()
    if const in ("MESSER_SWORD",):
        const = "ADDON_" + const  # base enum already has a MESSER_SWORD (iron, base mod)
    parts = [f'spec("{d["name"]}", "{d["display"]}").addon()']
    if d["part"] != "Blade":
        parts.append(f'.part("{d["part"]}")')
    if d["tooltype"]:
        parts.append(f'.tooltype("{d["tooltype"]}")')
    if d["cast"]:
        parts.append(f'.cast({d["cast"]})')
    if d["castable_type"]:
        parts.append(".castableType()")
    rows = ", ".join(f'"{r}"' for r in d["pattern"])
    parts.append(f'.forge({d["hammering"]}, {rows})')
    for letter, spec in d["keys"].items():
        spec = spec.replace("{common}", '" + Mappings.COMMON + "')
        parts.append(f".key('{letter}', \"{spec}\")")
    if d["assembly"]:
        ings = ", ".join('"' + a.replace("{common}", '" + Mappings.COMMON + "') + '"' for a in d["assembly"])
        parts.append(f".assemble({ings})")
    body = "\n            ".join(parts)
    return f"    {const}({body}),"


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("jar")
    ap.add_argument("--java", help="write enum entries here (default: stdout)")
    ap.add_argument("--masks", help="write blade-mask config here")
    args = ap.parse_args()

    with zipfile.ZipFile(args.jar) as z:
        names = z.namelist()
        lang_path = f"assets/{ADDON}/lang/en_us.json"
        lang = json.loads(z.read(lang_path).decode("utf-8-sig")) if lang_path in names else {}
        recipe_dir = next(p for p in (f"data/{ADDON}/recipe/", f"data/{ADDON}/recipes/") if any(n.startswith(p) for n in names))
        recipes = {}
        for n in sorted(names):
            if n.startswith(recipe_dir) and n.endswith(".json") and n[len(recipe_dir):].startswith("steel_"):
                recipes[n[len(recipe_dir) + len("steel_"):-5]] = json.loads(z.read(n).decode("utf-8-sig"))

    blades, direct = [], []
    for name, recipe in recipes.items():
        if recipe.get("type") != "minecraft:crafting_shaped":
            print(f"// skipped {name}: {recipe.get('type')}", file=sys.stderr)
            continue
        d = derive(name, recipe, lang)
        (direct if name in DIRECT else blades).append(d)

    out = ["    // ── Epic Knights: Addon (steel only; generated by tools/derive_addon.py) ──", ""]
    out += [java_entry(d) for d in blades]
    out += ["", "    // Direct forging (no blade item) — for the AddonForging table:"]
    for d in direct:
        keys = ", ".join(f"'{k}'=\"{v}\"" for k, v in d["keys"].items())
        out.append(f"    // {d['name']}: hammering={d['hammering']} pattern={d['pattern']} keys=[{keys}] assembly={d['assembly']}")
    text = "\n".join(out) + "\n"
    if args.java:
        with open(args.java, "w", encoding="utf-8") as f:
            f.write(text)
    else:
        print(text)

    if args.masks:
        masks = {"_comment": "per-weapon overrides for tools/textures/extract_blade.py; mode derived from the recipe's handle"}
        for d in blades:
            masks[d["name"]] = {"mode": d["mode"]}
        with open(args.masks, "w", encoding="utf-8") as f:
            json.dump(masks, f, indent=2)
            f.write("\n")
    print(f"{len(blades)} blade types, {len(direct)} direct-forged items", file=sys.stderr)


if __name__ == "__main__":
    main()
