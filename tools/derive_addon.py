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
With --table it also writes the ForgingTable rows for the addon's armour and
decorations: every shaped recipe whose ingredients are all metal (plus at most
one base piece) is forged 1:1 on the anvil, like the base mod's armour. Pieces
with cloth, leather, straps, dye or feathers, and pieces built on a base that is
itself not forged (EXCLUDED_ARMOR), stay vanilla crafting and are listed as
comments. Values (hammering, casting amount, part names) are heuristics meant
to be reviewed, not gospel.
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
# Base-mod counterparts: zweihander (3 ingots + shortsword blade) is hammering 6.
HAMMERING_OVERRIDES = {"german_greatsword": 6}
# Overgeared matches the 3x3 forging grid exactly (no shifting, no mirroring), so two blades
# with the same metal layout collide once the handle cells are gone. Resolved like the base
# mod does it: smallest plausible deviation from the original shape (tools/check_conflicts.py).
PATTERN_OVERRIDES = {
    "grand_falchion": [" II", "II ", "I  "],   # heavier than the cavalry saber: one more ingot along the blade
    "battleaxe":      ["II ", "II ", "   "],   # broad double-bit head; war_axe and lochaberaxe keep the L
    "war_axe":        ["I  ", "II ", "   "],   # one-handed axe: L flipped, blade facing down
    "war_hammer":     ["I  ", " I ", "   "],   # small head + pick; lucernhammer keeps the pole layout
    "scythe":         ["III", "  I", "   "],   # long curved blade with tang
    "bollock_dagger": ["   ", "I  ", "   "],   # single-ingot heads by class: daggers top/left, spears middle
    "short_spear":    ["   ", "  I", "   "],   #   right, pike keeps the centre, stylet the top right
    "goedendag":      ["   ", "   ", "  I"],   # spike at the far end of the club
}
# Not blades: forged directly into the addon item (ForgingTable rows, see --table).
DIRECT = {"mustache_decoration", "skirt_decoration", "puff_and_slash_boots", "puff_and_slash_chestplate"}

