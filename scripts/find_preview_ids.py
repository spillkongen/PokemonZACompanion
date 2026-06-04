import re
import urllib.request

raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "x"},
    ),
    timeout=60,
).read().decode("iso-8859-1", "replace")

for pat in [
    r"id=['\"][^'\"]*(?:male|female|regular)[^'\"]*['\"]",
    r"custom/[0-9]+[a-z]?\.jpg",
    r"custom/[0-9]+-[a-z]+\.jpg",
    r"getElementById\('[^']+'\)",
]:
    hits = list(dict.fromkeys(re.findall(pat, raw, re.I)))
    if hits:
        print(pat, len(hits))
        for h in hits[:20]:
            print(" ", h)

# extract all getElementById lines for custom
for line in raw.split("\n"):
    if "getElementById" in line and "custom" in line:
        print(line.strip()[:200])
