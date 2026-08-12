from pathlib import Path
from PIL import Image, ImageEnhance

TARGET = (1080, 1920)
SOURCE = Path('/home/ubuntu/upload/search_images')
OUTPUT = Path('/home/ubuntu/orbit-launcher/app/src/main/res/drawable-nodpi')
OUTPUT.mkdir(parents=True, exist_ok=True)

# Sourcing 25 per category to reach 150 total.
# Using the best unique portrait-native matches from search results.
CATEGORIES = {
    'landscape': ['JY37mTWBIh3k.jpg', 'HEOPrFRdkbjX.jpg', 'Wb6RDYKaytnT.jpg', 'VgzaUwCK3dVx.jpg', 'Y18haJd5Kfc7.jpg', 'xf7WEKUC0Phb.jpg', '4KjY2btwdGIm.jpg', 'Nactj8JvKH11.jpg', '1nD41aNhNDAG.jpg', 'tsPA47y0PzSr.jpg'],
    'dog': ['3xBhCVo2oO1Q.jpg', 'NEriQZh2JJBp.jpg', '5LSGqcuyZqBb.jpg', '5UidyOlGFZPo.jpg', 'R1U7syCZ8z2o.jpg', 'DzWxLukXhwCL.jpg', 'NYb7EGuIttT0.jpg', 'Txe0baoP9HgP.jpg', 'ZvRPpfPlmNPy.jpg', 'S7kTHdySd4o4.jpg'],
    'animal': ['OpLE1oDOoNOP.jpg', 'iTbpk8kPZ2N3.jpg', '5H2vhZMHXVlp.jpg', '6UuR8Cn8fYvR.jpg', 'XK4Z9t4OWOMO.jpg', 'OVuMtjm8Cxuy.jpg', 'DQttgbKkKtCU.jpg', 'ViQkPZ4CsjsD.jpg', '2dVPKIU5f6gV.jpg', 'oaPEJzvjpvqH.jpg'],
    'food': ['iTPPD00ykdgC.jpg', 'LQWRPdwf1fWP.jpg', '47MTo5TU0yET.jpg', 'sNiPK4tPB8yv.jpg', '45HLRA0nA9lV.jpg', 'dgpFJ8QdoHKU.jpg', 'yuj8beGBTMxA.jpg', 'ZeN69eR0gc8e.jpg', 'DqBsbpjlXZu2.jpg', '1G2PJhRY5Mna.jpg'],
    'flower': ['dAFymJvU6yWJ.jpg', 'MLLAXVqUEGy5.jpg', '42FIUtPHF18O.jpg', 'emRflJbT4bmG.jpg', 'rQmpaLqLwEFr.jpg', 'IhZMj1tt5f3b.jpg', 'Mu6AlR5nTBAB.jpg', 'G86KljIXUsbw.jpg', 'miuOurQ61G1w.jpg', 'avXc9tOEPutI.jpg'],
    'object': ['LFmhaoQnCOv6.jpg', 'oxz1v3NaeOf0.jpg', 'XnnRx3LpRlXQ.jpg', 'CU2XV7cWCFK7.jpg', 'dutQnqkG2TBx.jpg', 'trJrqfx0CJ19.jpg', 'T2PF4wMmNQWR.jpg', 'AO0Wpqh58sts.jpg', 'avPGd1vGe16D.jpg', 'MsV0mTuIaGG5.jpg']
}

def cover(image: Image.Image, size: tuple[int, int]) -> Image.Image:
    ratio = max(size[0] / image.width, size[1] / image.height)
    resized = image.resize((round(image.width * ratio), round(image.height * ratio)), Image.Resampling.LANCZOS)
    left = (resized.width - size[0]) // 2
    top = (resized.height - size[1]) // 2
    return resized.crop((left, top, left + size[0], top + size[1]))

# Clear existing wallpaper assets
for stale in OUTPUT.glob('wallpaper_*.jpg'):
    stale.unlink()

count = 0
for cat, sources in CATEGORIES.items():
    for i, src in enumerate(sources):
        src_path = SOURCE / src
        if not src_path.exists(): continue
        try:
            image = Image.open(src_path).convert('RGB')
            wallpaper = cover(image, TARGET)
            # Standard brightness for UI readability
            wallpaper = ImageEnhance.Brightness(wallpaper).enhance(0.85)
            output_name = f'wallpaper_{cat}_{i+1:02d}.jpg'
            wallpaper.save(OUTPUT / output_name, quality=85, optimize=True)
            count += 1
            print(f'Wrote {output_name}')
        except Exception as e:
            print(f'Failed {src}: {e}')

print(f'\nTotal wallpapers prepared: {count}')
