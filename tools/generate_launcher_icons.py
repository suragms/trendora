from PIL import Image
from pathlib import Path

src = Path(r"C:\Users\SURAG\Documents\my-project\trendora\app\src\main\res\drawable\app_icon_source.jpg")
res = Path(r"C:\Users\SURAG\Documents\my-project\trendora\app\src\main\res")

img = Image.open(src).convert("RGBA")
w, h = img.size
side = min(w, h)
left = (w - side) // 2
top = (h - side) // 2
img = img.crop((left, top, left + side, top + side))

sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

for folder, size in sizes.items():
    out_dir = res / folder
    out_dir.mkdir(parents=True, exist_ok=True)
    resized = img.resize((size, size), Image.Resampling.LANCZOS)
    for name in (
        "ic_launcher.webp",
        "ic_launcher_round.webp",
        "ic_launcher.png",
        "ic_launcher_round.png",
    ):
        old = out_dir / name
        if old.exists():
            old.unlink()
    resized.save(out_dir / "ic_launcher.png", optimize=True)
    resized.save(out_dir / "ic_launcher_round.png", optimize=True)
    print(f"wrote {folder}/{size}x{size}")

drawable = res / "drawable"
drawable.mkdir(parents=True, exist_ok=True)
img.resize((432, 432), Image.Resampling.LANCZOS).save(drawable / "ic_launcher_foreground.png", optimize=True)
img.resize((512, 512), Image.Resampling.LANCZOS).save(drawable / "app_icon.png", optimize=True)
print("wrote drawable/ic_launcher_foreground.png")
print("wrote drawable/app_icon.png")
print(f"source size: {w}x{h}, cropped: {side}")
