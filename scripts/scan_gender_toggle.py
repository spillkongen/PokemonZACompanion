import re
import urllib.request

raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "x"},
    ),
    timeout=60,
).read().decode("iso-8859-1", "replace")

# Look for second preview img or male/female toggle near tops-regular
idx = raw.find("tops-regular")
print("context around tops-regular:")
print(raw[max(0, idx - 500) : idx + 1200].replace("\n", " "))

for pat in [r"custom/\d+[a-z]\.jpg", r"custom/[mf]/", r"-male", r"-female", r"alt\.src", r"female-regular", r"male-regular"]:
    hits = re.findall(pat, raw, re.I)
    if hits:
        print(pat, list(dict.fromkeys(hits))[:15])
