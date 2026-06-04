import urllib.request

BASE = "https://www.serebii.net/legendsz-a/custom"
KEYS = ["805", "1066", "544"]  # romper, blouse, romper
SUFFIXES = [
    "/{k}.jpg",
    "/{k}f.jpg",
    "/{k}-f.jpg",
    "/{k}_f.jpg",
    "/f/{k}.jpg",
    "/female/{k}.jpg",
    "/th/{k}f.jpg",
    "/th/f/{k}.jpg",
]

def exists(url):
    try:
        r = urllib.request.urlopen(
            urllib.request.Request(url, headers={"User-Agent": "x"}), timeout=10
        )
        data = r.read()
        return r.status, len(data)
    except Exception as e:
        return str(e)[:40], 0

for k in KEYS:
    print(f"\nkey {k}:")
    for suf in SUFFIXES:
        url = BASE + suf.format(k=k)
        status, size = exists(url)
        if size > 1000:
            print(f"  OK {size:6} {url}")
