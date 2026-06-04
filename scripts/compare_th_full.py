import hashlib
import urllib.request

KEYS = ["1066", "1088", "1167"]  # blouse, biker, blazer blouse

def fetch(url):
    return urllib.request.urlopen(
        urllib.request.Request(url, headers={"User-Agent": "x"}), timeout=15
    ).read()

for k in KEYS:
    th = fetch(f"https://www.serebii.net/legendsz-a/custom/th/{k}.jpg")
    full = fetch(f"https://www.serebii.net/legendsz-a/custom/{k}.jpg")
    print(
        k,
        "th", len(th), hashlib.md5(th).hexdigest()[:8],
        "full", len(full), hashlib.md5(full).hexdigest()[:8],
        "same", th == full,
    )
