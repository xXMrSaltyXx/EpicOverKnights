#!/usr/bin/env python3
"""Extract a blade/head master texture from an Epic Knights (or addon) weapon texture.

The handle (wood, leather, wrapped grips, gilded guards) is detected by colour
saturation, the metal part is the largest remaining connected component. For
hilted weapons the crossguard is cut away along the weapon axis; pole weapons
keep everything that is not handle. The result is cropped and centred on the
smallest canvas of 16/32/48/64 px.

Usage:
  extract_blade.py --source <jar|dir> --namespace magistuarmoryaddon \
      --out src/main/resources/assets/overgeared_epic_knights/textures/item \
      --config tools/textures/blade_masks.json [--force] [weapon ...]

Config (JSON): { "<weapon>": { "source": "steel_<weapon>",   # texture to read (default steel_<weapon>)
                               "mode": "hilt" | "pole" | "keep",   # default hilt
                               "guard_cut": 2,        # rows along the axis removed above the grip (hilt mode)
                               "axis_cut": null,      # absolute cut instead: drop pixels with x - y <= axis_cut
                               "saturation": 40,      # max (R-G-B spread) still counted as metal
                               "erase": [[x, y, w, h], ...],   # rectangles forced transparent (source coords)
                               "keep": [[x, y, w, h], ...],    # rectangles forced kept (source coords)
                               "min_component": 4,    # drop metal islands smaller than this
                               "canvas": 0 } }         # 0 = auto
Weapons not listed in the config use the defaults; every generated file is
listed in the output so the result can be reviewed in a montage.
"""
import argparse
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from pixels import TextureSource, save  # noqa: E402

DEFAULTS = {"mode": "hilt", "guard_cut": 2, "axis_cut": None, "saturation": 40, "erase": [], "keep": [],
            "min_component": 4, "canvas": 0}


def is_metal(rgba, sat):
    r, g, b, a = rgba
    return a > 0 and (max(r, g, b) - min(r, g, b)) <= sat


def components(cells):
    """8-connected components of a set of (x, y)."""
    cells = set(cells)
    comps = []
    while cells:
        seed = cells.pop()
        stack, comp = [seed], {seed}
        while stack:
            x, y = stack.pop()
            for dx in (-1, 0, 1):
                for dy in (-1, 0, 1):
                    n = (x + dx, y + dy)
                    if n in cells:
                        cells.remove(n)
                        comp.add(n)
                        stack.append(n)
        comps.append(comp)
    return sorted(comps, key=len, reverse=True)


def in_rects(p, rects):
    return any(rx <= p[0] < rx + rw and ry <= p[1] < ry + rh for rx, ry, rw, rh in rects)


def extract(w, h, px, cfg):
    sat = cfg["saturation"]
    opaque = {p for p, c in px.items() if c[3] > 0 and not in_rects(p, cfg["erase"])}
    metal = {p for p in opaque if is_metal(px[p], sat) or in_rects(p, cfg["keep"])}
    handle = opaque - metal

    if cfg["mode"] == "keep":
        blade = set(metal)
    else:
        comps = components(metal)
        if not comps:
            return None
        blade = set(comps[0])
        cut = cfg["axis_cut"]
        if cut is None and cfg["mode"] == "hilt" and handle:
            # Weapon axis runs bottom-left -> top-right: u = x - y grows towards the tip.
            # Everything up to guard_cut rows beyond the grip's end is guard, not blade.
            cut = max(x - y for x, y in handle) + cfg["guard_cut"]
        if cut is not None:
            blade = {p for p in blade if (p[0] - p[1]) > cut}
            comps = components(blade)
            if not comps:
                return None
            blade = set(comps[0])
        # Re-attach forced pixels and drop tiny islands.
        blade |= {p for p in metal if in_rects(p, cfg["keep"])}
        blade = set().union(*[c for c in components(blade) if len(c) >= cfg["min_component"]]) if blade else set()

    if not blade:
        return None
    xs = [p[0] for p in blade]
    ys = [p[1] for p in blade]
    bw, bh = max(xs) - min(xs) + 1, max(ys) - min(ys) + 1
    canvas = cfg["canvas"] or next(s for s in (16, 32, 48, 64, 128) if s >= max(bw, bh))
    ox = (canvas - bw) // 2 - min(xs)
    oy = (canvas - bh) // 2 - min(ys)
    out = {(x + ox, y + oy): px[(x, y)] for x, y in blade}
    return canvas, canvas, out


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--source", required=True, help="mod jar or extracted directory")
    ap.add_argument("--namespace", default="magistuarmory")
    ap.add_argument("--out", required=True, help="target textures/item directory")
    ap.add_argument("--config", help="per-weapon overrides (JSON)")
    ap.add_argument("--material", default="steel", help="material prefix of the source and output")
    ap.add_argument("--force", action="store_true", help="overwrite existing output files")
    ap.add_argument("weapons", nargs="*", help="weapon names (default: all weapons in the config)")
    args = ap.parse_args()

    config = {}
    if args.config and os.path.isfile(args.config):
        with open(args.config, encoding="utf-8") as f:
            config = json.load(f)
    weapons = args.weapons or [w for w in config if not w.startswith("_")]
    if not weapons:
        ap.error("no weapons given and config is empty")

    src = TextureSource(args.source, args.namespace)
    os.makedirs(args.out, exist_ok=True)
    written, skipped, failed = [], [], []
    for weapon in weapons:
        cfg = dict(DEFAULTS)
        cfg.update(config.get(weapon, {}))
        source_item = cfg.get("source", f"{args.material}_{weapon}")
        target = os.path.join(args.out, f"{args.material}_{weapon}_blade.png")
        if os.path.exists(target) and not args.force:
            skipped.append(weapon)
            continue
        if not src.has(source_item):
            failed.append(f"{weapon} (no texture {source_item})")
            continue
        w, h, px = src.load(source_item)
        result = extract(w, h, px, cfg)
        if result is None:
            failed.append(f"{weapon} (nothing left after masking)")
            continue
        cw, ch, out = result
        save(cw, ch, out, target)
        written.append(f"{weapon} ({w}x{h} -> {cw}x{ch}, {len(out)} px)")

    for line in written:
        print("wrote  ", line)
    for line in skipped:
        print("kept   ", line, "(exists, use --force to regenerate)")
    for line in failed:
        print("FAILED ", line)
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
