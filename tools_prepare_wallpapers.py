from pathlib import Path
from PIL import Image, ImageEnhance

TARGET = (1080, 1920)
SOURCE = Path('/home/ubuntu/upload/search_images')
OUTPUT = Path('/home/ubuntu/orbit-launcher/app/src/main/res/drawable-nodpi')
OUTPUT.mkdir(parents=True, exist_ok=True)

# Every source below is already portrait-oriented. The output uses a direct 9:16 crop;
# it never adds blurred extension bands or letterbox panels around a landscape photo.
WALLPAPERS = {
    'wallpaper_mountain_reflection.jpg': 'JY37mTWBIh3k.jpg',
    'wallpaper_lakeside_silence.jpg': 'iA92cJqZlZDB.jpg',
    'wallpaper_emerald_forest.jpg': 'FUN05TmdmCt8.jpg',
    'wallpaper_blue_peak.jpg': 'Jv97HHSNzEY6.jpg',
    'wallpaper_canyon_falls.jpg': 'DumrlGBKc2eV.jpg',
    'wallpaper_forest_cascade.jpg': 'BmM01xUrOXeR.jpg',
    'wallpaper_alpine_falls.jpg': 'f2GDIHunmz2Q.jpg',
    'wallpaper_autumn_falls.jpg': 'VtH7epsCZy2O.jpg',
    'wallpaper_starlit_cliffs.jpg': 'GaazQFMkbvU8.jpg',
    'wallpaper_highland_lake.jpg': 'ePE0tiQ6hC8o.jpg',
    'wallpaper_golden_lake.jpg': 'lFRpRlP730p1.jpg',
    'wallpaper_sunset_bay.jpg': 'EeKR1hZuQu17.jpg',
}


def cover(image: Image.Image, size: tuple[int, int]) -> Image.Image:
    ratio = max(size[0] / image.width, size[1] / image.height)
    resized = image.resize((round(image.width * ratio), round(image.height * ratio)), Image.Resampling.LANCZOS)
    left = (resized.width - size[0]) // 2
    top = (resized.height - size[1]) // 2
    return resized.crop((left, top, left + size[0], top + size[1]))


for stale in OUTPUT.glob('wallpaper_*.jpg'):
    stale.unlink()

for output_name, source_name in WALLPAPERS.items():
    image = Image.open(SOURCE / source_name).convert('RGB')
    wallpaper = cover(image, TARGET)
    # Keep controls readable without adding any background bars or border treatment.
    wallpaper = ImageEnhance.Brightness(wallpaper).enhance(0.84)
    wallpaper.save(OUTPUT / output_name, quality=88, optimize=True, progressive=True)
    print(f'Wrote {output_name}')
