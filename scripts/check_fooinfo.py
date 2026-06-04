import re
import urllib.request
from bs4 import BeautifulSoup

raw = urllib.request.urlopen(
    urllib.request.Request(
        "https://www.serebii.net/legendsz-a/customisation.shtml",
        headers={"User-Agent": "Mozilla/5.0"},
    ),
    timeout=30,
).read()
text = raw.decode("latin-1")
soup = BeautifulSoup(raw, "html.parser", from_encoding="iso-8859-1")

# fooinfo divs with hidden content
foos = soup.find_all(class_="fooinfo")
print("fooinfo count", len(foos))
for td in foos[:5]:
    inner = td.decode_contents()[:300]
    print("---", inner.replace("\n", " ")[:250])

# search for second image in page per outfit id
ids = re.findall(r"/legendsz-a/custom/th/(\d+)\.jpg", text)
print("thumb ids", len(ids), "unique", len(set(ids)))

# duplicate ids with different path?
all_paths = re.findall(r"/legendsz-a/custom/[^\"'\s]+", text)
from collections import Counter
c = Counter(all_paths)
dups = [p for p, n in c.items() if "th/" in p and n > 1]
print("paths appearing 2+", len(dups))
for p in dups[:10]:
    print(p, c[p])

# look for /custom/ without /th/
other = [p for p in set(all_paths) if "/th/" not in p and "custom" in p]
print("non-th custom paths", other[:20])
