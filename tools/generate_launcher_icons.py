"""
Generate Trendora launcher assets from the official uploaded logo.

- Does NOT redesign or alter artwork pixels (only resize + pad).
- Adaptive foreground: logo centered in the Android safe zone (~66%).
- Legacy mipmaps: full-bleed logo for pre-API-26 launchers.
"""
from PIL import Image
from pathlib import Path

SRC = Path(
    r"C:\Users\SURAG\AppData\Roaming\Cursor\User\workspaceStorage"
    r"\2b2e72df4dd1cc3504090a4e4e745ff0\images"
    r"\ChatGPT Image Sep 2, 2026, 06_41_04 PM-f585ab5c-9980-4d8d-a19c-03de4a30b926.jpg"
)
RES = Path(r"C:\Users\SURAG\Documents\my-project\trendora\app\src\main\res")

# Adaptive icon canvas is 108dp; safe zone is the inner ~66% (72dp).
# Foreground assets at xxxhdpi: 108dp * 4 = 432px.
FG_SIZE = 432
# Keep logo comfortably inside the safe zone (~62% of canvas).
SAFE_SCALE = 0.62

LEGACY_SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}


def square_crop(img: Image.Image) -> Image.Image:
    w, h = img.size
    side = min(w, h)
    left = (w - side) // 2
    top = (h - side) // 2
    return img.crop((left, top, left + side, top + side))


def sample_bg_color(img: Image.Image) -> tuple[int, int, int, int]:
    """Sample near-corner pixels for a seamless adaptive background."""
    px = img.load()
    w, h = img.size
    samples = [
        px[2, 2],
        px[w - 3, 2],
        px[2, h - 3],
        px[w - 3, h - 3],
        px[w // 2, 2],
        px[2, h // 2],
    ]
    r = sum(s[0] for s in samples) // len(samples)
    g = sum(s[1] for s in samples) // len(samples)
    b = sum(s[2] for s in samples) // len(samples)
    return (r, g, b, 255)


def padded_foreground(logo: Image.Image, canvas: int, scale: float, bg: tuple) -> Image.Image:
    out = Image.new("RGBA", (canvas, canvas), bg)
    target = max(1, int(canvas * scale))
    resized = logo.resize((target, target), Image.Resampling.LANCZOS)
    offset = ((canvas - target) // 2, (canvas - target) // 2)
    out.paste(resized, offset, resized)
    return out


def main() -> None:
    logo = square_crop(Image.open(SRC).convert("RGBA"))
    bg = sample_bg_color(logo)
    print(f"source={logo.size} bg_rgb={bg[:3]}")

    drawable = RES / "drawable"
    drawable.mkdir(parents=True, exist_ok=True)

    # Adaptive / splash foreground (safe-zone padded)
    fg = padded_foreground(logo, FG_SIZE, SAFE_SCALE, bg)
    fg_path = drawable / "ic_launcher_foreground.png"
    # Remove conflicting XML vector if present
    xml_fg = drawable / "ic_launcher_foreground.xml"
    if xml_fg.exists():
        xml_fg.unlink()
    fg.save(fg_path, optimize=True)
    print(f"wrote {fg_path}")

    # Solid background color resource helpers are written separately as XML.
    # Also keep a matching PNG background for anydpi adaptive icons.
    # Background color is provided via @color/ic_launcher_background in adaptive XML.

    # Legacy density icons (full logo — recognizable on older launchers)
    for folder, size in LEGACY_SIZES.items():
        out_dir = RES / folder
        out_dir.mkdir(parents=True, exist_ok=True)
        for name in (
            "ic_launcher.webp",
            "ic_launcher_round.webp",
            "ic_launcher.png",
            "ic_launcher_round.png",
        ):
            old = out_dir / name
            if old.exists():
                old.unlink()
        resized = logo.resize((size, size), Image.Resampling.LANCZOS)
        resized.save(out_dir / "ic_launcher.png", optimize=True)
        resized.save(out_dir / "ic_launcher_round.png", optimize=True)
        print(f"wrote {folder}/{size}x{size}")

    # Write sampled background hex for colors.xml consumers
    hex_color = f"#{bg[0]:02X}{bg[1]:02X}{bg[2]:02X}"
    print(f"IC_LAUNCHER_BG={hex_color}")


if __name__ == "__main__":
    main()
