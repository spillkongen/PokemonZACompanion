import re
import urllib.request

raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "x"},
    ),
    timeout=60,
).read().decode("iso-8859-1", "replace")

ids = re.findall(r'id="([^"]+)"', raw)
for i in ids:
    if any(x in i for x in ("top", "female", "male", "custom", "in-one", "bottom", "hair")):
        print(i)

# search female word contexts
idx = 0
while True:
    i = raw.lower().find("female", idx)
    if i < 0:
        break
    print("ctx:", raw[max(0, i - 100) : i + 150].replace("\n", " "))
    idx = i + 6
