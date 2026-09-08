"""Tiny PNG <-> pixel-dict bridge on top of ImageMagick (no Pillow dependency).

Images are represented as (width, height, {(x, y): (r, g, b, a)}).
"""
import os
import subprocess
import tempfile
import zipfile

MAGICK = os.environ.get("MAGICK", "magick")


def load(path):
    txt = subprocess.run([MAGICK, str(path), "-depth", "8", "txt:-"],
                         capture_output=True, text=True, check=True).stdout
    lines = txt.splitlines()
    header = lines[0]  # "# ImageMagick pixel enumeration: W,H,255,srgba"
    w, h = (int(v) for v in header.split(":")[1].split(",")[:2])
    px = {}
    for line in lines[1:]:
        pos, rest = line.split(":", 1)
        x, y = (int(v) for v in pos.split(","))
        vals = rest.split("(")[1].split(")")[0].split(",")
        rgba = tuple(int(float(v)) for v in vals)
        if len(rgba) == 1:
            rgba = (rgba[0], rgba[0], rgba[0], 255)
        elif len(rgba) == 2:
            rgba = (rgba[0], rgba[0], rgba[0], rgba[1])
        elif len(rgba) == 3:
            rgba = rgba + (255,)
        px[(x, y)] = rgba
    return w, h, px


def save(w, h, px, path):
    fd, tmp = tempfile.mkstemp(suffix=".txt")
    with os.fdopen(fd, "w") as f:
        f.write(f"# ImageMagick pixel enumeration: {w},{h},255,srgba\n")
        for y in range(h):
            for x in range(w):
                r, g, b, a = px.get((x, y), (0, 0, 0, 0))
                f.write(f"{x},{y}: ({r},{g},{b},{a})  #{r:02X}{g:02X}{b:02X}{a:02X}  srgba({r},{g},{b},{a/255:.4f})\n")
    try:
        subprocess.run([MAGICK, tmp, "-define", "png:color-type=6", "PNG32:" + str(path)], check=True)
    finally:
        os.remove(tmp)


class TextureSource:
    """Reads item textures either from a directory or from a mod jar."""

    def __init__(self, location, namespace):
        self.namespace = namespace
        self._tmp = None
        if os.path.isdir(location):
            self.root = os.path.join(location, "assets", namespace, "textures", "item")
            if not os.path.isdir(self.root):
                self.root = location
        else:
            self._tmp = tempfile.mkdtemp(prefix="tex_")
            prefix = f"assets/{namespace}/textures/item/"
            with zipfile.ZipFile(location) as z:
                for name in z.namelist():
                    if name.startswith(prefix) and name.endswith(".png"):
                        z.extract(name, self._tmp)
            self.root = os.path.join(self._tmp, prefix)

    def path(self, item):
        return os.path.join(self.root, item + ".png")

    def has(self, item):
        return os.path.isfile(self.path(item))

    def load(self, item):
        return load(self.path(item))
