"""Download fashion thumbnails and full previews into assets (build-time only)."""
import json
import re
import urllib.request
from pathlib import Path

JSON_PATH = Path(r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\fashion_serebii.json")
THUMB_DIR = Path(r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\fashion\th")
FULL_DIR = Path(r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\fashion\full")


def thumb_id(item: dict) -> str | None:
    key = item.get("previewKey")
    if key:
        return str(key)
    url = item.get("imageUrl") or ""
    m = re.search(r"/th/(\d+)\.jpg", url)
    return m.group(1) if m else None


def download(url: str, dest: Path) -> bool:
    if dest.exists() and dest.stat().st_size > 80:
        return True
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "PokemonZACompanion/1.4"})
        data = urllib.request.urlopen(req, timeout=25).read()
        if len(data) < 80:
            return False
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(data)
        return True
    except Exception:
        return False


def main():
    data = json.loads(JSON_PATH.read_text(encoding="utf-8"))
    items = data["items"]
    ok = 0
    new_dl = 0
    for i, item in enumerate(items):
        tid = thumb_id(item)
        if not tid:
            continue
        thumb_url = item.get("imageUrl") or f"https://www.serebii.net/legendsz-a/custom/th/{tid}.jpg"
        full_url = f"https://www.serebii.net/legendsz-a/custom/{tid}.jpg"
        tdest = THUMB_DIR / f"{tid}.jpg"
        fdest = FULL_DIR / f"{tid}.jpg"
        had = tdest.exists()
        if download(thumb_url, tdest):
            ok += 1
            if not had:
                new_dl += 1
        if not fdest.exists():
            download(full_url, fdest)
        item["previewKey"] = tid
        item["thumbAsset"] = f"fashion/th/{tid}.jpg"
        item["fullAsset"] = f"fashion/full/{tid}.jpg"
        item.pop("imageUrl", None)
        if (i + 1) % 200 == 0:
            print(f"{i + 1}/{len(items)}")
    data["bundledOnly"] = True
    JSON_PATH.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    print("Done thumbs:", ok, "of", len(items), "| newly downloaded:", new_dl)


if __name__ == "__main__":
    main()
