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
decorations, by what a smith would do with the ingredients:
  * stock metal that has to be hammered into shape (ingots, nuggets, plates,
    small plates, gold) -> forged 1:1 in the addon's grid; straps, leather,
    cloth, blaze powder or feathers in the recipe stay in their cells and are
    fitted while finishing (there is no separate "unfinished" item to split on);
  * nothing to hammer, only prefabricated parts (chainmail, rings, lamellar
    rows) and/or loose parts joined by hand onto a base we forge (Proto
    Maximilian = knight boots + blaze powder) -> shapeless Overgeared assembly,
    so the base's forging quality carries over, like the hussar wings;
  * nothing to hammer and no forged base (a gambeson with plates tied on, mail
    with cloth), or dye/wool/planks -> stays vanilla crafting, listed as a
    comment with the reason.
Hammering follows the author's rule: 3 for one occupied slot, +1 per further
slot. Casting amounts and part names are heuristics meant to be reviewed.
"""
import argparse
import json
import os
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
# Hammering = 3 + one per occupied slot beyond the first (the author's rule for the base mod).
def hammering_for(cells):
    return 2 + max(1, cells)


HAMMERING_OVERRIDES = {}
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

# Armour / decoration ingredients that may sit in the anvil grid: letter, ForgingTable constant
# (or a literal "item:"/"tag:" spec).
METAL_ARMOR = {
    "#c:ingots/steel":                   ("I", "HEATED_STEEL"),
    "magistuarmory:steel_ingot":         ("I", "HEATED_STEEL"),
    "#c:plates/steel":                   ("P", "STEEL_PLATE"),
    "#c:nuggets/steel":                  ("N", "STEEL_NUGGET"),
    "magistuarmory:steel_nugget":        ("N", "STEEL_NUGGET"),
    "#magistuarmory:small_plates/steel": ("S", "SMALL_PLATE"),
    "#magistuarmory:chainmails/steel":   ("C", "CHAINMAIL"),
    "#magistuarmory:rings/steel":        ("R", "STEEL_RING"),
    "#magistuarmory:lamellar_rows/steel": ("M", "LAMELLAR_ROW"),
    "#c:nuggets/gold":                   ("G", "GOLD_NUGGET"),
    "#c:ingots/gold":                    ("A", "GOLD_INGOT"),
}
# Stock metal: hammered into shape on the anvil. The rest of METAL_ARMOR (chainmail, rings,
# lamellar rows) is prefabricated and joined by hand, like the loose parts below.
STOCK_METAL = {"#c:ingots/steel", "magistuarmory:steel_ingot", "#c:plates/steel", "#c:nuggets/steel",
               "magistuarmory:steel_nugget", "#magistuarmory:small_plates/steel", "#c:nuggets/gold", "#c:ingots/gold"}
# Loose parts fitted while finishing; allowed in the grid next to stock metal and, without any
# stock metal but with a forged base, the ingredients of an assembly.
SOFT_ARMOR = {
    "#magistuarmory:leather_strips":     ("L", "LEATHER_STRIP"),
    "minecraft:leather":                 ("E", "LEATHER"),
    "#magistuarmory:woolen_fabrics":     ("W", "WOOL_FABRIC"),
    "minecraft:blaze_powder":            ("Z", "BLAZE_POWDER"),
    "minecraft:feather":                 ("F", "FEATHER"),
    "minecraft:leather_boots":           ("O", "LEATHER_BOOTS"),
}
# Base-mod pieces that carry forging quality without an armor() row (assembled, see the provider).
ASSEMBLED_BASE = {"wingedhussar_chestplate", "crusader_chestplate"}
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
    hammering = HAMMERING_OVERRIDES.get(name, hammering_for(metal_cells))
    castable_type = ingot_only and metal_cells <= 2
    display = lang.get(f"item.{ADDON}.steel_{name}") or name.replace("_", " ").title()
    display = re.sub(r"^Steel ", "", display)
    on_pole = "tag:magistuarmory:poles" in assembly
    part = "Head" if on_pole or any(w in name for w in HEAD_WORDS) else "Blade"
    tooltype = f"ek_{name}" if name in RESERVED_TOOLTYPES else None
    result = recipe["result"]
    result = result.get("id") or result.get("item") if isinstance(result, dict) else result
    return {
        "name": name, "display": display, "part": part, "tooltype": tooltype,
        # the recipe file name and the item id can differ (cavalry_saber -> steel_cavalry_sabre)
        "result": None if result == f"{ADDON}:steel_{name}" else result,
        "pattern": forge_rows, "keys": forge_keys, "assembly": assembly,
        # casting needs a single metal and at least one ingot's worth of it
        "cast": int(units) if units >= 9 and "G" not in forge_keys and "H" not in forge_keys else 0,
        "hammering": hammering, "castable_type": castable_type,
        "mode": "hilt" if "tag:magistuarmory:hilts" in assembly else "pole",
    }


def derive_armor(name, recipe, lang, forged):
    """Classify one shaped addon recipe: a ForgingTable row (kind forge/assembly) or (None, reason)."""
    keys = {k: ingredient_id(v) for k, v in recipe["key"].items()}
    ings = set(keys.values())
    base = sorted(i for i in ings if i not in METAL_ARMOR and i not in SOFT_ARMOR and not i.startswith("#")
                  and i.split(":")[0] in ("magistuarmory", ADDON))
    other = sorted(i for i in ings if i not in METAL_ARMOR and i not in SOFT_ARMOR and i not in base)
    if other:
        return None, "not forgeable: " + ", ".join(other)
    if len(base) > 1:
        return None, "several base items: " + ", ".join(base)
    stock = ings & STOCK_METAL
    if base and base[0].split(":")[1] not in forged:
        return None, f"base {base[0]} is not forged"
    if not stock and not base:
        return None, "nothing to hammer" + (", prefab parts joined by hand" if ings & set(METAL_ARMOR) else "")
    result = recipe["result"]
    result = result.get("id") or result.get("item") if isinstance(result, dict) else result
    item = result.split(":", 1)[1]
    display = re.sub(r"^Steel ", "", lang.get(f"item.{ADDON}.{item}") or name.replace("_", " ").title())
    pattern = [row.ljust(3) for row in recipe["pattern"]] + ["   "] * (3 - len(recipe["pattern"]))
    cells = [keys[ch] for row in pattern for ch in row if ch != " "]
    if not stock:
        # forged base + parts joined by hand: assemble, the base's quality carries over
        extra = [(SOFT_ARMOR.get(i) or METAL_ARMOR[i])[1] for i in cells if i != base[0]]
        return {"kind": "assembly", "name": name, "display": display, "item": item, "result": result,
                "base": f'"item:{base[0]}"', "extra": extra}, None
    letters, rows = {}, []
    for row in pattern:
        out = ""
        for ch in row:
            ing = keys.get(ch)
            if ing is None:
                out += " "
            elif ing in METAL_ARMOR or ing in SOFT_ARMOR:
                letter, spec = (METAL_ARMOR.get(ing) or SOFT_ARMOR[ing])
                out += letter
                letters[letter] = spec
            else:
                out += "B"
                letters["B"] = f'"item:{ing}"'
        rows.append(out)
    tooltype = f"ek_{name}" if name in RESERVED_TOOLTYPES else name
    return {"kind": "forge", "name": name, "display": display, "item": item, "result": result, "tooltype": tooltype,
            "category": "misc" if "decoration" in name else "armor",
            "hammering": hammering_for(len(cells)), "pattern": rows, "keys": letters}, None


def java_row(d):
    if d["kind"] == "assembly":
        extra = ", ".join(d["extra"])
        return (f'        assemble("{d["name"]}", {d["base"]}, "{d["result"]}", {extra});'
                f'  // {d["display"]}')
    rows = ", ".join(f'"{r}"' for r in d["pattern"])
    keys = ", ".join(f"k('{c}', {spec})" for c, spec in d["keys"].items())
    tooltype = "" if d["tooltype"] == d["name"] else f', "{d["tooltype"]}"'
    return (f'        addon("{d["name"]}", "{d["display"]}", "{d["category"]}", {d["hammering"]}, '
            f'"{d["result"]}"{tooltype},\n                p({rows}), {keys});')


def java_entry(d):
    const = d["name"].upper()
    if const in ("MESSER_SWORD",):
        const = "ADDON_" + const  # base enum already has a MESSER_SWORD (iron, base mod)
    parts = [f'spec("{d["name"]}", "{d["display"]}").addon()']
    if d["part"] != "Blade":
        parts.append(f'.part("{d["part"]}")')
    if d["tooltype"]:
        parts.append(f'.tooltype("{d["tooltype"]}")')
    if d["result"]:
        parts.append(f'.result("{d["result"]}")')
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
    ap.add_argument("--forging-table", default=os.path.join(os.path.dirname(__file__), "..", "src", "main", "java",
                    "com", "saltycodes", "overgearedepicknights", "datagen", "ForgingTable.java"),
                    help="ForgingTable.java, read for the base-mod armour we forge (bases an addon piece may build on)")
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

    # ── ForgingTable rows: the steel_ DIRECT pieces plus every armour piece the rule accepts.
    # A piece may build on a base we forge; bases from this very table are found by iterating.
    with open(args.forging_table, encoding="utf-8") as f:
        forged = set(re.findall(r'\barmor\("(\w+)"', f.read())) | ASSEMBLED_BASE
    # keyed by recipe file name: the addon has both steel_puff_and_slash_boots and a cloth puff_and_slash_boots
    pending = {**{"steel_" + n: (n, recipes[n]) for n in sorted(DIRECT)}, **{n: (n, r) for n, r in others.items()}}
    rows, skipped = [], {}
    while pending:
        progress = False
        for key, (name, r) in list(pending.items()):
            d, why = derive_armor(name, r, lang, forged)
            if d:
                rows.append(d)
                forged.add(d["item"])
                del pending[key]
                skipped.pop(key, None)
                progress = True
            else:
                skipped[key] = why
        if not progress:
            break
    forge_rows = [java_row(d) for d in rows if d["kind"] == "forge"]
    assemblies = [java_row(d) for d in rows if d["kind"] == "assembly"]
    table = ["        // ── Epic Knights: Addon — armour and decorations, generated by tools/derive_addon.py --table:",
             "        //    everything with stock metal to hammer, forged 1:1 in the addon's grid (loose parts fitted) ──"]
    table += forge_rows
    table += ["", "        // Nothing to hammer: parts joined by hand onto a forged base, its quality carries over"]
    table += assemblies
    table += ["", "        // Everything else in the addon stays vanilla crafting:"]
    table += [f"        //   {n}: {why}" for n, why in sorted(skipped.items())]
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
          f"{len(forge_rows)} forging-table rows, {len(assemblies)} assemblies, {len(skipped)} left as crafting", file=sys.stderr)


if __name__ == "__main__":
    main()
