"""Generate Kalkulasa app icons from the approved logo design (Draft Logo V2).

Logo concept: Rangkiang Digital - curved roof of a Minang rice barn above a
calculator body. One amber key = the final result: profit.
Palette: Teal Lumbung #0F4C5C, Amber Cuan #F5A623, Tinta Malam #0A2A33, Krem Kasa #FFF7E8.
"""
import math
import os
from PIL import Image, ImageDraw

TEAL = (15, 76, 92, 255)       # 0F4C5C
AMBER = (245, 166, 35, 255)    # F5A623
INK = (10, 42, 51, 255)        # 0A2A33
CREAM = (255, 247, 232, 255)   # FFF7E8


def rr(draw, box, radius, fill):
    draw.rounded_rectangle(box, radius=radius, fill=fill)


def draw_logo(size: int, bg, roof, body, key, key_accent, monochrome: bool = False):
    """Draw the Kalkulasa mark centered on a `size` canvas."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    if bg is not None:
        rr(ImageDraw.Draw(img), (0, 0, size - 1, size - 1), int(size * 0.22), bg)
    d = ImageDraw.Draw(img)
    s = size  # shorthand

    # Roof: crescent (outer arc dipping down, inner arc cancelling)
    cx = s / 2
    roof_cy = s * 0.44
    outer_rx, outer_ry = s * 0.31, s * 0.15
    inner_rx, inner_ry = s * 0.235, s * 0.135
    roof_top = roof_cy - outer_ry
    # Draw outer filled ellipse then carve with background/transparent
    roof_img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    rd = ImageDraw.Draw(roof_img)
    rd.pieslice(
        [cx - outer_rx, roof_cy - outer_ry, cx + outer_rx, roof_cy + outer_ry * 1.9],
        start=180, end=360, fill=roof,
    )
    rr_img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    rdd = ImageDraw.Draw(rr_img)
    rdd.pieslice(
        [cx - inner_rx, roof_cy - inner_ry, cx + inner_rx, roof_cy + inner_ry * 1.9],
        start=180, end=360, fill=(0, 0, 0, 0),
    )
    # carve: subtract inner ellipse from the outer crescent using alpha compositing
    mask = Image.new("L", (s, s), 0)
    md = ImageDraw.Draw(mask)
    md.pieslice([cx - outer_rx, roof_cy - outer_ry, cx + outer_rx, roof_cy + outer_ry * 2.2], 180, 360, fill=255)
    md.pieslice([cx - inner_rx, roof_cy - inner_ry * 0.35, cx + inner_rx, roof_cy + inner_ry * 2.2], 180, 360, fill=0)
    crescent = Image.new("RGBA", (s, s), roof)
    crescent.putalpha(mask)
    img = Image.alpha_composite(img, crescent)
    d = ImageDraw.Draw(img)

    # Calculator body
    bw, bh = s * 0.46, s * 0.26
    bx, by = (s - bw) / 2, s * 0.50
    body_radius = s * 0.075
    rr(d, (bx, by, bx + bw, by + bh), body_radius, body)

    # Keys: 2 rows x 3 cols
    key_w, key_h = s * 0.095, s * 0.082
    gap_x = (bw - 3 * key_w) / 4
    gap_y = (bh - 2 * key_h) / 3
    for row in range(2):
        for col in range(3):
            kx = bx + gap_x + col * (key_w + gap_x)
            ky = by + gap_y + row * (key_h + gap_y)
            is_accent = (row == 1 and col == 2)
            rr(d, (kx, ky, kx + key_w, ky + key_h), s * 0.022, key_accent if is_accent else key)
    return img


def fg_canvas(size: int, logo_size_ratio: float, **kwargs):
    """Foreground with logo in the adaptive-icon safe zone (66%)."""
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    inner = int(size * logo_size_ratio)
    logo = draw_logo(inner, None, **kwargs)
    off = (size - inner) // 2
    canvas.alpha_composite(logo, (off, off))
    return canvas


BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(BASE, "app", "src", "main", "res")

DENSITIES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}

# Variants: (folder_suffix, bg, roof, body, key, accent)
variants = {
    "ic_launcher": (TEAL, CREAM, CREAM, TEAL, AMBER),
}
for name, (bg, roof, body, key, accent) in variants.items():
    for density, size in DENSITIES.items():
        out_dir = os.path.join(RES, f"mipmap-{density}")
        os.makedirs(out_dir, exist_ok=True)
        round_icon = draw_logo(size, None, roof, body, key, accent)
        # round: circular crop mask
        mask = Image.new("L", (size, size), 0)
        ImageDraw.Draw(mask).ellipse((0, 0, size - 1, size - 1), fill=255)
        bg_img = Image.new("RGBA", (size, size), bg)
        base = Image.composite(bg_img, Image.new("RGBA", (size, size), (0, 0, 0, 0)), mask)
        inner = draw_logo(size, None, roof, body, key, accent)
        base = Image.alpha_composite(base, inner)
        base.save(os.path.join(out_dir, "ic_launcher_round.png"))
        square = draw_logo(size, bg, roof, body, key, accent)
        square.save(os.path.join(out_dir, "ic_launcher.png"))

# Adaptive icon foreground (432px for xxxhdpi baseline) and background color
fg_dir = os.path.join(RES, "drawable")
os.makedirs(fg_dir, exist_ok=True)
fg = fg_canvas(432, 0.66, roof=CREAM, body=CREAM, key=TEAL, key_accent=AMBER)
fg.save(os.path.join(fg_dir, "ic_launcher_foreground.png"))
mono = fg_canvas(432, 0.66, roof=(255, 255, 255, 255), body=(255, 255, 255, 255),
                 key=(0, 0, 0, 0), key_accent=(0, 0, 0, 0))
mono.save(os.path.join(fg_dir, "ic_launcher_monochrome.png"))

anydpi = os.path.join(RES, "mipmap-anydpi-v26")
os.makedirs(anydpi, exist_ok=True)
adaptive = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
"""
for fname in ("ic_launcher.xml", "ic_launcher_round.xml"):
    with open(os.path.join(anydpi, fname), "w", encoding="utf-8") as fh:
        fh.write(adaptive)

# Play Store assets
store_dir = os.path.join(BASE, "assets", "store")
os.makedirs(store_dir, exist_ok=True)
store_icon = draw_logo(512, TEAL, CREAM, CREAM, TEAL, AMBER)
store_icon.save(os.path.join(store_dir, "icon-512.png"))

# Feature graphic 1024x500: teal background, mark on left, wordmark
fg_graphic = Image.new("RGBA", (1024, 500), TEAL)
logo500 = draw_logo(360, None, CREAM, CREAM, TEAL, AMBER)
fg_graphic.alpha_composite(logo500, (70, 70))
from PIL import ImageFont
try:
    font_big = ImageFont.truetype("arialbd.ttf", 150)
    font_small = ImageFont.truetype("arial.ttf", 40)
except OSError:
    font_big = ImageFont.load_default()
    font_small = ImageFont.load_default()
d = ImageDraw.Draw(fg_graphic)
d.text((500, 165), "Kalkulasa", font=font_big, fill=CREAM)
d.text((500, 330), "Usaha kecil, semua urusan beres.", font=font_small, fill=(255, 247, 232, 220))
fg_graphic.save(os.path.join(store_dir, "feature-graphic-1024x500.png"))
print("icons done")
