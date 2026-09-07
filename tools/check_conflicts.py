#!/usr/bin/env python3
"""Find recipe conflicts in the generated data (base pack + addon pack together).

Checks
  forging    two shaped forging recipes with the same grid. Overgeared's ForgingRecipe
             does not shrink patterns and never mirrors: a 3x3 pattern only matches in
             exactly that placement, so grids are compared as written. Pass --loose to
             also treat shifted and mirrored grids as equal (vanilla-style matching).
  shapeless  two shapeless recipes (assembly) with the same ingredient multiset
  casting    two casting recipes for the same tool type and material
  tooltype   one item mapped to several tool types
  ids        the same recipe id written by both packs

Usage:  check_conflicts.py versions/1.21.1-neoforge/src/generated/resources
Exit code 1 when a conflict was found.
"""
import collections
import json
import os
import sys


# Deliberate: one blade serving two tool types (morgenstern blade -> morgenstern and flail).
SHARED_TOOLTYPES = [{"morgenstern", "chainmorgenstern"}]


def ingredient_key(ing):
    if isinstance(ing, list):
        return "|".join(sorted(ingredient_key(i) for i in ing))
    if "item" in ing:
        return ing["item"]
    if "tag" in ing:
        return "#" + ing["tag"]
    return json.dumps(ing, sort_keys=True)


def grid_signature(pattern, key, loose):
    rows = [[ingredient_key(key[c]) if c != " " else None for c in row] for row in pattern]
    if not loose:
        return tuple(tuple(r) for r in rows)
    # trim empty rows / columns
    while rows and all(c is None for c in rows[0]):
        rows.pop(0)
    while rows and all(c is None for c in rows[-1]):
        rows.pop()
    if not rows:
        return ()
    width = max(len(r) for r in rows)
    rows = [r + [None] * (width - len(r)) for r in rows]
    while rows and all(r[0] is None for r in rows):
        rows = [r[1:] for r in rows]
    while rows and rows[0] and all(r[-1] is None for r in rows):
        rows = [r[:-1] for r in rows]
    return tuple(tuple(r) for r in rows)


def mirrored(sig):
    return tuple(tuple(reversed(r)) for r in sig)


def result_of(r):
    res = r.get("result", {})
    if isinstance(res, dict):
        return res.get("id") or res.get("item") or "?"
    return res


def walk(root):
    for dirpath, _, files in os.walk(root):
        if os.sep + ".cache" in dirpath:
            continue
        for f in files:
            if not f.endswith(".json"):
                continue
            path = os.path.join(dirpath, f)
            rel = os.path.relpath(path, root)
            parts = rel.split(os.sep)
            # data/<ns>/recipe(s)/... or datapacks/<pack>/data/<ns>/recipe(s)/...
            if "recipe" not in parts and "recipes" not in parts:
                continue
            idx = parts.index("recipe") if "recipe" in parts else parts.index("recipes")
            if idx < 1:
                continue
            ns = parts[idx - 1]
            rid = ns + ":" + "/".join(parts[idx + 1:])[:-5]
            pack = parts[1] if parts[0] == "datapacks" else "base"
            with open(path, encoding="utf-8") as fh:
                yield pack, rid, json.load(fh)


def main():
    loose = "--loose" in sys.argv
    roots = [a for a in sys.argv[1:] if a != "--loose"]
    if not roots:
        print(__doc__)
        return 2
    forging = collections.defaultdict(list)
    shapeless = collections.defaultdict(list)
    casting = collections.defaultdict(list)
    tooltypes = collections.defaultdict(set)
    ids = collections.defaultdict(list)
    for root in roots:
        for pack, rid, r in walk(root):
            ids[rid].append((pack, json.dumps(r, sort_keys=True)))
            t = r.get("type", "")
            if t == "overgeared:forging" and "pattern" in r:
                sig = grid_signature(r["pattern"], r["key"], loose)
                forging[sig].append((rid, result_of(r)))
            elif t.endswith("crafting_shapeless"):
                sig = tuple(sorted(ingredient_key(i) for i in r["ingredients"]))
                shapeless[sig].append((rid, result_of(r)))
            elif t in ("overgeared:casting", "overgeared:cast_smelting", "overgeared:cast_blasting"):
                for mat in r.get("input", {}):
                    casting[(t, r.get("tool_type"), mat)].append((rid, result_of(r)))
            elif t == "overgeared:item_to_tooltype":
                for item in r.get("item", []):
                    tooltypes[ingredient_key(item)].add(r.get("tooltype"))

    problems = 0

    def report(title, entries):
        nonlocal problems
        problems += 1
        print(f"CONFLICT {title}")
        for rid, res in entries:
            print(f"    {rid}  ->  {res}")

    seen = set()
    for sig, entries in forging.items():
        if sig in seen:
            continue
        group = list(entries)
        m = mirrored(sig) if loose else sig
        if m != sig and m in forging:
            group += forging[m]
            seen.add(m)
        seen.add(sig)
        if len({res for _, res in group}) > 1:
            report("forging grid " + " / ".join("".join("#" if c else "." for c in row) for row in sig), group)
    for sig, entries in shapeless.items():
        if len({res for _, res in entries}) > 1:
            report("shapeless " + ", ".join(sig), entries)
    for key, entries in casting.items():
        if len({res for _, res in entries}) > 1:
            report("casting " + str(key), entries)
    for item, types in tooltypes.items():
        if len(types) > 1 and types not in SHARED_TOOLTYPES:
            problems += 1
            print(f"CONFLICT tooltype {item} -> {sorted(types)}")
    for rid, entries in ids.items():
        if len(entries) > 1 and len({content for _, content in entries}) > 1:
            problems += 1
            print(f"CONFLICT recipe id {rid} written with different content by {[p for p, _ in entries]}")

    print(f"{'no conflicts' if not problems else str(problems) + ' conflict(s)'} "
          f"in {sum(len(v) for v in forging.values())} forging, {sum(len(v) for v in shapeless.values())} shapeless, "
          f"{sum(len(v) for v in casting.values())} casting recipes")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
