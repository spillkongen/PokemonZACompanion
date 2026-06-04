import re
import urllib.request
from bs4 import BeautifulSoup

raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "x"},
    ),
    timeout=60,
).read().decode("iso-8859-1", "replace")

for pat in [
    r"legendsz-a/custom/[a-z0-9/_-]+\.jpg",
    r"data-[a-z-]+=\"[^\"]+\"",
    r"female|male|gender",
]:
    m = re.findall(pat, raw, re.I)
    if m:
        uniq = list(dict.fromkeys(m))[:12]
        print(pat, "count", len(m), "sample", uniq)

soup = BeautifulSoup(raw, "html.parser")
multi = 0
for row in soup.select("table.dextable tr")[1:]:
    imgs = row.find_all("img")
    if len(imgs) >= 2:
        multi += 1
        if multi <= 3:
            print("multi img row", [(i.get("src"), i.get("class")) for i in imgs])
print("rows with 2+ imgs:", multi)

anchors = soup.select("a[data-key]")[:5]
for a in anchors:
    print("anchor", {k: a.get(k) for k in a.attrs})

# scripts mentioning custom
for script in soup.find_all("script"):
    t = script.string or ""
    if "custom" in t.lower() or "gender" in t.lower():
        print("script snippet:", t[:400].replace("\n", " "))