# Armour / decoration ingredients that may sit in the anvil grid: letter, ForgingTable key spec
# (a constant name from ForgingTable or a literal "item:"/"tag:" spec).
METAL_ARMOR = {
    "#c:ingots/steel":                   ("I", "HEATED_STEEL"),
    "magistuarmory:steel_ingot":         ("I", "HEATED_STEEL"),
    "#c:plates/steel":                   ("P", "STEEL_PLATE"),
    "#c:nuggets/steel":                  ("N", "STEEL_NUGGET"),
    "magistuarmory:steel_nugget":        ("N", "STEEL_NUGGET"),
    "#magistuarmory:small_plates/steel": ("S", "SMALL_PLATE"),
    "#magistuarmory:chainmails/steel":   ("C", "CHAINMAIL"),
    "#magistuarmory:rings/steel":        ("R", "STEEL_RING"),
    "#c:nuggets/gold":                   ("G", "GOLD_NUGGET"),
    "#c:ingots/gold":                    ("A", "GOLD_INGOT"),
}
# Pure-metal pieces built on a base that is not forged by us (cloth, leather, mail, lamellar,
# brigandine): riveting onto those is needlework in the base mod's convention -> stays crafting.
EXCLUDED_ARMOR = {
    "chained_gambeson":               "base gambeson_chestplate is cloth",
    "chained_gambeson_boots":         "base gambeson_boots is cloth/leather",
    "xiii_century_knight_boots":      "base chainmail_boots is leather",
    "xiii_century_knight_leggings":   "base pantyhose is cloth",
    "xiii_century_knight_chestplate": "base chainmail_chestplate is not forged",
    "heavy_brigandine_chestplate":    "base brigandine_chestplate is not forged",
    "mirror_chestplate":              "base lamellar_chestplate is not forged",
    "chainmail_hood_decoration":      "base chainmail_helmet is not forged",
}
# Base armour is hammering 4..11, roughly the number of metal cells; keep the hand-set values.
ARMOR_HAMMERING_OVERRIDES = {"puff_and_slash_chestplate": 7, "puff_and_slash_boots": 7, "mustache_decoration": 3}
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
    if name in PATTERN_OVERRIDES:
        forge_rows = PATTERN_OVERRIDES[name]
        by_letter = {v[0]: v for v in METAL.values()}
        cells = [c for row in forge_rows for c in row if c != " "]
        units = sum(by_letter[c][2] for c in cells)
        weight = sum(by_letter[c][3] for c in cells)
        metal_cells = len(cells)
        forge_keys = {c: by_letter[c][1] for c in dict.fromkeys(cells)}
        ingot_only = all(by_letter[c][1] == "ingot" for c in cells)
    # Recipes that consume a finished shortsword/stylet reuse its hilt; our blade needs one at assembly.
    if not assembly and any(spec.startswith("blade:") for spec in forge_keys.values()):
        assembly.append("tag:magistuarmory:hilts")
    hammering = HAMMERING_OVERRIDES.get(name, max(3, min(7, 2 + round(weight))))
    castable_type = ingot_only and metal_cells <= 2
    display = lang.get(f"item.{ADDON}.steel_{name}") or name.replace("_", " ").title()
    display = re.sub(r"^Steel ", "", display)
    on_pole = "tag:magistuarmory:poles" in assembly
    part = "Head" if on_pole or any(w in name for w in HEAD_WORDS) else "Blade"
    tooltype = f"ek_{name}" if name in RESERVED_TOOLTYPES else None
    return {
        "name": name, "display": display, "part": part, "tooltype": tooltype,
        "pattern": forge_rows, "keys": forge_keys, "assembly": assembly,
        # casting needs a single metal and at least one ingot's worth of it
        "cast": int(units) if units >= 9 and "G" not in forge_keys and "H" not in forge_keys else 0,
        "hammering": hammering, "castable_type": castable_type,
        "mode": "hilt" if "tag:magistuarmory:hilts" in assembly else "pole",
    }


def derive_armor(name, recipe, lang, steel_prefixed):
    """ForgingTable row for a pure-metal shaped recipe, or (None, reason) when it stays crafting."""
    if name in EXCLUDED_ARMOR:
        return None, EXCLUDED_ARMOR[name]
    keys = {k: ingredient_id(v) for k, v in recipe["key"].items()}
    base = [i for i in set(keys.values()) if i not in METAL_ARMOR and not i.startswith("#")
            and i.split(":")[0] in ("magistuarmory", ADDON)]
    soft = sorted(i for i in set(keys.values()) if i not in METAL_ARMOR and i not in base)
    if soft:
        return None, "not pure metal: " + ", ".join(soft)
    if len(base) > 1:
        return None, "several base items: " + ", ".join(base)
    pattern = [row.ljust(3) for row in recipe["pattern"]] + ["   "] * (3 - len(recipe["pattern"]))
    letters, rows, cells = {}, [], 0
    for row in pattern:
        out = ""
        for ch in row:
            ing = keys.get(ch)
            if ing is None:
                out += " "
            elif ing in METAL_ARMOR:
                letter, spec = METAL_ARMOR[ing]
                out += letter
                letters[letter] = spec
                cells += 1
            else:
                out += "B"
                letters["B"] = f'"item:{ing}"'
        rows.append(out)
    item = f"steel_{name}" if steel_prefixed else name
    display = lang.get(f"item.{ADDON}.{item}") or name.replace("_", " ").title()
    display = re.sub(r"^Steel ", "", display)
    hammering = ARMOR_HAMMERING_OVERRIDES.get(name, max(4, min(11, cells)))
    tooltype = f"ek_{name}" if name in RESERVED_TOOLTYPES else name
    return {"name": name, "display": display, "item": item, "tooltype": tooltype,
            "category": "misc" if "decoration" in name else "armor",
            "hammering": hammering, "pattern": rows, "keys": letters}, None


