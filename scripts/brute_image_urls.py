import urllib.request

BASES = [
    "https://www.serebii.net/legendsz-a/custom/th/{k}{s}.jpg",
    "https://www.serebii.net/legendsz-a/custom/{k}{s}.jpg",
    "https://www.serebii.net/legendsz-a/custom/th/{s}{k}.jpg",
    "https://www.serebii.net/legendsz-a/custom/{s}{k}.jpg",
]
KEY = "1066"
SUFFIXES = ["", "f", "m", "F", "M", "-f", "-m", "_f", "_m", "a", "b", "2", "w", "g"]

def size(url):
    try:
        return len(urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": "x"}), timeout=8).read())
    except Exception:
        return 0

found = []
for base in BASES:
    for s in SUFFIXES:
        url = base.format(k=KEY, s=s)
        n = size(url)
        if n > 1000:
            found.append((n, url))

for n, url in sorted(found, reverse=True):
    print(n, url)
