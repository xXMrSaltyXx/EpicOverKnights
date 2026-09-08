#!/usr/bin/env python3
"""Generate the non-steel blade textures from the steel master.

Epic Knights ships every weapon in several materials that are palette swaps of
the same shape. For each weapon this script learns the colour map
steel_<weapon>.png -> <material>_<weapon>.png from the Epic Knights textures and
applies it to our hand-made steel_<weapon>_blade.png. Colours that only exist in
the master (hand-painted pixels) are mapped through the closest known colour.

Usage:
  gen_materials.py --source libs/mvn/local/epic-knights-neoforge/10.12/epic-knights-neoforge-10.12.jar \
      --textures src/main/resources/assets/overgeared_epic_knights/textures/item [--force] [--check] [weapon ...]

Without weapon arguments every steel_*_blade.png in the textures directory is
processed. Existing files are kept unless --force is given; --check only reports
how many pixels of an existing file the generator would reproduce.
"""
import argparse
import collections
import glob
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from pixels import TextureSource, load, save  # noqa: E402

MATERIALS = ["iron", "bronze", "copper", "gold", "silver", "tin", "stone"]


def colour_map(src, weapon, material):
    if not (src.has(f"steel_{weapon}") and src.has(f"{material}_{weapon}")):
        return None
    _, _, steel = src.load(f"steel_{weapon}")
    _, _, other = src.load(f"{material}_{weapon}")
    votes = collections.defaultdict(collections.Counter)
    for p, c in steel.items():
        o = other.get(p)
        if c[3] > 0 and o and o[3] > 0:
            votes[c[:3]][o[:3]] += 1
    return {k: v.most_common(1)[0][0] for k, v in votes.items()}


def nearest(colour, cmap):
    return min(cmap, key=lambda k: sum((k[i] - colour[i]) ** 2 for i in range(3)))


def apply(cmap, w, h, px):
    out = {}
    for p, c in px.items():
        if c[3] == 0:
            continue
        key = c[:3] if c[:3] in cmap else nearest(c[:3], cmap)
        out[p] = cmap[key] + (c[3],)
    return out


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--source", required=True, help="Epic Knights jar or extracted directory")
    ap.add_argument("--namespace", default="magistuarmory")
    ap.add_argument("--textures", required=True, help="our textures/item directory (masters and output)")
    ap.add_argument("--materials", default=",".join(MATERIALS))
    ap.add_argument("--force", action="store_true", help="overwrite existing textures")
    ap.add_argument("--check", action="store_true", help="compare against existing textures, write nothing")
    ap.add_argument("weapons", nargs="*")
    args = ap.parse_args()

    src = TextureSource(args.source, args.namespace)
    weapons = args.weapons or sorted(
        os.path.basename(f)[len("steel_"):-len("_blade.png")]
        for f in glob.glob(os.path.join(args.textures, "steel_*_blade.png")))
    materials = args.materials.split(",")

    total_ok = total_px = 0
    for weapon in weapons:
        master_path = os.path.join(args.textures, f"steel_{weapon}_blade.png")
        if not os.path.isfile(master_path):
            print(f"{weapon}: no steel master, skipped")
            continue
        w, h, master = load(master_path)
        report = []
        for material in materials:
            cmap = colour_map(src, weapon, material)
            target = os.path.join(args.textures, f"{material}_{weapon}_blade.png")
            if cmap is None:
                report.append(f"{material}=no source variant")
                continue
            out = apply(cmap, w, h, master)
            if args.check:
                if os.path.isfile(target):
                    _, _, ref = load(target)
                    ok = sum(1 for p, c in out.items() if ref.get(p, (0, 0, 0, 0))[:3] == c[:3])
                    total_ok += ok
                    total_px += len(out)
                    report.append(f"{material}={ok}/{len(out)}")
                else:
                    report.append(f"{material}=missing")
                continue
            if os.path.exists(target) and not args.force:
                report.append(f"{material}=kept")
                continue
            save(w, h, out, target)
            report.append(f"{material}=written")
        print(f"{weapon:18s} " + " ".join(report))
    if args.check and total_px:
        print(f"identical pixels: {total_ok}/{total_px} = {100 * total_ok / total_px:.1f}%")


if __name__ == "__main__":
    main()