def java_row(d):
    rows = ", ".join(f'"{r}"' for r in d["pattern"])
    keys = ", ".join(f"k('{c}', {spec})" for c, spec in d["keys"].items())
    tooltype = "" if d["tooltype"] == d["name"] else f', "{d["tooltype"]}"'
    return (f'        addon("{d["name"]}", "{d["display"]}", "{d["category"]}", {d["hammering"]}, '
            f'"{ADDON}:{d["item"]}"{tooltype},\n                p({rows}), {keys});')


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
    ap.add_argument("--table", help="write ForgingTable rows (addon armour/decorations) here")
    args = ap.parse_args()

    with zipfile.ZipFile(args.jar) as z:
        names = z.namelist()
        lang_path = f"assets/{ADDON}/lang/en_us.json"
        lang = json.loads(z.read(lang_path).decode("utf-8-sig")) if lang_path in names else {}
        recipe_dir = next(p for p in (f"data/{ADDON}/recipe/", f"data/{ADDON}/recipes/") if any(n.startswith(p) for n in names))
        recipes, others = {}, {}
        for n in sorted(names):
            if not (n.startswith(recipe_dir) and n.endswith(".json")) or "/" in n[len(recipe_dir):]:
                continue
            rel = n[len(recipe_dir):-5]
            r = json.loads(z.read(n).decode("utf-8-sig"))
            if rel.startswith("steel_"):
                recipes[rel[len("steel_"):]] = r
            elif r.get("type") == "minecraft:crafting_shaped":
                others[rel] = r

    blades, direct, assembly_only = [], [], []
    for name, recipe in recipes.items():
        if recipe.get("type") != "minecraft:crafting_shaped":
            print(f"// skipped {name}: {recipe.get('type')}", file=sys.stderr)
            continue
        d = derive(name, recipe, lang)
        if all(spec.startswith("blade:") for spec in d["keys"].values()) and len(d["keys"]) == 1:
            assembly_only.append(d)          # e.g. glaive = shortsword blade + pole + rod
        else:
            (direct if name in DIRECT else blades).append(d)

    out = ["    // ── Epic Knights: Addon (steel only; generated by tools/derive_addon.py) ──", ""]
    out += [java_entry(d) for d in blades]
    out += ["", "    // Assembly only (no blade item, like ranseur) — for ForgingTable.ASSEMBLY_ONLY:"]
    for d in assembly_only:
        blade = next(iter(d["keys"].values()))
        out.append(f"    // {d['name']}: {blade} + {d['assembly']}")
    out += ["", "    // Direct forging (no blade item) — see --table / ForgingTable."]
    text = "\n".join(out) + "\n"

    # ── ForgingTable rows: the four steel_ DIRECT pieces plus every pure-metal armour piece ──
    rows, skipped = [], []
    for name in sorted(DIRECT):
        d, why = derive_armor(name, recipes[name], lang, True)
        (rows.append(java_row(d)) if d else skipped.append((name, why)))
    for name, r in others.items():
        d, why = derive_armor(name, r, lang, False)
        (rows.append(java_row(d)) if d else skipped.append((name, why)))
    table = ["        // ── Epic Knights: Addon — pure-metal armour and decorations, generated by",
             "        //    tools/derive_addon.py --table (grid 1:1 the addon recipe, steel only) ──"]
    table += rows
    table += ["", "        // Stays addon crafting (not pure metal, or base not forged by us):"]
    table += [f"        //   {n}: {why}" for n, why in skipped]
    if args.table:
        with open(args.table, "w", encoding="utf-8") as f:
            f.write("\n".join(table) + "\n")
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
    print(f"{len(blades)} blade types, {len(assembly_only)} assembly-only, "
          f"{len(rows)} forging-table rows, {len(skipped)} left as crafting", file=sys.stderr)


if __name__ == "__main__":
    main()
