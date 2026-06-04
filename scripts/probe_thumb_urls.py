import urllib.request

BASE = "https://www.serebii.net/legendsz-a/custom/th"
KEYS = ["1066", "805", "1"]  # blouse, romper, maybe first top

def ok(url):
    try:
        r = urllib.request.urlopen(
            urllib.request.Request(url, headers={"User-Agent": "x"}), timeout=8
        )
        d = r.read()
        return len(d) if r.status == 200 and len(d) > 500 else 0
    except Exception:
        return 0

patterns = [
    "{k}.jpg",
    "{k}m.jpg",
    "{k}f.jpg",
    "m{k}.jpg",
    "f{k}.jpg",
    "{k}_m.jpg",
    "{k}_f.jpg",
    "male/{k}.jpg",
    "female/{k}.jpg",
    "m/{k}.jpg",
    "f/{k}.jpg",
]

for k in KEYS:
    print(f"\nkey {k}")
    for p in patterns:
        url = f"{BASE}/{p.format(k=k)}"
        n = ok(url)
        if n:
            print(f"  {n:6} {url}")
