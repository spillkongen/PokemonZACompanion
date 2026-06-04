import json
import urllib.request
from collections import defaultdict
from pathlib import Path

JSON_PATH = Path(r"C:\Users\marti\PokemonZACompanion\app\src\main\assets\fashion_serebii.json")
items = json.loads(JSON_PATH.read_text(encoding="utf-8"))["items"]

by_name = defaultdict(list)
for it in items:
    by_name[it["name"]].append(it)

# names with many styles - check preview keys
for name in ["Blouse & Skort Set", "Biker Jacket Set", "Blazer & Blouse Set"]:
    rows = by_name[name][:4]
    print(name, [(r["style"], r.get("previewKey")) for r in rows])

# Test if custom/KEY.jpg differs between masculine/feminine same name
import urllib.request

def fetch_size(key):
    url = f"https://www.serebii.net/legendsz-a/custom/{key}.jpg"
    try:
        d = urllib.request.urlopen(
            urllib.request.Request(url, headers={"User-Agent": "x"}), timeout=10
        ).read()
        return len(d)
    except Exception:
        return 0

fem = [i for i in items if i.get("feminineCut")][:3]
masc = [i for i in items if not i.get("feminineCut")][:3]
print("\nfull image sizes:")
for label, group in [("fem", fem), ("masc", masc)]:
    for i in group:
        k = i.get("previewKey")
        print(f"  {label} {i['name'][:25]} key={k} size={fetch_size(k)}")

# Search page for second preview img ids
raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "x"},
    ),
    timeout=60,
).read().decode("iso-8859-1", "replace")
import re

imgs = re.findall(r'id="([^"]*-regular)"', raw)
print("\nregular ids:", imgs)
# any img near tops with two large previews?
for m in re.finditer(r'<img[^>]+id="([^"]+)"[^>]+src="([^"]+)"', raw):
    iid, src = m.group(1), m.group(2)
    if "custom" in src or "regular" in iid:
        if "female" in iid or "male" in iid or "tops" in iid or "all-in" in iid:
            print("img", iid, src[:60])
