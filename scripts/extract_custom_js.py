import re
import urllib.request

raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "x"},
    ),
    timeout=60,
).read().decode("iso-8859-1", "replace")

for term in ["male", "female", "fem", "gender", "all-in-one-select", "custom/", "preview"]:
    idx = 0
    hits = []
    while True:
        i = raw.lower().find(term.lower(), idx)
        if i < 0:
            break
        hits.append(raw[max(0, i - 80) : i + 120].replace("\n", " "))
        idx = i + len(term)
        if len(hits) >= 5:
            break
    if hits:
        print(f"\n=== {term} ===")
        for h in hits:
            print(h)
